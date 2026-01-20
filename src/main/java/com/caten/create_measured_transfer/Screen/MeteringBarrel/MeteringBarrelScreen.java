package com.caten.create_measured_transfer.Screen.MeteringBarrel;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MeteringBarrelScreen extends Screen {

    public MeteringBarrelScreen(){
        this(Component.translatable("screen.create_measured_transfer.metering_barrel"));
    }

    protected MeteringBarrelScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers){
        if(keyCode == 69) {
            super.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
