package com.limbus.limbusexplore.sin;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SinResourcesTest {

    @Test
    void startsAtZero() {
        SinResources resources = new SinResources();
        for (SinType type : SinType.values()) {
            assertEquals(0, resources.get(type));
        }
    }

    @Test
    void addStopsAtZero() {
        SinResources resources = new SinResources();
        resources.add(SinType.WRATH, 3);
        resources.add(SinType.WRATH, -10);
        assertEquals(0, resources.get(SinType.WRATH));
    }

    // 侵蚀释放允许透支，只有 forceAdd 能扣成负数
    @Test
    void forceAddAllowsOverdraft() {
        SinResources resources = new SinResources();
        resources.add(SinType.GLOOM, 2);
        resources.forceAdd(SinType.GLOOM, -5);
        assertEquals(-3, resources.get(SinType.GLOOM));
    }

    @Test
    void setNeverGoesBelowZero() {
        SinResources resources = new SinResources();
        resources.set(SinType.PRIDE, -8);
        assertEquals(0, resources.get(SinType.PRIDE));
    }

    @Test
    void arrayOfWrongLengthIsIgnored() {
        SinResources resources = new SinResources();
        resources.set(SinType.ENVY, 5);
        resources.fromArray(new int[]{1, 2, 3});
        assertEquals(5, resources.get(SinType.ENVY));
    }

    // 同步包按数组传，顺序必须跟枚举一致，重排枚举就是破坏协议
    @Test
    void arrayOrderFollowsEnumOrder() {
        SinResources resources = new SinResources();
        resources.set(SinType.WRATH, 1);
        resources.set(SinType.ENVY, 7);

        int[] expected = new int[SinType.values().length];
        expected[SinType.WRATH.ordinal()] = 1;
        expected[SinType.ENVY.ordinal()] = 7;
        assertArrayEquals(expected, resources.toArray());

        SinResources loaded = new SinResources();
        loaded.load(resources.save());
        assertArrayEquals(expected, loaded.toArray());
    }

    @Test
    void saveRoundTripKeepsNegativeValues() {
        SinResources resources = new SinResources();
        resources.forceAdd(SinType.LUST, -4);

        SinResources loaded = new SinResources();
        loaded.load(resources.save());
        assertEquals(-4, loaded.get(SinType.LUST));

        loaded.load(new CompoundTag());
        assertEquals(0, loaded.get(SinType.LUST));
    }

    @Test
    void clearResetsEverySin() {
        SinResources resources = new SinResources();
        resources.set(SinType.SLOTH, 9);
        resources.clear();
        assertEquals(0, resources.get(SinType.SLOTH));
    }
}
