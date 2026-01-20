package com.caten.create_measured_transfer.item;

import com.caten.create_measured_transfer.ModDataComponents;
import com.caten.create_measured_transfer.Screen.MeteringBarrel.MeteringBarrelScreen;
import com.caten.create_measured_transfer.data_component.MeteringBarrelData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.logging.Logger;

public class MeteringBarrelItem extends Item {

    private static final Logger LOGGER = Logger.getLogger(MeteringBarrelItem.class.getName());
    private static final int A_BUCKET_VOLUME = FluidType.BUCKET_VOLUME;

    protected static final int MaxLiquidVolume = 4000;

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

        //获取玩家视角的方块信息
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);

        //判断视线朝向结果
        if (blockhitresult.getType() == HitResult.Type.MISS) {          //没有命中任何方块,打开物品界面
            if(!( player instanceof ServerPlayer)&& player.isShiftKeyDown()){
                Minecraft.getInstance().setScreen(new MeteringBarrelScreen());
            }
            return InteractionResultHolder.pass(itemStack);
        } else if (blockhitresult.getType() != HitResult.Type.BLOCK) {  //命中结果不是方块,不做任何操作
            return InteractionResultHolder.pass(itemStack);
        } else {                                                        //命中结果是方块,进行相应操作


            BlockPos blockpos = blockhitresult.getBlockPos();

            BlockState blockState = level.getBlockState(blockpos);
            FluidState fluidState = blockState.getFluidState();
            
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
                        placeFluid(level, player, blockpos, blockhitresult, barrelData);
                        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
                    } else if (fluidAmount == A_BUCKET_VOLUME) {
                        itemStack.set(ModDataComponents.METERING_BARREL_DATA, barrelData.keepCapacityEmpty());
                        placeFluid(level, player, blockpos, blockhitresult, barrelData);
                        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
                    }
                } else {
                    if (fluidAmount >= A_BUCKET_VOLUME) {
                        itemStack.set(ModDataComponents.METERING_BARREL_DATA, barrelData.setAmount(fluidAmount - A_BUCKET_VOLUME));
                        placeFluid(level, player, blockpos, blockhitresult, barrelData);
                        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
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

    private boolean placeFluid(@NotNull Level level, Player player, BlockPos blockpos,
                               BlockHitResult blockHitResult, MeteringBarrelData barrelData) {

        BlockState blockState = level.getBlockState(blockpos);

        boolean flag = false;

        if (!(blockState.getBlock() instanceof LiquidBlockContainer) && !blockState.isAir()) {
            flag = blockHitResult != null && this.placeFluid(level, player, blockHitResult.getBlockPos().relative(blockHitResult.getDirection()), null, barrelData);
        } else if (blockState.getBlock() instanceof LiquidBlockContainer liquidBlockContainer) {
            liquidBlockContainer.placeLiquid(level, blockpos, blockState, barrelData.getFluidState());
            flag = true;
        }else{
            level.setBlock(blockpos, barrelData.getFluidState().createLegacyBlock(), 11);
            flag = true;
        }
        if (flag) {
            player.awardStat(Stats.ITEM_USED.get(this));
            playEmptySound(player,level, blockpos, barrelData);
            level.gameEvent(player, GameEvent.FLUID_PLACE, blockpos);
        }
        return flag;
    }

    protected void playEmptySound(@Nullable Player p_40696_, LevelAccessor p_40697_, BlockPos p_40698_, MeteringBarrelData barrelData) {
        FluidStack content = barrelData.getFluidStack();
        SoundEvent soundevent = content.getFluidType().getSound(p_40696_, p_40697_, p_40698_, net.neoforged.neoforge.common.SoundActions.BUCKET_EMPTY);
        if(soundevent == null) soundevent = content.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        p_40697_.playSound(p_40696_, p_40698_, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
        p_40697_.gameEvent(p_40696_, GameEvent.FLUID_PLACE, p_40698_);
    }
}
