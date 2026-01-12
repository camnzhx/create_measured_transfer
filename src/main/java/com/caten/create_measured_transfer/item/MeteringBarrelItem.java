package com.caten.create_measured_transfer.item;

import com.caten.create_measured_transfer.data_component.MeteringBarrelData;
import com.caten.create_measured_transfer.data_component.ModDataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MeteringBarrelItem extends Item {
    public static final int MaxLiquidVolume = 4000;


    public MeteringBarrelItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use (@NotNull Level level, Player player , @NotNull InteractionHand hand){
        ItemStack itemStack = player.getItemInHand(hand);
        MeteringBarrelData barrelData = itemStack.get(ModDataComponents.METERING_BARREL_DATA);



        return InteractionResultHolder.fail(itemStack);
    }


}
