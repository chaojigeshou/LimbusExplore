package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.sanity.SanityApi;
import com.limbus.limbusexplore.sin.SinApi;
import net.minecraft.server.level.ServerPlayer;

// 释放判定的唯一入口（服务端）。罪名组合 + 理智都够才扣，少一样都不动。
// 侵蚀路径以后加在这里。
public final class EgoApi {

    public enum ReleaseResult {
        RELEASED,     // 都够，扣了
        SIN_LACK,     // 罪孽资源不够
        SANITY_LACK   // 理智不够（扣完会跌破 -45）
    }

    private EgoApi() {
    }

    // 只判断不动手，给 UI 预检用
    public static ReleaseResult check(ServerPlayer player, Ego ego) {
        if (!SinApi.canPay(player, ego.costs)) {
            return ReleaseResult.SIN_LACK;
        }
        if (!SanityApi.canPay(player, ego.sanityCost)) {
            return ReleaseResult.SANITY_LACK;
        }
        return ReleaseResult.RELEASED;
    }

    public static ReleaseResult release(ServerPlayer player, Ego ego) {
        ReleaseResult result = check(player, ego);
        if (result != ReleaseResult.RELEASED) {
            return result;
        }
        SinApi.consumeAll(player, ego.costs);
        SanityApi.consume(player, ego.sanityCost);
        return ReleaseResult.RELEASED;
    }
}
