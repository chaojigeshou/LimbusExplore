package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.net.EgoStateSyncPacket;
import com.limbus.limbusexplore.net.ModNetworking;
import com.limbus.limbusexplore.sin.SinApi;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * EGO 状态（30s）的服务端管理器：
 * 计时、三段监听（beforeEnter/whileInState/onEnd，见 EgoStateListener）、
 * 每秒同步剩余时间给客户端、状态结束时负值资源归 0。
 * 释放的入口在 EgoApi（release / releaseCorrosion），成功后调 enter。
 */
public final class EgoStateApi {

    private static final List<EgoStateListener> LISTENERS = new ArrayList<>();

    private EgoStateApi() {
    }

    public static void registerListener(EgoStateListener listener) {
        LISTENERS.add(listener);
    }

    private static EgoState of(Player player) {
        return player.getCapability(EgoStateCapabilities.EGO_STATE).orElse(null);
    }

    /** 服务端才有效，客户端看 ClientEgoState。 */
    public static boolean isInState(Player player) {
        EgoState state = of(player);
        return state != null && state.isActive(System.currentTimeMillis());
    }

    /** 剩余秒数，不在状态返回 0。 */
    public static int remainingSeconds(Player player) {
        EgoState state = of(player);
        return state == null ? 0 : state.remainingSeconds(System.currentTimeMillis());
    }

    /** 释放成功后调用：先触发 beforeEnter，再写入状态。 */
    public static void enter(ServerPlayer player, Ego ego, boolean corroded) {
        EgoState state = of(player);
        if (state == null) {
            return;
        }
        for (EgoStateListener listener : LISTENERS) {
            listener.beforeEnter(player, ego, corroded);
        }
        state.enter(ego, corroded, System.currentTimeMillis());
        sync(player);
    }

    /** 结束状态：负值资源归 0，触发 onEnd。 */
    public static void end(ServerPlayer player) {
        EgoState state = of(player);
        if (state == null) {
            return;
        }
        Ego ego = state.activeEgo();
        boolean corroded = state.isCorroded();
        state.clear();
        SinApi.normalizeNegative(player);
        for (EgoStateListener listener : LISTENERS) {
            listener.onEnd(player, ego, corroded);
        }
        sync(player);
    }

    /** 每 tick 调用一次（PlayerTickEvent 服务端）：到点结束，每秒回调 whileInState + 同步。 */
    public static void tick(ServerPlayer player) {
        EgoState state = of(player);
        if (state == null || !state.isActive(System.currentTimeMillis())) {
            if (state != null && state.activeEgo() != null) {
                end(player);
            }
            return;
        }
        int seconds = state.remainingSeconds(System.currentTimeMillis());
        if (seconds != state.lastNotifiedSecond()) {
            state.setLastNotifiedSecond(seconds);
            for (EgoStateListener listener : LISTENERS) {
                listener.whileInState(player, state.activeEgo(), state.isCorroded(), seconds);
            }
            sync(player);
        }
    }

    /** 全量同步状态给客户端。 */
    public static void sync(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        EgoState state = of(player);
        if (state == null) {
            return;
        }
        boolean active = state.isActive(System.currentTimeMillis());
        ModNetworking.sendToPlayer(serverPlayer, new EgoStateSyncPacket(
                active ? state.remainingSeconds(System.currentTimeMillis()) : 0,
                active && state.activeEgo() != null ? state.activeEgo().id : "",
                active && state.isCorroded()));
    }
}
