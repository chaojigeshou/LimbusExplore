package com.limbus.limbusexplore.net;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.sanity.SanityCapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// 理智值的同步入口，和 PlayerSinSync 同一个模板。
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class PlayerSanitySync {

    private PlayerSanitySync() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        sync(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        sync(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        sync(event.getEntity());
    }

    public static void sync(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        player.getCapability(SanityCapabilities.SANITY).ifPresent(sanity ->
                ModNetworking.sendToPlayer(serverPlayer, new SanitySyncPacket(sanity.get())));
    }
}
