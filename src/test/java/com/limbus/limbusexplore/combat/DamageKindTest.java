package com.limbus.limbusexplore.combat;

import com.limbus.limbusexplore.TestBootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 这三个 tag 路径是对外契约：整合包和其它 mod 靠数据包 json 接进来，
 * 改名前先想清楚（tag 文件和默认值都在 data/limbusexplore/tags/ 下）。
 */
class DamageKindTest {

    @BeforeAll
    static void bootstrap() {
        // weaponTag 走 ItemTags，要注册表建好才能建 TagKey
        TestBootstrap.ensure();
    }

    @Test
    void tagPathsAreStable() {
        for (DamageKind kind : DamageKind.values()) {
            assertEquals("limbusexplore:weapons/" + kind.id, kind.weaponTag().location().toString());
            assertEquals("limbusexplore:attackers/" + kind.id, kind.attackerTag().location().toString());
            assertEquals("limbusexplore:damage/" + kind.id, kind.damageTag().location().toString());
        }
    }

    @Test
    void thereAreExactlyThreeKinds() {
        assertEquals(3, DamageKind.values().length);
        assertEquals(DamageKind.SLASH, DamageKind.values()[0]);
        assertEquals(DamageKind.PIERCE, DamageKind.values()[1]);
        assertEquals(DamageKind.BLUNT, DamageKind.values()[2]);
    }

    @Test
    void lookupIsCaseInsensitive() {
        assertEquals(DamageKind.SLASH, DamageKind.byId("slash"));
        assertEquals(DamageKind.SLASH, DamageKind.byId("SLASH"));
        assertEquals(DamageKind.BLUNT, DamageKind.byId("Blunt"));
        assertNull(DamageKind.byId("magic"));
    }

    @Test
    void displayKeysFollowConvention() {
        assertEquals("damage_kind.limbusexplore.pierce", DamageKind.PIERCE.displayKey());
    }
}
