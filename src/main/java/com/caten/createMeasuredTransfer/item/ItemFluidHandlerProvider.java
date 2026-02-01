package com.caten.createMeasuredTransfer.item;

import com.caten.createMeasuredTransfer.ModDataComponents;
import com.caten.createMeasuredTransfer.component.MeteringBarrelData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * 将 MeteringBarrelData 封装为一个可用于 ItemStack 的 IFluidHandler 实现。
 * - 从 ItemStack 的 ModDataComponents 读取初始状态
 * - 在执行修改（EXECUTE）后，调用 saveTo 将变化写回 ModDataComponents
 * 该类尽量保持轻量并复用现有的 MeteringBarrelData API（setFluid / setAmount / keepCapacityEmpty / getCapacity 等）。
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemFluidHandlerProvider implements IFluidHandler {

    private FluidStack fluid;
    private final int capacity;

    public ItemFluidHandlerProvider(FluidStack fluid, int capacity) {
        this.fluid = fluid;
        this.capacity = capacity <= 0 ? MeteringBarrelItem.MaxLiquidVolume : capacity;
    }

    public static ItemFluidHandlerProvider loadFrom(ItemStack stack) {
        MeteringBarrelData data = stack.get(ModDataComponents.METERING_BARREL_DATA);
        if (data != null) {
            return new ItemFluidHandlerProvider(data.getFluidStack(), data.getCapacity());
        }
        // fallback: empty with default capacity
        return new ItemFluidHandlerProvider(FluidStack.EMPTY, MeteringBarrelItem.MaxLiquidVolume);
    }

    public void saveTo(ItemStack stack) {
        MeteringBarrelData old = stack.get(ModDataComponents.METERING_BARREL_DATA);
        if (old == null) {
            // 没有原始组件时无法构造新 MeteringBarrelData（保持原有行为，不写入）
            return;
        }
        MeteringBarrelData updated;
        if (this.fluid.isEmpty()) {
            updated = old.keepCapacityEmpty();
        } else {
            // 假设 FluidStack.getFluid() 返回 net.minecraft.world.level.material.Fluid
            Fluid mcFluid = this.fluid.getFluid();
            updated = old.setFluid(mcFluid, this.fluid.getAmount());
        }
        stack.set(ModDataComponents.METERING_BARREL_DATA, updated);
    }

    // 便利方法
    public FluidStack getFluidStack() {
        return this.fluid;
    }

    public int getAmount() {
        return this.fluid.getAmount();
    }

    public int getCapacity() {
        return this.capacity;
    }

    public boolean isEmpty() {
        return this.fluid.isEmpty();
    }

    // IFluidHandler 单槽实现
    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank != 0) return FluidStack.EMPTY;
        return getFluidStack();
    }

    @Override
    public int getTankCapacity(int tank) {
        if (tank != 0) return 0;
        return this.capacity;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        if (tank != 0) return false;
        if (stack.isEmpty()) return true; // 空流体总是有效（表示可接收任意流体）
        if (isEmpty()) return true; // 空容器可以接收任何流体
        return FluidStack.isSameFluidSameComponents(this.fluid, stack);
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty()) return 0;
        // 如果当前为空或同种流体才允许
        if (!isEmpty() && !FluidStack.isSameFluidSameComponents(this.fluid, resource)) return 0;

        int space = this.capacity - getAmount();
        int toFill = Math.min(space, resource.getAmount());
        if (toFill <= 0) return 0;
        if (action == IFluidHandler.FluidAction.EXECUTE) {
            if (isEmpty()) {
                // 复制一个只包含 toFill 的实例
                this.fluid = new FluidStack(resource.getFluid(), toFill);
            } else {
                this.fluid.setAmount(this.fluid.getAmount() + toFill);
            }
        }
        return toFill;
    }

    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        if (resource.isEmpty() || isEmpty()) return FluidStack.EMPTY;
        if (!FluidStack.isSameFluidSameComponents(this.fluid, resource)) return FluidStack.EMPTY;
        int toDrain = Math.min(resource.getAmount(), this.getAmount());
        if (toDrain <= 0) return FluidStack.EMPTY;
        FluidStack drained = new FluidStack(this.fluid.getFluid(), toDrain);
        if (action == IFluidHandler.FluidAction.EXECUTE) {
            int remain = this.fluid.getAmount() - toDrain;
            if (remain <= 0) {
                this.fluid = FluidStack.EMPTY;
            } else {
                this.fluid.setAmount(remain);
            }
        }
        return drained;
    }

    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        if (isEmpty() || maxDrain <= 0) return FluidStack.EMPTY;
        int toDrain = Math.min(maxDrain, getAmount());
        FluidStack drained = new FluidStack(this.fluid.getFluid(), toDrain);
        if (action == IFluidHandler.FluidAction.EXECUTE) {
            int remain = this.fluid.getAmount() - toDrain;
            if (remain <= 0) {
                this.fluid = FluidStack.EMPTY;
            } else {
                this.fluid.setAmount(remain);
            }
        }
        return drained;
    }
}
