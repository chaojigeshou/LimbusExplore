package com.limbus.limbusexplore.net;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.ego.EgoStateApi;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// EGO 状态的同步与计时钩子：登录/换维/重生全量同步；
// 玩家每 tick 给 EgoStateApi.tick（到期结束、每秒回调）。
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class PlayerEgoStateSync {

    private PlayerEgoStateSync() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        EgoStateApi.sync(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        EgoStateApi.sync(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        EgoStateApi.sync(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        EgoStateApi.tick(serverPlayer);
    }
}
