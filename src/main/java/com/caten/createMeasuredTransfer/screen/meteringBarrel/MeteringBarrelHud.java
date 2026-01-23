package com.caten.createMeasuredTransfer.screen.meteringBarrel;

import com.caten.createMeasuredTransfer.ModDataComponents;
import com.caten.createMeasuredTransfer.component.MeteringBarrelData;
import com.caten.createMeasuredTransfer.item.MeteringBarrelItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import static com.caten.createMeasuredTransfer.CreateMeasuredTransfer.MOD_ID;

// StaticTextHUD.java - 直接在屏幕上显示文字
@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class MeteringBarrelHud {



    @SubscribeEvent
    public static void contentDisplay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        Player player = mc.player;

        ItemStack itemStack = null;
        if(player.getMainHandItem().getItem() instanceof MeteringBarrelItem){
            itemStack = player.getMainHandItem();
        } else if (player.getOffhandItem().getItem() instanceof MeteringBarrelItem) {
            itemStack = player.getOffhandItem();
        }
        if(itemStack != null){
            GuiGraphics gui = event.getGuiGraphics();
            Font font = mc.font;

            MeteringBarrelData barrelData = itemStack.get(ModDataComponents.METERING_BARREL_DATA);
            if(barrelData == null){
                return;
            }

            String fluidName = barrelData.getFluidName();
            int volume = barrelData.getAmount();

            int height = gui.guiHeight();

            gui.drawString(font, fluidName, 10, height-25, 0xFF8C00);
            gui.drawString(font, volume + " mB", 10, height-10, 0x55FF55);
        }


    }


}
