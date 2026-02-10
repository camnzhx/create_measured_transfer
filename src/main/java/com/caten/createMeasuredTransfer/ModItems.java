package com.caten.createMeasuredTransfer;


import com.caten.createMeasuredTransfer.component.MeteringBarrelData;
import com.caten.createMeasuredTransfer.item.MeteringBarrelItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS=
            DeferredRegister.createItems(CreateMeasuredTransfer.MOD_ID);

    public static final DeferredItem<Item> metering_barrel =
            ITEMS.registerItem("metering_barrel", MeteringBarrelItem::new,
                    new Item.Properties()
                            .stacksTo(1)
                            .component(ModDataComponents.METERING_BARREL_DATA,MeteringBarrelData.CreateDefault(MeteringBarrelItem.MAX_CAPACITY))
            );

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
