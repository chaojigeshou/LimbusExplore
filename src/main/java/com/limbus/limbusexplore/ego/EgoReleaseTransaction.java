package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.sanity.Sanity;
import com.limbus.limbusexplore.sin.SinResources;

/** 纯数据事务：先完整校验，再统一扣费和进入状态。失败路径不改变任何数据。 */
final class EgoReleaseTransaction {
    private EgoReleaseTransaction() {}

    static EgoApi.ReleaseResult check(EgoLoadout loadout, SinResources sins, Sanity sanity,
                                      EgoState state, Ego ego, boolean corroded,
                                      boolean alive, boolean staggered, long now) {
        if (!alive || loadout == null || sins == null || sanity == null || state == null || ego == null) {
            return EgoApi.ReleaseResult.UNAVAILABLE;
        }
        if (!loadout.contains(ego)) return EgoApi.ReleaseResult.NOT_EQUIPPED;
        if (staggered) return EgoApi.ReleaseResult.IN_CHAOS;
        if (state.isActive(now)) return EgoApi.ReleaseResult.IN_EGO_STATE;
        if (!corroded && !sins.canPay(ego.costs())) return EgoApi.ReleaseResult.SIN_LACK;
        if (!corroded && !sanity.canConsume(ego.sanityCost)) return EgoApi.ReleaseResult.SANITY_LACK;
        return corroded ? EgoApi.ReleaseResult.CORRODED : EgoApi.ReleaseResult.RELEASED;
    }

    static EgoApi.ReleaseResult execute(EgoLoadout loadout, SinResources sins, Sanity sanity,
                                        EgoState state, Ego ego, boolean corroded,
                                        boolean alive, boolean staggered, long now) {
        EgoApi.ReleaseResult result = check(loadout, sins, sanity, state, ego, corroded, alive, staggered, now);
        if (result != EgoApi.ReleaseResult.RELEASED && result != EgoApi.ReleaseResult.CORRODED) return result;
        SinCost[] costs = ego.costs();
        if (corroded) {
            for (int i = 0; i < costs.length; i++) costs[i] = costs[i].scaledForCorrosion();
            sins.consumeAll(costs, true);
            sanity.consumeForce((ego.sanityCost * 3 + 1) / 2);
        } else {
            sins.consumeAll(costs, false);
            sanity.consume(ego.sanityCost);
        }
        state.enter(ego, corroded, now);
        return result;
    }
}
