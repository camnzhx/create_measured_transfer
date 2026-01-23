package com.caten.createMeasuredTransfer.item;


import com.caten.createMeasuredTransfer.CreateMeasuredTransfer;
import com.caten.createMeasuredTransfer.component.MeteringBarrelData;
import com.caten.createMeasuredTransfer.ModDataComponents;
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
                            .component(ModDataComponents.METERING_BARREL_DATA,MeteringBarrelData.CreateDefault(MeteringBarrelItem.MaxLiquidVolume))
            );

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
