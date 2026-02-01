package com.caten.createMeasuredTransfer.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Adapter that exposes an ItemFluidHandlerProvider as an IFluidHandlerItem for capability use.
 * This class is deliberately lightweight and delegates SIMULATE/EXECUTE behavior to the provider.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MeteringBarrelItemCapability implements IFluidHandlerItem {
    private final ItemStack stack;
    private final ItemFluidHandlerProvider provider;

    public MeteringBarrelItemCapability(ItemStack stack) {
        this.stack = stack;
        this.provider = ItemFluidHandlerProvider.loadFrom(stack);
    }

    @Override
    public ItemStack getContainer() {
        return stack;
    }

    @Override
    public int getTanks() {
        return provider.getTanks();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return provider.getFluidInTank(tank);
    }

    @Override
    public int getTankCapacity(int tank) {
        return provider.getTankCapacity(tank);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return provider.isFluidValid(tank, stack);
    }

    @Override
    public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
        int filled = provider.fill(resource, action);
        if (action == IFluidHandler.FluidAction.EXECUTE && filled > 0) {
            // persist changes back to the ItemStack
            provider.saveTo(stack);
        }
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
        FluidStack drained = provider.drain(resource, action);
        if (action == IFluidHandler.FluidAction.EXECUTE && !drained.isEmpty()) {
            provider.saveTo(stack);
        }
        return drained;
    }

    @Override
    public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
        FluidStack drained = provider.drain(maxDrain, action);
        if (action == IFluidHandler.FluidAction.EXECUTE && !drained.isEmpty()) {
            provider.saveTo(stack);
        }
        return drained;
    }
}