package com.caten.createMeasuredTransfer.item;

import com.caten.createMeasuredTransfer.ModDataComponents;
import com.caten.createMeasuredTransfer.component.MeteringBarrelData;
import com.caten.createMeasuredTransfer.event.OpenMeteringBarrelScreenEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.material.LavaFluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.SoundAction;
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

        for(Direction direction : Direction.values()) {
            IFluidHandler capability = level.getCapability(Capabilities.FluidHandler.BLOCK, blockPos, direction);

            if (capability != null) {
                FluidStack fluidStack = fluidHandler(capability, barrelData, context);
                if(fluidStack != barrelData.getFluidStack()){
                    itemStack.set(ModDataComponents.METERING_BARREL_DATA, barrelData.copyWithFluidStack(fluidStack));
                    return InteractionResult.sidedSuccess(level.isClientSide());
                }
            }
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

            int fluidAmount = barrelData.getAmount();

            if (block instanceof BucketPickup) {
                if (canPickupFluid(blockState, barrelData)) {
                    Fluid pickedFluid = pickupFluid(level, player, blockPos, blockState);
                    itemStack.set(ModDataComponents.METERING_BARREL_DATA, barrelData.setFluid(pickedFluid,fluidAmount + A_BUCKET_VOLUME));
                    return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
                }
            }
            if (canPlaceFluid(barrelData) && placeFluid(level, player, blockPos, blockhitresult, barrelData)) {
                itemStack.set(ModDataComponents.METERING_BARREL_DATA, barrelData.setAmount(fluidAmount - A_BUCKET_VOLUME));
                return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
            }
            return InteractionResultHolder.pass(itemStack);
        }
    }

    private FluidStack fluidHandler(IFluidHandler capability,MeteringBarrelData barrelData,UseOnContext context) {

        Fluid fluid = barrelData.getFluid();
        int fluidAmount = barrelData.getAmount();
        int fluidCapacity = barrelData.getCapacity();

        //如果桶内流体少于设置容量，尝试从方块中抽取流体
        if(fluidAmount < fluidCapacity){
            if(barrelData.isEmpty()) {
                playFillSound(context.getPlayer(),context.getLevel(), context.getClickedPos(),barrelData);
                return capability.drain(fluidCapacity, IFluidHandler.FluidAction.EXECUTE);
            }
            FluidStack defaultFluid = new FluidStack(fluid, fluidCapacity - fluidAmount);
            FluidStack drainedFluid = capability.drain(defaultFluid, IFluidHandler.FluidAction.SIMULATE);
            if(!drainedFluid.isEmpty()){
                drainedFluid = capability.drain(defaultFluid, IFluidHandler.FluidAction.EXECUTE);
                playFillSound(context.getPlayer(),context.getLevel(), context.getClickedPos(),barrelData);
                return barrelData.setAmount(fluidAmount + drainedFluid.getAmount()).getFluidStack();
            }
        }

        //如果桶内存在流体，尝试向方块中灌入设置的流体量
        if(fluidAmount > 0){
            FluidStack defaultFluid = new FluidStack(fluid, Math.min(fluidAmount, fluidCapacity));
            int filledAmount = capability.fill(defaultFluid, IFluidHandler.FluidAction.SIMULATE);
            if(filledAmount > 0) {
                filledAmount = capability.fill(defaultFluid, IFluidHandler.FluidAction.EXECUTE);
                playDrainSound(context.getPlayer(),context.getLevel(), context.getClickedPos(), barrelData);
                return barrelData.setAmount(fluidAmount - filledAmount).getFluidStack();
            }
        }
        return barrelData.getFluidStack();
    }

    private boolean canPlaceFluid(MeteringBarrelData barrelData) {
        int capacity = barrelData.getCapacity();
        int amount = barrelData.getAmount();
        return capacity < amount ? capacity >= A_BUCKET_VOLUME : amount >= A_BUCKET_VOLUME;
    }

    private boolean canPickupFluid(BlockState blockState, MeteringBarrelData barrelData) {
        int amount = barrelData.getAmount();
        int capacity = barrelData.getCapacity();
        if(capacity - amount < A_BUCKET_VOLUME){
            return false;
        }
        if(blockState.getFluidState().isEmpty()){
            return false;
        }
        return blockState.getFluidState().getFluidType().equals(barrelData.getFluid().getFluidType()) || barrelData.isEmpty();
    }

    private Fluid pickupFluid(@NotNull Level level, Player player, BlockPos blockPos, BlockState blockState) {

        BucketPickup bucketPickup = (BucketPickup) blockState.getBlock();
        bucketPickup.pickupBlock(player, level, blockPos, blockState);

        player.awardStat(Stats.ITEM_USED.get(this));
        bucketPickup.getPickupSound(blockState).ifPresent(p_150709_ -> player.playSound(p_150709_, 1.0F, 1.0F));
        level.gameEvent(player, GameEvent.FLUID_PICKUP, blockPos);
        return blockState.getFluidState().getType();
    }

    private boolean placeFluid(@NotNull Level level, Player player, BlockPos blockpos,
                               BlockHitResult blockHitResult, MeteringBarrelData barrelData) {

        BlockState blockState = level.getBlockState(blockpos);

        boolean flag;

        if (!(blockState.getBlock() instanceof LiquidBlockContainer) && !blockState.isAir() && blockState.getFluidState().isEmpty()) {
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
            playDrainSound(player,level, blockpos, barrelData);
            level.gameEvent(player, GameEvent.FLUID_PLACE, blockpos);
        }
        return flag;
    }

    private static void playFillSound(Player player, Level level, BlockPos blockPos, MeteringBarrelData barrelData) {
        Fluid fluid = barrelData.getFluid();
        SoundEvent sound = fluid.getFluidType().getSound(SoundAction.get("net.neoforged.neoforge.common.SoundActions.BUCKET_FILL"));
        if(sound == null) sound = fluid instanceof LavaFluid ? SoundEvents.BUCKET_FILL_LAVA : SoundEvents.BUCKET_FILL;
        level.playSound(player, blockPos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);

    }

    private static void playDrainSound(@Nullable Player p_40696_, LevelAccessor p_40697_, BlockPos p_40698_, MeteringBarrelData barrelData) {
        FluidStack content = barrelData.getFluidStack();
        SoundEvent soundevent = content.getFluidType().getSound(p_40696_, p_40697_, p_40698_, net.neoforged.neoforge.common.SoundActions.BUCKET_EMPTY);
        if(soundevent == null) soundevent = content.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        p_40697_.playSound(p_40696_, p_40698_, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
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
