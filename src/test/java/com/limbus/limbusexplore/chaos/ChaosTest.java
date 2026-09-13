package com.limbus.limbusexplore.chaos;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChaosTest {

    @Test
    void startsFull() {
        Chaos chaos = new Chaos();
        assertEquals(Chaos.MAX, chaos.get());
        assertFalse(chaos.isInChaos());
    }

    @Test
    void damageReportsOnlyWhenItHitsZero() {
        Chaos chaos = new Chaos();
        assertFalse(chaos.damage(30));
        assertEquals(70, chaos.get());
        assertTrue(chaos.damage(70));
        assertEquals(0, chaos.get());
        // 打过头也停在 0
        assertTrue(chaos.damage(999));
        assertEquals(0, chaos.get());
    }

    @Test
    void zeroOrNegativeDamageIsIgnored() {
        Chaos chaos = new Chaos();
        assertFalse(chaos.damage(0));
        assertFalse(chaos.damage(-5));
        assertEquals(Chaos.MAX, chaos.get());
    }

    @Test
    void damageIsIgnoredWhileAlreadyInChaos() {
        Chaos chaos = new Chaos();
        chaos.enterChaos();
        assertFalse(chaos.damage(50));
        assertEquals(Chaos.MAX, chaos.get());
    }

    @Test
    void chaosLastsFifteenSeconds() {
        Chaos chaos = new Chaos();
        chaos.enterChaos();
        assertTrue(chaos.isInChaos());
        assertEquals(Chaos.CHAOS_TICKS, chaos.getChaosTicks());
        assertEquals(15, chaos.getRemainingSeconds());

        for (int i = 0; i < 20; i++) {
            chaos.tickChaos();
        }
        assertEquals(14, chaos.getRemainingSeconds());

        for (int i = 0; i < Chaos.CHAOS_TICKS - 20; i++) {
            chaos.tickChaos();
        }
        assertFalse(chaos.isInChaos());
        assertEquals(0, chaos.getRemainingSeconds());
        // 数到 0 之后再 tick 不会变负
        chaos.tickChaos();
        assertEquals(0, chaos.getChaosTicks());
    }

    @Test
    void refillPutsItBackToMax() {
        Chaos chaos = new Chaos();
        chaos.damage(80);
        chaos.enterChaos();
        chaos.refill();
        assertEquals(Chaos.MAX, chaos.get());
    }

    @Test
    void regenTakesFortyTicksAndStopsAtMax() {
        Chaos chaos = new Chaos();
        chaos.damage(10);

        for (int tick = 1; tick < Chaos.REGEN_INTERVAL; tick++) {
            assertFalse(chaos.tickRegen(), "第 " + tick + " tick 不该回复");
        }
        assertEquals(90, chaos.get());

        assertTrue(chaos.tickRegen());
        assertEquals(91, chaos.get());
    }

    @Test
    void noRegenWhileInChaosOrAtFull() {
        Chaos chaos = new Chaos();
        // 满值不回复，也不攒计时器
        for (int tick = 0; tick < Chaos.REGEN_INTERVAL * 2; tick++) {
            assertFalse(chaos.tickRegen());
        }
        assertEquals(Chaos.MAX, chaos.get());

        chaos.damage(50);
        chaos.enterChaos();
        for (int tick = 0; tick < Chaos.REGEN_INTERVAL * 2; tick++) {
            assertFalse(chaos.tickRegen());
        }
        assertEquals(50, chaos.get());
    }

    @Test
    void setClampsIntoRange() {
        Chaos chaos = new Chaos();
        chaos.set(-20);
        assertEquals(0, chaos.get());
        chaos.set(Chaos.MAX + 50);
        assertEquals(Chaos.MAX, chaos.get());
    }

    @Test
    void lockRoundTrip() {
        Chaos chaos = new Chaos();
        assertFalse(chaos.hasLock());
        chaos.setLock(1.5, 64.0, -3.25);
        assertTrue(chaos.hasLock());
        assertEquals(1.5, chaos.getLockX());
        assertEquals(64.0, chaos.getLockY());
        assertEquals(-3.25, chaos.getLockZ());
        chaos.clearLock();
        assertFalse(chaos.hasLock());
    }

    @Test
    void resetClearsEverything() {
        Chaos chaos = new Chaos();
        chaos.damage(90);
        chaos.enterChaos();
        chaos.setLock(0, 0, 0);
        chaos.reset();

        assertEquals(Chaos.MAX, chaos.get());
        assertFalse(chaos.isInChaos());
        assertFalse(chaos.hasLock());
    }

    // 读档时停在 0 会直接卡成混乱，所以读出来是满值
    @Test
    void loadNeverLeavesItAtZero() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("value", 0);
        tag.putInt("chaosTicks", 100);

        Chaos chaos = new Chaos();
        chaos.load(tag);
        assertEquals(Chaos.MAX, chaos.get());
        assertEquals(100, chaos.getChaosTicks());
        assertTrue(chaos.isInChaos());
    }

    @Test
    void saveRoundTrip() {
        Chaos chaos = new Chaos();
        chaos.damage(45);
        chaos.enterChaos();
        chaos.tickChaos();

        Chaos loaded = new Chaos();
        loaded.load(chaos.save());
        assertEquals(55, loaded.get());
        assertEquals(Chaos.CHAOS_TICKS - 1, loaded.getChaosTicks());
    }

    @Test
    void cloneCarriesValueAndChaosTicks() {
        Chaos source = new Chaos();
        source.damage(20);
        source.enterChaos();

        Chaos copy = new Chaos();
        copy.copyFrom(source);
        assertEquals(80, copy.get());
        assertTrue(copy.isInChaos());
    }
}
