package com.caten.create_measured_transfer.data_component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;


public record MeteringBarrelData(FluidStack barrelFluid,int maxCapacity) {

    public static final Codec<MeteringBarrelData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    FluidStack.OPTIONAL_CODEC.fieldOf("barrel_fluid").forGetter(MeteringBarrelData::barrelFluid),
                    Codec.INT.fieldOf("max_capacity").forGetter(MeteringBarrelData::maxCapacity)
            ).apply(instance, MeteringBarrelData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MeteringBarrelData> STREAM_CODEC = StreamCodec.composite(
            FluidStack.OPTIONAL_STREAM_CODEC, MeteringBarrelData::barrelFluid,
            ByteBufCodecs.INT, MeteringBarrelData::maxCapacity,
            MeteringBarrelData::new
    );

    public static MeteringBarrelData CreateDefault(int maxCapacity){
        return new MeteringBarrelData(FluidStack.EMPTY, maxCapacity);
    }

    public MeteringBarrelData copyWithFluid(FluidStack barrelFluid) {
        return new MeteringBarrelData(barrelFluid, this.maxCapacity);
    }

    public MeteringBarrelData keepCapacityEmpty(){return this.copyWithFluid(FluidStack.EMPTY);}

    public FluidStack getFluidStack() {
        return barrelFluid;
    }

    public MeteringBarrelData set(FluidStack newFluidStack){
        return this.copyWithFluid(newFluidStack);
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
            return this.copyWithFluid(this.barrelFluid);
        barrelFluid.setAmount(newAmount);
        return this.copyWithFluid(this.barrelFluid.copyWithAmount(newAmount));
    }

    public boolean isEmpty() {
        return barrelFluid.isEmpty();
    }

    public String getFluidName() {
        if (!(barrelFluid.isEmpty())) {
            return barrelFluid.getFluid().getFluidType().getDescription().getString();
        }
        return "Empty";
    }
}
