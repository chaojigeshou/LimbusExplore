package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.combat.Resistance;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EgoTest {

    // 装备界面按槽位 = RiskLevel.ordinal() 摆卡片，每个等级必须正好有一条
    @Test
    void everyRiskLevelHasExactlyOneEgo() {
        assertEquals(RiskLevel.values().length, Ego.values().length);
        Set<RiskLevel> used = new HashSet<>();
        for (Ego ego : Ego.values()) {
            assertTrue(used.add(ego.level), ego.id + " 的等级重复了");
            assertEquals(ego.level, RiskLevel.values()[ego.level.ordinal()]);
        }
        for (RiskLevel level : RiskLevel.values()) {
            assertTrue(used.contains(level), level + " 没有对应的 EGO");
        }
    }

    @Test
    void idsAreUniqueAndResolvable() {
        Set<String> ids = new HashSet<>();
        for (Ego ego : Ego.values()) {
            assertTrue(ids.add(ego.id), ego.id + " 重复");
            assertEquals(ego, Ego.byId(ego.id));
        }
        assertNull(Ego.byId("not_an_ego"));
    }

    // 状态期间会拿这个覆盖对应那一系抗性，倍率必须是合法档位
    @Test
    void resistanceOverrideIsUsable() {
        for (Ego ego : Ego.values()) {
            assertNotNull(ego.resistanceKind, ego.id + " 没写抗性系");
            assertTrue(ego.resistanceRate >= Resistance.IMMUNE, ego.id + " 倍率是负的");
            assertTrue(ego.resistanceRate <= Resistance.FATAL, ego.id + " 倍率超出档位范围");
        }
    }

    @Test
    void pathsFollowConvention() {
        for (Ego ego : Ego.values()) {
            assertEquals("limbusexplore:textures/ego/" + ego.id + ".png", ego.texture().toString());
            assertEquals("ego.limbusexplore." + ego.id, ego.displayKey());
            assertEquals("ego.limbusexplore." + ego.id + ".desc", ego.descKey());
            assertEquals("ego.limbusexplore." + ego.id + ".awakening", ego.awakeningKey());
            assertEquals("ego.limbusexplore." + ego.id + ".corrosion", ego.corrosionKey());
            assertEquals("ego.limbusexplore." + ego.id + ".passive", ego.passiveKey());
        }
    }

    @Test
    void everyEgoHasCostsAndPositiveSanityCost() {
        for (Ego ego : Ego.values()) {
            assertTrue(ego.costs.length > 0, ego.id + " 没有消耗");
            assertTrue(ego.sanityCost > 0, ego.id + " 理智消耗不是正的");
            assertTrue(ego.noiseR >= 0f && ego.noiseG >= 0f && ego.noiseB >= 0f);
        }
    }
}
