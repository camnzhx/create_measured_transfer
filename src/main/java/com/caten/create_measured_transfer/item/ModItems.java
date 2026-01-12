package com.caten.create_measured_transfer.item;


import com.caten.create_measured_transfer.Create_measured_transfer;
import com.caten.create_measured_transfer.data_component.MeteringBarrelData;
import com.caten.create_measured_transfer.data_component.ModDataComponents;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS=
            DeferredRegister.createItems(Create_measured_transfer.MODID);

    public static final DeferredItem<Item> metering_barrel =
            ITEMS.registerItem("metering_barrel",MeteringBarrelItem::new,
                    new Item.Properties()
                            .stacksTo(1)
                            .component( ModDataComponents.METERING_BARREL_DATA, new MeteringBarrelData(0,"null")));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
