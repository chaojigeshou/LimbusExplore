package com.limbus.limbusexplore.net;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.chaos.ChaosApi;
import com.limbus.limbusexplore.ego.EgoStateApi;
import com.limbus.limbusexplore.sanity.SanityCapabilities;
import com.limbus.limbusexplore.sin.ModCapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 玩家数据同步的唯一入口（FORGE 总线）：
 * 登录/换维/重生后把罪孽资源、理智值、EGO 状态全量同步一次；
 * 玩家每 tick 驱动 EgoStateApi（到期结束、每秒回调）。
 * 各 Api 修改数据后调这里对应的 syncXxx 方法。
 */
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class PlayerDataSync {

    private PlayerDataSync() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        syncAll(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncAll(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncAll(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        EgoStateApi.tick(serverPlayer);
    }

    private static void syncAll(Player player) {
        syncSin(player);
        syncSanity(player);
        EgoStateApi.sync(player);
        ChaosApi.sync(player);   // Player 本身就是 LivingEntity
    }

    /** 罪孽资源全量同步（SinApi 修改后也调它）。 */
    public static void syncSin(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        player.getCapability(ModCapabilities.SIN_RESOURCES).ifPresent(resources ->
                ModNetworking.sendToPlayer(serverPlayer, new SinSyncPacket(resources.toArray())));
    }

    /** 理智值全量同步（SanityApi 修改后也调它）。 */
    public static void syncSanity(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        player.getCapability(SanityCapabilities.SANITY).ifPresent(sanity ->
                ModNetworking.sendToPlayer(serverPlayer, new SanitySyncPacket(sanity.get())));
    }
}
