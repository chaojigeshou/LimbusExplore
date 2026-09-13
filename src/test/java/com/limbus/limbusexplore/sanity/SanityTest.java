package com.limbus.limbusexplore.sanity;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SanityTest {

    @Test
    void startsAtZero() {
        assertEquals(0, new Sanity().get());
    }

    @Test
    void clampsToRange() {
        Sanity sanity = new Sanity();
        sanity.set(999);
        assertEquals(Sanity.MAX, sanity.get());
        sanity.set(-999);
        assertEquals(Sanity.MIN, sanity.get());
    }

    @Test
    void addWorksBothWays() {
        Sanity sanity = new Sanity();
        sanity.add(20);
        assertEquals(20, sanity.get());
        sanity.add(-5);
        assertEquals(15, sanity.get());
    }

    // 正常消耗不许跌破 -45（-45 是崩溃线，另外处理）
    @Test
    void consumeRefusesToBreakTheFloor() {
        Sanity sanity = new Sanity();
        assertFalse(sanity.canConsume(46));
        assertFalse(sanity.consume(46));
        assertEquals(0, sanity.get());
    }

    @Test
    void consumeTakesExactlyTheAmount() {
        Sanity sanity = new Sanity();
        assertTrue(sanity.consume(45));
        assertEquals(-45, sanity.get());
        assertTrue(sanity.consume(0));
        assertEquals(-45, sanity.get());
    }

    @Test
    void forceConsumeStopsAtFloor() {
        Sanity sanity = new Sanity();
        sanity.consumeForce(45);
        assertEquals(-45, sanity.get());
        sanity.consumeForce(30);
        assertEquals(-45, sanity.get());
    }

    @Test
    void resetGoesBackToZero() {
        Sanity sanity = new Sanity();
        sanity.set(-30);
        sanity.reset();
        assertEquals(0, sanity.get());
    }

    @Test
    void saveRoundTrip() {
        Sanity sanity = new Sanity();
        sanity.set(-17);

        Sanity loaded = new Sanity();
        loaded.load(sanity.save());
        assertEquals(-17, loaded.get());

        loaded.load(new CompoundTag());
        assertEquals(0, loaded.get());
    }

    @Test
    void copyFromTakesTheValue() {
        Sanity source = new Sanity();
        source.set(31);
        Sanity copy = new Sanity();
        copy.copyFrom(source);
        assertEquals(31, copy.get());
    }
}
