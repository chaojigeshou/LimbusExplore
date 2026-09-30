package com.limbus.limbusexplore.combat;

/** 可直接测试的结算结果。三系与混乱分别受自己的开关控制，零伤害不产生混乱伤害。 */
public record DamageResolution(float healthDamage, int staggerDamage) {
    public static DamageResolution resolve(float raw, float resistance, float levelMultiplier,
                                           boolean staggered, boolean kindsEnabled, boolean chaosEnabled) {
        if (!Float.isFinite(raw) || raw <= 0) return new DamageResolution(0, 0);
        double amount = raw;
        if (kindsEnabled) amount *= Math.max(0, resistance) * Math.max(0, levelMultiplier);
        if (chaosEnabled && staggered) amount *= 1.5;
        if (!Double.isFinite(amount)) return new DamageResolution(0, 0);
        float damage = (float) Math.min(Float.MAX_VALUE, amount);
        int stagger = chaosEnabled && !staggered && damage > 0
                ? (int) Math.min(Integer.MAX_VALUE, Math.max(1, Math.round((double) damage))) : 0;
        return new DamageResolution(damage, stagger);
    }
}
