package com.caten.createMeasuredTransfer.component;

import com.caten.createMeasuredTransfer.ModDataComponents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;


public record MeteringBarrelData(FluidStack barrelFluid,int Capacity) {

    public static final Codec<MeteringBarrelData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    FluidStack.OPTIONAL_CODEC.fieldOf("barrel_fluid").forGetter(MeteringBarrelData::barrelFluid),
                    Codec.INT.fieldOf("max_capacity").forGetter(MeteringBarrelData::Capacity)
            ).apply(instance, MeteringBarrelData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MeteringBarrelData> STREAM_CODEC = StreamCodec.composite(
            FluidStack.OPTIONAL_STREAM_CODEC, MeteringBarrelData::barrelFluid,
            ByteBufCodecs.INT, MeteringBarrelData::Capacity,
            MeteringBarrelData::new
    );
    public boolean isEmpty() {
        return barrelFluid.isEmpty();
    }

    public static MeteringBarrelData CreateDefault(int maxCapacity){
        return new MeteringBarrelData(FluidStack.EMPTY, maxCapacity);
    }

    public MeteringBarrelData copyWithFluidStack(FluidStack barrelFluid) {
        return new MeteringBarrelData(barrelFluid, this.Capacity);
    }

    public int getCapacity() {
        return Capacity;
    }

    public MeteringBarrelData setCapacity(int newCapacity){
        return new MeteringBarrelData(this.barrelFluid, newCapacity);
    }

    // 将流体设置为空，但保留容量不变
    public MeteringBarrelData keepCapacityEmpty(){return this.copyWithFluidStack(FluidStack.EMPTY);}

    public FluidStack getFluidStack() {
        return barrelFluid;
    }

    public FluidState getFluidState(){
        return getFluid().defaultFluidState();
    }

    public Fluid getFluid(){
        return barrelFluid.getFluid();
    }

    public MeteringBarrelData setFluid(Fluid newFluid, int amount){
        return this.copyWithFluidStack(new FluidStack(newFluid, amount));
    }

    public int getAmount() {
        return barrelFluid.getAmount();
    }

    public MeteringBarrelData setAmount(int newAmount){
        if (newAmount <= 0 || this.isEmpty()) return this.copyWithFluidStack(FluidStack.EMPTY);
        barrelFluid.setAmount(newAmount);
        return this.copyWithFluidStack(this.barrelFluid.copyWithAmount(newAmount));
    }

    public String getFluidName() {
        if (!(barrelFluid.isEmpty())) {
            return barrelFluid.getFluid().getFluidType().getDescription().getString();
        }
        return Component.translatable("item.create_measured_transfer.empty").getString();
    }

    public  MeteringBarrelHandler getFluidHandler(ItemStack container) {
        return new MeteringBarrelHandler(container);
    }

    public static class MeteringBarrelHandler implements IFluidHandlerItem {
        private final ItemStack container;
        
        public MeteringBarrelHandler(ItemStack container) {
            this.container = container;
        }

        private MeteringBarrelData getData() {
            return container.get(ModDataComponents.METERING_BARREL_DATA);
        }

        private void updateData(MeteringBarrelData newData) {
            container.set(ModDataComponents.METERING_BARREL_DATA, newData);
        }

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            MeteringBarrelData data = getData();
            return data != null ? data.getFluidStack() : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            MeteringBarrelData data = getData();
            return data != null ? data.getCapacity() : 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return true;
        }

        @Override
        public int fill(@NotNull FluidStack resource, @NotNull FluidAction action) {
            MeteringBarrelData data = getData();
            if (data == null) return 0;
            
            FluidStack currentFluid = data.getFluidStack();
            if (currentFluid.isEmpty() || resource.is(currentFluid.getFluid())) {
                int amount = currentFluid.getAmount();
                int fillAmount = Math.min(resource.getAmount(), data.getCapacity() - amount);
                
                if (action.execute() && fillAmount > 0) {
                    FluidStack newFluid;
                    if (currentFluid.isEmpty()) {
                        newFluid = resource.copyWithAmount(fillAmount);
                    } else {
                        newFluid = currentFluid.copyWithAmount(amount + fillAmount);
                    }
                    updateData(data.copyWithFluidStack(newFluid));
                }
                return fillAmount;
            }
            return 0;
        }

        @Override
        public @NotNull FluidStack drain(@NotNull FluidStack resource, @NotNull FluidAction action) {
            MeteringBarrelData data = getData();
            if (data == null) return FluidStack.EMPTY;
            
            FluidStack currentFluid = data.getFluidStack();
            if (resource.is(currentFluid.getFluid())) {
                return drain(resource.getAmount(), action);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, @NotNull FluidAction action) {
            MeteringBarrelData data = getData();
            if (data == null) return FluidStack.EMPTY;
            
            FluidStack currentFluid = data.getFluidStack();
            if (!currentFluid.isEmpty()) {
                int amount = currentFluid.getAmount();
                int drainAmount = Math.min(maxDrain, amount);

                if (drainAmount > 0) {
                    if (action.execute()) {
                        FluidStack newFluid;
                        if (amount - drainAmount <= 0) {
                            newFluid = FluidStack.EMPTY;
                        } else {
                            newFluid = currentFluid.copyWithAmount(amount - drainAmount);
                        }
                        updateData(data.copyWithFluidStack(newFluid));
                    }
                    return currentFluid.copyWithAmount(drainAmount);
                }
            }
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack getContainer() {
            return container;
        }
    }
}
