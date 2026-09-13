package com.limbus.limbusexplore.ego;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EgoStateTest {

    @Test
    void startsIdle() {
        EgoState state = new EgoState();
        assertFalse(state.isActive(System.currentTimeMillis()));
        assertEquals(0, state.remainingSeconds(System.currentTimeMillis()));
    }

    @Test
    void lastsThirtySeconds() {
        EgoState state = new EgoState();
        long now = System.currentTimeMillis();
        state.enter(Ego.TEST_ZAYIN, false, now);

        assertTrue(state.isActive(now));
        assertTrue(state.isActive(now + EgoState.DURATION_MS - 1));
        assertFalse(state.isActive(now + EgoState.DURATION_MS));
        assertEquals(Ego.TEST_ZAYIN, state.activeEgo());
        assertFalse(state.isCorroded());
    }

    @Test
    void remainingSecondsRoundsUp() {
        EgoState state = new EgoState();
        long now = System.currentTimeMillis();
        state.enter(Ego.TEST_TETH, true, now);

        assertEquals(30, state.remainingSeconds(now));
        assertEquals(30, state.remainingSeconds(now + 1));
        assertEquals(1, state.remainingSeconds(now + EgoState.DURATION_MS - 1));
        assertEquals(0, state.remainingSeconds(now + EgoState.DURATION_MS));
        // 过期之后不会变成负数
        assertEquals(0, state.remainingSeconds(now + EgoState.DURATION_MS * 2));
        assertTrue(state.isCorroded());
    }

    @Test
    void clearResetsToIdle() {
        EgoState state = new EgoState();
        state.enter(Ego.TEST_HE, true, System.currentTimeMillis());
        state.clear();

        assertFalse(state.isActive(System.currentTimeMillis()));
        assertEquals(null, state.activeEgo());
        assertFalse(state.isCorroded());
    }

    @Test
    void saveRoundTripKeepsRunningState() {
        EgoState state = new EgoState();
        state.enter(Ego.TEST_WAW, true, System.currentTimeMillis());

        EgoState loaded = new EgoState();
        loaded.load(state.save());

        assertTrue(loaded.isActive(System.currentTimeMillis()));
        assertEquals(Ego.TEST_WAW, loaded.activeEgo());
        assertTrue(loaded.isCorroded());
    }

    // 下线期间把 30 秒走完了，上来就是没状态
    @Test
    void expiredSaveLoadsAsIdle() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("endAt", System.currentTimeMillis() - 1);
        tag.putString("egoId", Ego.TEST_ALEPH.id);
        tag.putBoolean("corroded", false);

        EgoState loaded = new EgoState();
        loaded.load(tag);

        assertFalse(loaded.isActive(System.currentTimeMillis()));
        assertEquals(null, loaded.activeEgo());
    }
}
