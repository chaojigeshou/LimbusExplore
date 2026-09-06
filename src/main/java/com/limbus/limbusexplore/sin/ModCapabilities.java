package com.limbus.limbusexplore.sin;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// capability 定义 + 挂到玩家身上。网络同步不在这里，去 net/PlayerSinSync。
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class ModCapabilities {

    public static final Capability<SinResources> SIN_RESOURCES =
            CapabilityManager.get(new CapabilityToken<>() {});

    private ModCapabilities() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(LimbusExplore.MODID, "sin_resources"),
                    new SinResourcesProvider());
        }
    }

    // 死亡重生的新实体会克隆一份旧数据，不然换个身体资源就清零了
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getEntity().getCapability(SIN_RESOURCES).ifPresent(resources ->
                event.getOriginal().getCapability(SIN_RESOURCES)
                        .ifPresent(resources::copyFrom));
    }
}
