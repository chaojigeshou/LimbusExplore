package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.chaos.ChaosApi;
import com.limbus.limbusexplore.sin.SinResources;
import com.limbus.limbusexplore.sin.SinCapabilities;
import com.limbus.limbusexplore.sanity.Sanity;
import com.limbus.limbusexplore.sanity.SanityCapabilities;
import com.limbus.limbusexplore.net.PlayerDataSync;
import net.minecraft.server.level.ServerPlayer;

/**
 * 释放判定入口（服务端）。
 * 普通释放：罪孽组合 + 理智都够才扣，成功进入 30s EGO 状态。
 * 侵蚀释放：消耗 ×1.5 向上取整，罪孽允许透支为负，理智不查下限直接扣，也进入 EGO 状态。
 * EGO 状态中或混乱状态中禁止释放。
 */
public final class EgoApi {

    public enum ReleaseResult {
        RELEASED,       // 普通释放，扣了，进入 EGO 状态
        CORRODED,       // 侵蚀释放，资源透支扣负，进入 EGO 状态
        SIN_LACK,       // 罪孽资源不够（普通释放）
        SANITY_LACK,    // 理智不够（普通释放，扣完跌破 -45）
        IN_EGO_STATE,   // 已经处于 EGO 状态中，禁止释放
        IN_CHAOS,       // 混乱状态中，禁止释放
        NOT_EQUIPPED,   // 服务端未装备或未解锁
        UNAVAILABLE    // 死亡、未知定义或能力未挂载；禁止部分扣费
    }

    private EgoApi() {
    }

    // 只判断不动手，给 UI 预检用
    public static ReleaseResult check(ServerPlayer player, Ego ego) {
        return transact(player, ego, false, false);
    }

    public static ReleaseResult release(ServerPlayer player, Ego ego) {
        return transact(player, ego, false, true);
    }

    /** 侵蚀释放：×1.5 向上取整的罪孽组合（可透支）+ 理智不查下限，成功进入 EGO 状态。 */
    public static ReleaseResult releaseCorrosion(ServerPlayer player, Ego ego) {
        return transact(player, ego, true, true);
    }

    private static ReleaseResult transact(ServerPlayer player, Ego ego, boolean corroded, boolean commit) {
        if (commit) EgoStateApi.reconcile(player);
        EgoLoadout loadout = player.getCapability(EgoLoadoutCapabilities.EGO_LOADOUT).orElse(null);
        SinResources sins = player.getCapability(SinCapabilities.SIN_RESOURCES).orElse(null);
        Sanity sanity = player.getCapability(SanityCapabilities.SANITY).orElse(null);
        EgoState state = player.getCapability(EgoStateCapabilities.EGO_STATE).orElse(null);
        long now = System.currentTimeMillis();
        boolean staggered = ChaosApi.isInChaos(player);
        if (!commit) return EgoReleaseTransaction.check(loadout, sins, sanity, state, ego,
                corroded, player.isAlive(), staggered, now);
        ReleaseResult result = EgoReleaseTransaction.execute(loadout, sins, sanity, state, ego,
                corroded, player.isAlive(), staggered, now);
        if (result == ReleaseResult.RELEASED || result == ReleaseResult.CORRODED) {
            EgoStateApi.onStarted(player, ego, corroded);
            PlayerDataSync.syncSin(player);
            PlayerDataSync.syncSanity(player);
            EgoSkills.execute(player, ego, corroded);
        }
        return result;
    }
}
