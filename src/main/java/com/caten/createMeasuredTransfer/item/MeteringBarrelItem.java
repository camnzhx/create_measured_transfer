package com.caten.createMeasuredTransfer.item;

import com.caten.createMeasuredTransfer.ModDataComponents;
import com.caten.createMeasuredTransfer.component.MeteringBarrelData;
import com.caten.createMeasuredTransfer.event.OpenMeteringBarrelScreenEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.logging.Logger;

public class MeteringBarrelItem extends Item {

    private static final Logger LOGGER = Logger.getLogger(MeteringBarrelItem.class.getName());
    private static final int A_BUCKET_VOLUME = FluidType.BUCKET_VOLUME;

    public static final int MaxLiquidVolume = 4000;

    public MeteringBarrelItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult onItemUseFirst(@NotNull ItemStack itemStack, UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        MeteringBarrelData barrelData = itemStack.get(ModDataComponents.METERING_BARREL_DATA);
        if(barrelData == null){
            LOGGER.warning("MeteringBarrelData is null!");
            return InteractionResult.FAIL;
        }

        IFluidHandler blockHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, blockPos, null);

        if(blockHandler != null) {
            // 使用 ItemFluidHandlerProvider 来表示物品侧的流体能力
            ItemFluidHandlerProvider itemHandler = ItemFluidHandlerProvider.loadFrom(itemStack);
            boolean changed = transferBetween(blockHandler, itemHandler, itemStack);
            return changed ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
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
            if(level.isClientSide() && player.isShiftKeyDown()){
//                Minecraft.getInstance().setScreen(new com.caten.create_measured_transfer.Screen.MeteringBarrel.MeteringBarrelScreen(itemStack));
                NeoForge.EVENT_BUS.post(new OpenMeteringBarrelScreenEvent(itemStack));
            }
            return InteractionResultHolder.pass(itemStack);
        } else if (blockhitresult.getType() != HitResult.Type.BLOCK) {  //命中结果不是方块,不做任何操作
            return InteractionResultHolder.pass(itemStack);
        } else {                                                        //命中结果是方块,进行相应操作
            BlockPos blockPos = blockhitresult.getBlockPos();
            BlockState blockState = level.getBlockState(blockPos);
            Block block = blockState.getBlock();

            // 使用 itemHandler 来代表物品内状态
            ItemFluidHandlerProvider itemHandler = ItemFluidHandlerProvider.loadFrom(itemStack);

            if (block instanceof BucketPickup) {
                // 判断是否可以拾取方块流体
                if (canPickupFluid(blockState, itemHandler)) {
                    Fluid pickedFluid = pickupFluid(level, player, blockPos, blockState);
                    // 把拾取到的流体填入物品（直接 execute）
                    itemHandler.fill(new FluidStack(pickedFluid, A_BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
                    itemHandler.saveTo(itemStack);
                    return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
                }
            }
            if (canPlaceFluid(itemHandler) && placeFluid(level, player, blockPos, blockhitresult, itemHandler)) {
                // placeFluid 已在执行时从 itemHandler 中抽取流体，保存并返回成功
                itemHandler.saveTo(itemStack);
                return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
            }
            return InteractionResultHolder.pass(itemStack);
        }
    }

    // 将方块 handler 与物品 handler 做 simulate -> execute 的一次 transfer（尝试从方块到物品，若不可则尝试反向）
    private boolean transferBetween(IFluidHandler blockHandler, ItemFluidHandlerProvider itemHandler, ItemStack stack) {
        // 先尝试从方块抽取到物品
        FluidStack simulatedFromBlock = blockHandler.drain(A_BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
        if (!simulatedFromBlock.isEmpty()) {
            int accepted = itemHandler.fill(simulatedFromBlock, IFluidHandler.FluidAction.SIMULATE);
            if (accepted > 0) {
                FluidStack drained = blockHandler.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
                itemHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                itemHandler.saveTo(stack);
                return true;
            }
        }
        // 再尝试从物品放到方块
        FluidStack simulatedFromItem = itemHandler.drain(A_BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
        if (!simulatedFromItem.isEmpty()) {
            int willFill = blockHandler.fill(simulatedFromItem, IFluidHandler.FluidAction.SIMULATE);
            if (willFill > 0) {
                FluidStack drained = itemHandler.drain(willFill, IFluidHandler.FluidAction.EXECUTE);
                blockHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
                itemHandler.saveTo(stack);
                return true;
            }
        }
        return false;
    }

    private boolean canPlaceFluid(ItemFluidHandlerProvider itemHandler) {
        int amount = itemHandler.getAmount();
        return amount >= A_BUCKET_VOLUME;
    }

    private boolean canPickupFluid(BlockState blockState, ItemFluidHandlerProvider itemHandler) {
        int amount = itemHandler.getAmount();
        int capacity = itemHandler.getCapacity();
        return !(capacity - amount < A_BUCKET_VOLUME) && !blockState.getFluidState().isEmpty()
                && (itemHandler.isEmpty() || blockState.getFluidState().getFluidType().equals(itemHandler.getFluidStack().getFluid().getFluidType()));
    }

    private Fluid pickupFluid(@NotNull Level level, Player player, BlockPos blockPos, BlockState blockState) {

        BucketPickup bucketPickup = (BucketPickup) blockState.getBlock();
        bucketPickup.pickupBlock(player, level, blockPos, blockState);

        player.awardStat(Stats.ITEM_USED.get(this));

        //播放音效和触发游戏事件
        bucketPickup.getPickupSound(blockState).ifPresent(p_150709_ -> player.playSound(p_150709_, 1.0F, 1.0F));
        level.gameEvent(player, GameEvent.FLUID_PICKUP, blockPos);
        return blockState.getFluidState().getType();
    }

    private boolean placeFluid(@NotNull Level level, Player player, BlockPos blockpos,
                               BlockHitResult blockHitResult, ItemFluidHandlerProvider itemHandler) {

        BlockState blockState = level.getBlockState(blockpos);

        boolean flag;

        if (!(blockState.getBlock() instanceof LiquidBlockContainer) && !blockState.isAir() && blockState.getFluidState().isEmpty()) {
            flag = blockHitResult != null && this.placeFluid(level, player, blockHitResult.getBlockPos().relative(blockHitResult.getDirection()), null, itemHandler);
        } else if (blockState.getBlock() instanceof LiquidBlockContainer liquidBlockContainer) {
            // 从 itemHandler 中抽出合适量的流体并放入方块
            FluidStack toPlace = itemHandler.drain(A_BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
            if (!toPlace.isEmpty()) {
                liquidBlockContainer.placeLiquid(level, blockpos, blockState, toPlace.getFluid().defaultFluidState());
                // 在这里我们已经放置，执行抽取
                itemHandler.drain(A_BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
                flag = true;
            } else {
                flag = false;
            }
        }else{
            // world.setBlock to fluid block
            FluidStack toPlace = itemHandler.drain(A_BUCKET_VOLUME, IFluidHandler.FluidAction.SIMULATE);
            if (!toPlace.isEmpty()) {
                level.setBlock(blockpos, toPlace.getFluid().defaultFluidState().createLegacyBlock(), 11);
                itemHandler.drain(A_BUCKET_VOLUME, IFluidHandler.FluidAction.EXECUTE);
                flag = true;
            } else {
                flag = false;
            }
        }
        if (flag) {
            player.awardStat(Stats.ITEM_USED.get(this));
            playEmptySound(player,level, blockpos, itemHandler);
            level.gameEvent(player, GameEvent.FLUID_PLACE, blockpos);
        }
        return flag;
    }

    protected void playEmptySound(@Nullable Player p_40696_, LevelAccessor p_40697_, BlockPos p_40698_, ItemFluidHandlerProvider itemHandler) {
        FluidStack content = itemHandler.getFluidStack();
        SoundEvent soundevent = content.getFluidType().getSound(p_40696_, p_40697_, p_40698_, net.neoforged.neoforge.common.SoundActions.BUCKET_EMPTY);
        if(soundevent == null) soundevent = content.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        p_40697_.playSound(p_40696_, p_40698_, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
        p_40697_.gameEvent(p_40696_, GameEvent.FLUID_PLACE, p_40698_);
    }

    public static void emptyFluid(ItemStack itemStack){
        MeteringBarrelData barrelData = itemStack.get(ModDataComponents.METERING_BARREL_DATA);
        if(barrelData == null || barrelData.isEmpty()){
            return;
        }
        itemStack.set(ModDataComponents.METERING_BARREL_DATA, barrelData.keepCapacityEmpty());
    }

    public static int getCapacity(ItemStack itemStack){
        MeteringBarrelData barrelData = itemStack.get(ModDataComponents.METERING_BARREL_DATA);
        if (barrelData != null) {
            return barrelData.getCapacity();
        }
        return MaxLiquidVolume;
    }

    public static void setCapacity(ItemStack itemStack, int newCapacity){
        MeteringBarrelData barrelData = itemStack.get(ModDataComponents.METERING_BARREL_DATA);
        if (barrelData != null) {
            itemStack.set(ModDataComponents.METERING_BARREL_DATA, barrelData.setCapacity(newCapacity));
        }
    }
}
