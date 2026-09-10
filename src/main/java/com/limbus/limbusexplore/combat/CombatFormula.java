package com.limbus.limbusexplore.combat;

import net.minecraft.world.entity.LivingEntity;

/**
 * 伤害公式骨架。现在只有抗性倍率 + 保留的等级差修正。
 * 等级系统落地后把 levelDeltaMultiplier 改成 (1 + 0.03 × (攻击等级 - 防御等级))，
 * 调用方（CombatHandler）不用动。
 */
public final class CombatFormula {

    /** 等级差系数：每差 1 级约 ±3%（巴士的算法），暂时没有等级系统，返回 1.0 */
    public static final float LEVEL_DELTA_PER_LEVEL = 0.03f;

    private CombatFormula() {
    }

    public static float levelDeltaMultiplier(LivingEntity attacker, LivingEntity target) {
        return 1.0f;
    }

    /** 最终伤害 = 原伤害 × 抗性 × 等级差 */
    public static float finalDamage(LivingEntity attacker, LivingEntity target, DamageKind kind, float rawDamage) {
        return rawDamage
                * ResistanceApi.multiplier(target, kind)
                * levelDeltaMultiplier(attacker, target);
    }
}
