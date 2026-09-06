package com.limbus.limbusexplore;

import com.limbus.limbusexplore.config.ModConfig;
import com.limbus.limbusexplore.net.ModNetworking;
import com.limbus.limbusexplore.registry.ModCreativeTabs;
import com.limbus.limbusexplore.registry.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(LimbusExplore.MODID)
public class LimbusExplore {

    public static final String MODID = "limbusexplore";

    private static final Logger LOGGER = LogUtils.getLogger();

    public LimbusExplore() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ModNetworking.register();
        ModItems.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        // 两个 ModConfig 撞名：Forge 的配置注册类和我们自己的 config.ModConfig
        ModLoadingContext.get().registerConfig(
                net.minecraftforge.fml.config.ModConfig.Type.COMMON, ModConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        if (ModConfig.LOG_STARTUP.get()) {
            LOGGER.info("{} loaded", MODID);
        }
    }

    @SubscribeEvent
    public void onServerStarting(final ServerStartingEvent event) {
        LOGGER.info("{} server starting", MODID);
    }
}
