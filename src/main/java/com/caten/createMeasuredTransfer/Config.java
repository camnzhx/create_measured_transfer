package com.caten.createMeasuredTransfer;

import com.caten.createMeasuredTransfer.item.MeteringBarrelItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;


@EventBusSubscriber(modid = CreateMeasuredTransfer.MOD_ID)
public class Config {
    private static final ModConfigSpec.Builder REGISTER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.IntValue METERING_BARREL_MAX_CAPACITY = REGISTER
            .translation("config.create_measured_transfer.metering_barrel_max_capacity")
            .defineInRange("meteringBarrelMaxCapacity",4000,1000,10000);

    public static final IConfigSpec SPEC = REGISTER.build();

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event){
        MeteringBarrelItem.initMaxCapacity(METERING_BARREL_MAX_CAPACITY.get());
    }
}
