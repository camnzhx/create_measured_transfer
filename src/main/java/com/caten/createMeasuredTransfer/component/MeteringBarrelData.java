package com.caten.createMeasuredTransfer.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;


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

    public MeteringBarrelData copyWithFluid(FluidStack barrelFluid) {
        return new MeteringBarrelData(barrelFluid, this.Capacity);
    }

    public int getCapacity() {
        return Capacity;
    }

    public MeteringBarrelData setCapacity(int newCapacity){
        return new MeteringBarrelData(this.barrelFluid, newCapacity);
    }

    // 将流体设置为空，但保留容量不变
    public MeteringBarrelData keepCapacityEmpty(){return this.copyWithFluid(FluidStack.EMPTY);}

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
        if (barrelFluid.isEmpty()) return this.copyWithFluid(new FluidStack(newFluid, amount));
        else return this;
    }

    public int getAmount() {
        return barrelFluid.getAmount();
    }

    public MeteringBarrelData setAmount(int newAmount){
        if (barrelFluid.isEmpty())
            return this;
        barrelFluid.setAmount(newAmount);
        return this.copyWithFluid(this.barrelFluid.copyWithAmount(newAmount));
    }

    public String getFluidName() {
        if (!(barrelFluid.isEmpty())) {
            return barrelFluid.getFluid().getFluidType().getDescription().getString();
        }
        return Component.translatable("item.create_measured_transfer.metering_barrel.empty").getString();
    }
}
