package com.caten.createMeasuredTransfer.screen.meteringBarrel;

import com.caten.createMeasuredTransfer.ModDataComponents;
import com.caten.createMeasuredTransfer.component.MeteringBarrelData;
import com.caten.createMeasuredTransfer.event.OpenMeteringBarrelScreenEvent;
import com.caten.createMeasuredTransfer.item.MeteringBarrelItem;
import com.caten.createMeasuredTransfer.packet.MeteringBarrelActionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

/*
 * "Metering Barrel"的右键菜单实现
 * 添加容量的配置滑块
 * 添加基本按键
 * 显示流体基本信息
 */
public class MeteringBarrelScreen extends Screen {

    private final ItemStack itemStack;
    private MeteringBarrelData barrelData;

    private final int capacity;

    private EditBox capacityEditBox;
    private CapacitySlider capacitySlider;
    private boolean isUpdating = false;

    public static void openScreen(OpenMeteringBarrelScreenEvent event) {
        Minecraft.getInstance().setScreen(new MeteringBarrelScreen(event.itemStack));
    }

    public MeteringBarrelScreen(ItemStack itemStack) {
        super(Component.translatable("item.create_measured_transfer.metering_barrel"));
        this.itemStack = itemStack;
        this.barrelData = itemStack.get(ModDataComponents.METERING_BARREL_DATA);
        if(barrelData == null){
            throw new IllegalStateException("MeteringBarrelData is missing from the ItemStack");
        }
        this.capacity = barrelData.getCapacity();
    }

    @Override
    protected void init() {
        super.init();

        // 先创建编辑框
        createCapacityEditBox();
        this.addRenderableWidget(capacityEditBox);

        // 添加容量滑块（需要编辑框已创建）
        capacitySlider = new CapacitySlider(
                50,
                20,
                200,
                20,
                Component.empty(),
                Component.literal(" mB"),
                0.0,
                MeteringBarrelItem.MAX_CAPACITY,
                capacity,
                true
        );
        this.addRenderableWidget(capacitySlider);

        // 添加清空按钮
        this.addRenderableWidget(Button.builder(
                        Component.translatable("button.create_measured_transfer.clear"),
                        button -> {
                            if(itemStack.getItem() instanceof MeteringBarrelItem){
                                barrelData = barrelData.keepCapacityEmpty();
                                PacketDistributor.sendToServer(MeteringBarrelActionPacket.clear());
                            }
                        })
                .pos(50, 60)
                .build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderInfo(guiGraphics);
    }

    private void renderInfo(GuiGraphics guiGraphics) {

        Component fluidComponent = Component.literal(barrelData.getFluidName() + " : " + barrelData.getAmount() + " mB");

        guiGraphics.drawString(Minecraft.getInstance().font, fluidComponent, 50, 100, 0x33FFFF);
    }

    private void createCapacityEditBox() {
        capacityEditBox = new EditBox(
                Minecraft.getInstance().font,
                260,
                20,
                40,
                20,
                Component.literal(String.valueOf(capacity))
        );
        capacityEditBox.setValue(String.valueOf(capacity));
        capacityEditBox.setFilter(input -> {
            if (input.isEmpty()) return true;
            try {
                int value = Integer.parseInt(input);
                // 限制在0-4000之间（根据你的MaxLiquidVolume）
                return value >= 0 && value <= MeteringBarrelItem.MAX_CAPACITY;
            } catch (NumberFormatException e) {
                return false;
            }
        });
        
        // 编辑框值变化时同步到滑块（防递归）
        capacityEditBox.setResponder(text -> {
            if (isUpdating) return; // 防止递归
            
            isUpdating = true;
            try {
                if (text.isEmpty()) {
                    // 如果编辑框为空，设置为0
                    capacitySlider.setValue(0);
                    capacitySlider.applyValue();
                } else {
                    try {
                        int value = Integer.parseInt(text);
                        capacitySlider.setValue(value);
                        capacitySlider.applyValue();
                    } catch (NumberFormatException e) {
                        // 忽略无效输入
                    }
                }
            } finally {
                isUpdating = false;
            }
        });
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers){
        // E键关闭界面
        if(keyCode == 69) {
            super.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private class CapacitySlider extends ExtendedSlider {
        private final EditBox linkedEditBox;

        public CapacitySlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, boolean showDecimal) {
            super(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, showDecimal);
            this.linkedEditBox = capacityEditBox;
        }

        @Override
        protected void applyValue(){
            int value = (int) this.getValue();
            
            // 更新编辑框显示
            if (linkedEditBox != null && !MeteringBarrelScreen.this.isUpdating) {
                MeteringBarrelScreen.this.isUpdating = true;
                try {
                    linkedEditBox.setValue(String.valueOf(value));
                } finally {
                    MeteringBarrelScreen.this.isUpdating = false;
                }
            }
            
            // 发送数据包
            PacketDistributor.sendToServer(MeteringBarrelActionPacket.setCapacity(value));
        }
        
        @Override
        protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
            if(Screen.hasShiftDown()){
                super.stepSize = 100;
            }else{
                super.stepSize = 1;
            }
            super.onDrag(mouseX, mouseY, dragX, dragY);

            // 拖动时实时更新编辑框
            if (linkedEditBox != null && !MeteringBarrelScreen.this.isUpdating) {
                MeteringBarrelScreen.this.isUpdating = true;
                try {
                    linkedEditBox.setValue(String.valueOf((int) this.getValue()));
                } finally {
                    MeteringBarrelScreen.this.isUpdating = false;
                }
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            if(Screen.hasShiftDown()){
                super.stepSize = 100;
            }else{
                super.stepSize = 1;
            }
            super.onClick(mouseX, mouseY);
        }
    }
}
