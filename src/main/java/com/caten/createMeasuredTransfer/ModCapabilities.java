package com.caten.createMeasuredTransfer;


import com.caten.createMeasuredTransfer.item.MeteringBarrelItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = CreateMeasuredTransfer.MOD_ID)
public class ModCapabilities {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        MeteringBarrelItem.regesterCapability(event);
    }
}
