package com.limbus.limbusexplore.sin;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// capability 定义 + 挂到玩家身上。网络同步不在这里，去 net/PlayerDataSync。
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class SinCapabilities {

    public static final Capability<SinResources> SIN_RESOURCES =
            CapabilityManager.get(new CapabilityToken<>() {});

    private SinCapabilities() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(LimbusExplore.MODID, "sin_resources"),
                    new SinResourcesProvider());
        }
    }

}
