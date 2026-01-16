package com.caten.create_measured_transfer.item;

import com.caten.create_measured_transfer.data_component.MeteringBarrelData;
import com.caten.create_measured_transfer.ModDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class MeteringBarrelItem extends Item {

    public static final Logger LOGGER = Logger.getLogger(MeteringBarrelItem.class.getName());

    public static final int MaxLiquidVolume = 4000;
    public static final int A_BUCKET_VOLUME = 1000;

    public MeteringBarrelItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        MeteringBarrelData barrelData = itemStack.get(ModDataComponents.METERING_BARREL_DATA);
        if(barrelData == null){
            LOGGER.warning("MeteringBarrelData is null!");
            return InteractionResultHolder.fail(itemStack);
        }
        BlockHitResult blockhitresult = null;

        //获取玩家视角的方块信息
        blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

        //判断视线朝向结果
        if (blockhitresult.getType() == HitResult.Type.MISS) {          //没有命中任何方块,打开物品界面
            return InteractionResultHolder.pass(itemStack);
        } else if (blockhitresult.getType() != HitResult.Type.BLOCK) {  //命中结果不是方块,不做任何操作
            return InteractionResultHolder.pass(itemStack);
        } else {                                                        //命中结果是方块,进行相应操作


            BlockPos blockpos = blockhitresult.getBlockPos();

            BlockState blockState = level.getBlockState(blockpos);
            FluidState fluidState = blockState.getFluidState();
            BucketPickup bucketpickup = null;
            int c = 0;

            if (barrelData.isEmpty()) {//桶内没有液体，尝试拾取方块内的液体
                if(blockState.getBlock() instanceof BucketPickup) {
                    itemStack.set(ModDataComponents.METERING_BARREL_DATA, barrelData.setFluid(fluidState.getType(), A_BUCKET_VOLUME));
                    pickupFluid(level, player, blockState, blockpos);
                    return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
                }
            } else {
                int fluidAmount = barrelData.getAmount();
                int residualAmount = MaxLiquidVolume - fluidAmount;//计算剩余可装液体量
                if (residualAmount >= A_BUCKET_VOLUME) {
                    if (blockState.getBlock() instanceof BucketPickup) {
                        itemStack.set(ModDataComponents.METERING_BARREL_DATA, barrelData.setAmount(fluidAmount + A_BUCKET_VOLUME));
                        pickupFluid(level, player, blockState, blockpos);
                        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
                    } else if (fluidAmount > A_BUCKET_VOLUME) {
                        itemStack.set(ModDataComponents.METERING_BARREL_DATA, barrelData.setAmount(fluidAmount - A_BUCKET_VOLUME));

                    } else if (fluidAmount == A_BUCKET_VOLUME) {
                        itemStack.set(ModDataComponents.METERING_BARREL_DATA, barrelData.keepCapacityEmpty());
                        c = 2;

                    }
                } else {
                    if (fluidAmount >= A_BUCKET_VOLUME) {
                        itemStack.set(ModDataComponents.METERING_BARREL_DATA, barrelData.setAmount(fluidAmount - A_BUCKET_VOLUME));
                        c = 2;
                    }
                }

            }
            return InteractionResultHolder.pass(itemStack);
        }
    }

    private void pickupFluid(@NotNull Level level, Player player, BlockState blockState, BlockPos blockpos) {
        BucketPickup bucketPickup = (BucketPickup) blockState.getBlock();
        bucketPickup.pickupBlock(player, level, blockpos, blockState);

        player.awardStat(Stats.ITEM_USED.get(this));

        //播放音效和触发游戏事件
        bucketPickup.getPickupSound(blockState).ifPresent(p_150709_ -> player.playSound(p_150709_, 1.0F, 1.0F));
        level.gameEvent(player, GameEvent.FLUID_PICKUP, blockpos);
    }

    private boolean placeFluid(@NotNull Level level, Player player, BlockState blockState, BlockPos blockpos) {

        return false;
    }

}
