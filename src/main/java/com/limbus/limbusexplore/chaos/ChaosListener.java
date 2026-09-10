package com.limbus.limbusexplore.chaos;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * 混乱状态的三段钩子（玩家和怪物共用）。战斗/表现效果接这里，
 * 只覆写关心的方法即可；当前都是空实现。
 */
public interface ChaosListener {

    /** 进入混乱之前。 */
    default void beforeEnter(LivingEntity entity, Chaos chaos) {
    }

    /** 混乱中，每秒回调，remainingSeconds 从 15 往下走。 */
    default void whileInChaos(LivingEntity entity, Chaos chaos, int remainingSeconds) {
    }

    /** 混乱结束（时间到或被手动解除）。 */
    default void onEnd(LivingEntity entity, Chaos chaos) {
    }

    /** 玩家专用的便捷判断（大部分表现只关心玩家）。 */
    static boolean isPlayer(LivingEntity entity) {
        return entity instanceof ServerPlayer;
    }
}
