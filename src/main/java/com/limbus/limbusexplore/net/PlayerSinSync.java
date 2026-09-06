package com.limbus.limbusexplore.net;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.sin.ModCapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// 罪孽资源的同步都走这里：登录/换维/重生自动发一次全量，
// 别的地方（SinApi 改完、战斗模块）调 sync 就行。
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class PlayerSinSync {

    private PlayerSinSync() {
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
        player.getCapability(ModCapabilities.SIN_RESOURCES).ifPresent(resources ->
                ModNetworking.sendToPlayer(serverPlayer, new SinSyncPacket(resources.toArray())));
    }
}
