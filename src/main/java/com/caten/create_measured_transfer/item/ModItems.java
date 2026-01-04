package com.caten.create_measured_transfer.item;


import com.caten.create_measured_transfer.Create_measured_transfer;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS=
            DeferredRegister.createItems(Create_measured_transfer.MODID);

    public static final DeferredItem<Item> metering_barrel =
            ITEMS.registerItem("metering_barrel",Item::new);

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
