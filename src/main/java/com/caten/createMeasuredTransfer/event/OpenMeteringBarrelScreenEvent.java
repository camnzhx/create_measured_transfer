package com.caten.createMeasuredTransfer.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

public class OpenMeteringBarrelScreenEvent extends  Event {
    public ItemStack itemStack;
    public OpenMeteringBarrelScreenEvent(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
}
