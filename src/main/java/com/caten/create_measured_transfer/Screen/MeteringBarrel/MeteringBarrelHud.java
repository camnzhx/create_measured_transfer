package com.caten.create_measured_transfer.Screen.MeteringBarrel;

import com.caten.create_measured_transfer.ModDataComponents;
import com.caten.create_measured_transfer.data_component.MeteringBarrelData;
import com.caten.create_measured_transfer.item.MeteringBarrelItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import static com.caten.create_measured_transfer.Create_measured_transfer.MODID;

// StaticTextHUD.java - 直接在屏幕上显示文字
@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class MeteringBarrelHud {



    @SubscribeEvent
    public static void contentDisplay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        Player player = mc.player;
        ItemStack itemStack = player.getMainHandItem();
        if(itemStack.getItem() instanceof MeteringBarrelItem){
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
            gui.drawString(font, volume + " mL", 10, height-10, 0x55FF55);
        }


    }


}
