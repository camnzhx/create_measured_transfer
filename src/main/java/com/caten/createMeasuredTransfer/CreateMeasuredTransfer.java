package com.caten.createMeasuredTransfer;

import com.caten.createMeasuredTransfer.item.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CreateMeasuredTransfer.MOD_ID)
public class CreateMeasuredTransfer {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "create_measured_transfer";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public CreateMeasuredTransfer(IEventBus modEventBus, ModContainer modContainer) {

        // Register mod items and data components
        ModItems.register(modEventBus);
        ModDataComponents.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (Create_measured_transfer) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        //NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreativeTab);

        // Register mod packet payloads
        modEventBus.addListener(ModPacket::registerPayloads);

//        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
//        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void addCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.metering_barrel);
        }
    }

    @Mod(value = MOD_ID, dist = Dist.CLIENT)
    public static class OnlyClient {
        public OnlyClient() {
            ModClientEvent.register();
        }
    }
}
