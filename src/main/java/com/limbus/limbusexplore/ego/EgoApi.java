package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.sanity.SanityApi;
import com.limbus.limbusexplore.sin.SinApi;
import net.minecraft.server.level.ServerPlayer;

/**
 * 释放判定入口（服务端）。
 * 普通释放：罪孽组合 + 理智都够才扣，成功进入 30s EGO 状态。
 * 侵蚀释放：消耗 ×1.5 向上取整，罪孽允许透支为负，理智不查下限直接扣，也进入 EGO 状态。
 * 状态进行中禁止再次释放（两种都不行），返回 IN_EGO_STATE。
 */
public final class EgoApi {

    public enum ReleaseResult {
        RELEASED,       // 普通释放，扣了，进入 EGO 状态
        CORRODED,       // 侵蚀释放，资源透支扣负，进入 EGO 状态
        SIN_LACK,       // 罪孽资源不够（普通释放）
        SANITY_LACK,    // 理智不够（普通释放，扣完跌破 -45）
        IN_EGO_STATE    // 已经处于 EGO 状态中，禁止释放
    }

    private EgoApi() {
    }

    // 只判断不动手，给 UI 预检用
    public static ReleaseResult check(ServerPlayer player, Ego ego) {
        if (EgoStateApi.isInState(player)) {
            return ReleaseResult.IN_EGO_STATE;
        }
        if (!SinApi.canPay(player, ego.costs)) {
            return ReleaseResult.SIN_LACK;
        }
        if (!SanityApi.canPay(player, ego.sanityCost)) {
            return ReleaseResult.SANITY_LACK;
        }
        return ReleaseResult.RELEASED;
    }

    public static ReleaseResult release(ServerPlayer player, Ego ego) {
        if (EgoStateApi.isInState(player)) {
            return ReleaseResult.IN_EGO_STATE;
        }
        ReleaseResult result = check(player, ego);
        if (result != ReleaseResult.RELEASED) {
            return result;
        }
        SinApi.consumeAll(player, ego.costs);
        SanityApi.consume(player, ego.sanityCost);
        EgoStateApi.enter(player, ego, false);
        return ReleaseResult.RELEASED;
    }

    /** 侵蚀释放：×1.5 向上取整的罪孽组合（可透支）+ 理智不查下限，成功进入 EGO 状态。 */
    public static ReleaseResult releaseCorrosion(ServerPlayer player, Ego ego) {
        if (EgoStateApi.isInState(player)) {
            return ReleaseResult.IN_EGO_STATE;
        }
        SinApi.consumeAllForced(player, scaleCosts(ego.costs));
        SanityApi.consumeForce(player, scale(ego.sanityCost));
        EgoStateApi.enter(player, ego, true);
        return ReleaseResult.CORRODED;
    }

    // ×1.5 向上取整
    private static int scale(int amount) {
        return (amount * 3 + 1) / 2;
    }

    private static SinCost[] scaleCosts(SinCost[] costs) {
        SinCost[] scaled = new SinCost[costs.length];
        for (int i = 0; i < costs.length; i++) {
            scaled[i] = new SinCost(costs[i].sin(), scale(costs[i].amount()));
        }
        return scaled;
    }
}
