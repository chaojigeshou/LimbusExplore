package com.limbus.limbusexplore.sanity;

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

// 理智值的 capability 定义和挂载，跟罪孽资源（sin/ModCapabilities）是两份独立数据。
// 同步在 net/PlayerSanitySync。
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class SanityCapabilities {

    public static final Capability<Sanity> SANITY =
            CapabilityManager.get(new CapabilityToken<>() {});

    private SanityCapabilities() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(LimbusExplore.MODID, "sanity"),
                    new SanityProvider());
        }
    }

    // 同上：死亡重生死，理智跟着旧身体走
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getEntity().getCapability(SANITY).ifPresent(sanity ->
                event.getOriginal().getCapability(SANITY).ifPresent(sanity::copyFrom));
    }
}
