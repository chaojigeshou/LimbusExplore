package com.limbus.limbusexplore.ego;

import net.minecraft.server.level.ServerPlayer;

/**
 * EGO 状态的三段钩子。战斗/表现效果想接进去就实现这个接口，
 * 然后 EgoStateApi.registerListener 挂上；只覆写需要的方法。
 * 当前都是空实现，效果等玩法定了再填。
 */
public interface EgoStateListener {

    /** 进入状态之前（还没写入状态数据）。 */
    default void beforeEnter(ServerPlayer player, Ego ego, boolean corroded) {
    }

    /** 状态中，每秒回调一次，remainingSeconds 从 30 往下走。 */
    default void whileInState(ServerPlayer player, Ego ego, boolean corroded, int remainingSeconds) {
    }

    /** 状态结束（30 秒到或被手动清理）。 */
    default void onEnd(ServerPlayer player, Ego ego, boolean corroded) {
    }
}
