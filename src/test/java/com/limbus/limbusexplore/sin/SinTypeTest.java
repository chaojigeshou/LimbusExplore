package com.limbus.limbusexplore.sin;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SinTypeTest {

    // 枚举顺序就是同步包数组下标，重排等于破坏协议（见 README 约定第 6 条）
    @Test
    void thereAreSevenSinsInFixedOrder() {
        assertEquals(7, SinType.values().length);
        assertEquals(SinType.WRATH, SinType.values()[0]);
        assertEquals(SinType.LUST, SinType.values()[1]);
        assertEquals(SinType.SLOTH, SinType.values()[2]);
        assertEquals(SinType.GLUTTONY, SinType.values()[3]);
        assertEquals(SinType.GLOOM, SinType.values()[4]);
        assertEquals(SinType.PRIDE, SinType.values()[5]);
        assertEquals(SinType.ENVY, SinType.values()[6]);
    }

    @Test
    void idsAreUniqueAndResolvable() {
        Set<String> ids = new HashSet<>();
        for (SinType type : SinType.values()) {
            assertTrue(ids.add(type.id), type.id + " 重复");
            assertEquals(type, SinType.byId(type.id));
        }
        assertNull(SinType.byId("kindness"));
    }

    @Test
    void textureAndLangKeysFollowConvention() {
        for (SinType type : SinType.values()) {
            assertEquals("limbusexplore:textures/hud/sin_" + type.id + ".png", type.texture().toString());
            assertEquals("sin.limbusexplore." + type.id, type.displayKey());
        }
    }

    @Test
    void colorsAreOpaque() {
        for (SinType type : SinType.values()) {
            assertEquals(0xFF, (type.color >> 24) & 0xFF, type.id + " 的颜色不是不透明的");
        }
    }
}
