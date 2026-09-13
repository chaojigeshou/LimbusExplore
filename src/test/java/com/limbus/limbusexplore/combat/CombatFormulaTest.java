package com.limbus.limbusexplore.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CombatFormulaTest {

    // 等级系统还没做，这项必须恒等于 1.0（一旦接上等级，改这里的同时要改这个测试）
    @Test
    void levelDeltaIsNeutralForNow() {
        assertEquals(1.0f, CombatFormula.levelDeltaMultiplier(null, null));
    }

    @Test
    void perLevelRateIsThreePercent() {
        assertEquals(0.03f, CombatFormula.LEVEL_DELTA_PER_LEVEL);
    }
}
