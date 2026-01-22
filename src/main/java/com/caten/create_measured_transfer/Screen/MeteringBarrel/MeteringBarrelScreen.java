package com.caten.create_measured_transfer.Screen.MeteringBarrel;

import com.caten.create_measured_transfer.item.MeteringBarrelItem;
import com.caten.create_measured_transfer.packet.MeteringBarrelActionPacket;
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

    public MeteringBarrelScreen(ItemStack itemStack) {
        super(Component.translatable("screen.create_measured_transfer.metering_barrel"));
        this.itemStack = itemStack;
    }

    @Override
    protected void init() {
        super.init();



        this.addRenderableWidget(Button.builder(
                        Component.translatable("button.create_measured_transfer.clear"),
                        button -> {
                            if(itemStack.getItem() instanceof MeteringBarrelItem){
                                PacketDistributor.sendToServer(MeteringBarrelActionPacket.clear());
                            }
                        })
                .pos(50, 60)
                .build());

        this.addRenderableWidget(new CapacitySlider(
                50,
                20,
                200,
                20,
                Component.empty(),
                Component.literal(" mB"),
                0.0,
                MeteringBarrelItem.MaxLiquidVolume,
                MeteringBarrelItem.getCapacity(itemStack),
                true
        ));

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers){
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
