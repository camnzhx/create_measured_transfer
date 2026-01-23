package com.caten.createMeasuredTransfer;

import com.caten.createMeasuredTransfer.component.MeteringBarrelData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.UnaryOperator;

public class ModDataComponents {
    private static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CreateMeasuredTransfer.MOD_ID);

    public static final DataComponentType<MeteringBarrelData> METERING_BARREL_DATA = register(
            "metering_barrel_data",
            builder -> builder.persistent(MeteringBarrelData.CODEC).networkSynchronized(MeteringBarrelData.STREAM_CODEC)
    );

    private static <T> DataComponentType<T> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
        DATA_COMPONENTS.register(name, () -> type);
        return type;
    }

    public static void register(IEventBus eventBus) {
        DATA_COMPONENTS.register(eventBus);
    }
}
