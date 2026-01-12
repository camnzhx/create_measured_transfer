package com.caten.create_measured_transfer.data_component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Objects;

public record MeteringBarrelData(Integer FluidAmount, String FluidName ) {


    public static final Codec< MeteringBarrelData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("fluid_amount").forGetter(MeteringBarrelData::FluidAmount),
                    Codec.STRING.fieldOf("fluid_name").forGetter(MeteringBarrelData::FluidName)
            ).apply(instance, MeteringBarrelData::new)
    );

    public static final StreamCodec<ByteBuf, MeteringBarrelData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, MeteringBarrelData::FluidAmount,
            ByteBufCodecs.STRING_UTF8, MeteringBarrelData::FluidName,
            MeteringBarrelData::new
    );

}
