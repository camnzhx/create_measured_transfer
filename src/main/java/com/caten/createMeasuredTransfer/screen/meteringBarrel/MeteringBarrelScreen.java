package com.caten.createMeasuredTransfer.screen.meteringBarrel;

import com.caten.createMeasuredTransfer.ModDataComponents;
import com.caten.createMeasuredTransfer.component.MeteringBarrelData;
import com.caten.createMeasuredTransfer.event.OpenMeteringBarrelScreenEvent;
import com.caten.createMeasuredTransfer.item.MeteringBarrelItem;
import com.caten.createMeasuredTransfer.packet.MeteringBarrelActionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;
import net.neoforged.neoforge.network.PacketDistributor;

/*
 * "Metering Barrel"的右键菜单实现
 * 添加容量的配置滑块
 * 添加基本按键
 * 显示流体基本信息
 */
public class MeteringBarrelScreen extends Screen {

    private final ItemStack itemStack;
    private final MeteringBarrelData barrelData;
    private final int fluidAmount;
    private final String fluidName;

    private int capacity;

    private EditBox capacityEditBox;
    private CapacitySlider capacitySlider;

    public static void openScreen(OpenMeteringBarrelScreenEvent event) {
        Minecraft.getInstance().setScreen(new MeteringBarrelScreen(event.itemStack));
    }

    public MeteringBarrelScreen(ItemStack itemStack) {
        super(Component.translatable("screen.create_measured_transfer.metering_barrel"));
        this.itemStack = itemStack;
        this.barrelData = itemStack.get(ModDataComponents.METERING_BARREL_DATA);
        if(barrelData == null){
            throw new IllegalStateException("MeteringBarrelData is missing from the ItemStack");
        }
        this.fluidAmount = barrelData.getAmount();
        this.capacity = barrelData.getCapacity();
        this.fluidName =  barrelData.getFluidName();
    }

    @Override
    protected void init() {
        super.init();



        // 添加清空按钮
        this.addRenderableWidget(Button.builder(
                        Component.translatable("button.create_measured_transfer.clear"),
                        button -> {
                            if(itemStack.getItem() instanceof MeteringBarrelItem){
                                PacketDistributor.sendToServer(MeteringBarrelActionPacket.clear());
                            }
                        })
                .pos(50, 60)
                .build());

        // 添加容量滑块
        capacitySlider = new CapacitySlider(
                50,
                20,
                200,
                20,
                Component.empty(),
                Component.literal(" mB"),
                0.0,
                MeteringBarrelItem.MaxLiquidVolume,
                capacity,
                true
        );
        this.addRenderableWidget(capacitySlider);

        // 添加容量编辑框
        capacityEditBox = new EditBox(
                Minecraft.getInstance().font,
                260,
                20,
                30,
                20,
                Component.literal(String.valueOf(capacity))
        );
        capacityEditBox.setFilter(input -> {
            if (input.isEmpty()) return true;
            try {
                int value = Integer.parseInt(input);
                // 限制在0-4000之间（根据你的MaxLiquidVolume）
                return value >= 0 && value <= MeteringBarrelItem.MaxLiquidVolume;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        this.addRenderableWidget(capacityEditBox);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers){
        String strValue = capacityEditBox.getValue();
        int value;
        try{
            value = Integer.parseInt(strValue);
        } catch (NumberFormatException e){
            value = 0;
        }
            capacitySlider.setValue(value);
            capacitySlider.applyValue();

        if(keyCode == 69) {
            super.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private static class CapacitySlider extends ExtendedSlider {

        public CapacitySlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, boolean showDecimal) {
            super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, showDecimal);
        }

        @Override
        protected void applyValue(){
            PacketDistributor.sendToServer(MeteringBarrelActionPacket.setCapacity((int) this.getValue()));
        }
    }
}
