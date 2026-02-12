package com.caten.createMeasuredTransfer;

import com.caten.createMeasuredTransfer.screen.meteringBarrel.MeteringBarrelScreen;
import net.neoforged.neoforge.common.NeoForge;

public class ModClientEvents {
    public static void register() {
        NeoForge.EVENT_BUS.addListener(MeteringBarrelScreen::openScreen);
    }
}
