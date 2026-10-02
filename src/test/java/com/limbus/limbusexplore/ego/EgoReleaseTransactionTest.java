package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.sanity.Sanity;
import com.limbus.limbusexplore.sin.SinResources;
import com.limbus.limbusexplore.sin.SinType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EgoReleaseTransactionTest {
    private final EgoLoadout loadout = new EgoLoadout();
    private final SinResources sins = new SinResources();
    private final Sanity sanity = new Sanity();
    private final EgoState state = new EgoState();
    private final long now = 100_000L;

    EgoReleaseTransactionTest() {
        loadout.unlock(Ego.EMBER_WATCH);
        loadout.set(0, Ego.EMBER_WATCH);
        sins.set(SinType.WRATH, 10);
        sanity.set(45);
    }

    private EgoApi.ReleaseResult release(boolean corroded) {
        return EgoReleaseTransaction.execute(loadout, sins, sanity, state, Ego.EMBER_WATCH,
                corroded, true, false, now);
    }

    @Test void successCommitsAllThreeContainers() {
        assertEquals(EgoApi.ReleaseResult.RELEASED, release(false));
        assertEquals(8, sins.get(SinType.WRATH));
        assertEquals(35, sanity.get());
        assertTrue(state.isActive(now));
        assertEquals(Ego.EMBER_WATCH, state.activeEgo());
    }

    @Test void insufficientSinDoesNotSpendSanityOrEnterState() {
        sins.set(SinType.WRATH, 1);
        assertEquals(EgoApi.ReleaseResult.SIN_LACK, release(false));
        assertEquals(1, sins.get(SinType.WRATH));
        assertEquals(45, sanity.get());
        assertFalse(state.hasState());
    }

    @Test void insufficientSanityDoesNotPartiallySpendSin() {
        sanity.set(-40);
        assertEquals(EgoApi.ReleaseResult.SANITY_LACK, release(false));
        assertEquals(10, sins.get(SinType.WRATH));
        assertEquals(-40, sanity.get());
        assertFalse(state.hasState());
    }

    @Test void corrosionCanOverdrawAndClampsSanity() {
        sins.set(SinType.WRATH, 0);
        sanity.set(-40);
        assertEquals(EgoApi.ReleaseResult.CORRODED, release(true));
        assertEquals(-3, sins.get(SinType.WRATH));
        assertEquals(-45, sanity.get());
        assertTrue(state.isCorroded());
    }

    @Test void unequippedRejectsBothFormsWithoutCharge() {
        loadout.set(0, null);
        assertEquals(EgoApi.ReleaseResult.NOT_EQUIPPED, release(false));
        assertEquals(EgoApi.ReleaseResult.NOT_EQUIPPED, release(true));
        assertEquals(10, sins.get(SinType.WRATH));
        assertEquals(45, sanity.get());
    }

    @Test void duplicateRequestCannotSpendTwice() {
        release(false);
        assertEquals(EgoApi.ReleaseResult.IN_EGO_STATE, release(true));
        assertEquals(8, sins.get(SinType.WRATH));
        assertEquals(35, sanity.get());
    }

    @Test void missingCapabilitiesAndDeadPlayersFailClosed() {
        assertEquals(EgoApi.ReleaseResult.UNAVAILABLE, EgoReleaseTransaction.execute(
                loadout, sins, sanity, null, Ego.EMBER_WATCH, true, true, false, now));
        assertEquals(EgoApi.ReleaseResult.UNAVAILABLE, EgoReleaseTransaction.execute(
                loadout, sins, sanity, state, Ego.EMBER_WATCH, true, false, false, now));
        assertEquals(10, sins.get(SinType.WRATH));
        assertFalse(state.hasState());
    }

    @Test void staggerAndPrecheckNeverMutateData() {
        assertEquals(EgoApi.ReleaseResult.IN_CHAOS, EgoReleaseTransaction.execute(
                loadout, sins, sanity, state, Ego.EMBER_WATCH, true, true, true, now));
        assertEquals(EgoApi.ReleaseResult.RELEASED, EgoReleaseTransaction.check(
                loadout, sins, sanity, state, Ego.EMBER_WATCH, false, true, false, now));
        assertEquals(10, sins.get(SinType.WRATH));
        assertEquals(45, sanity.get());
        assertFalse(state.hasState());
    }
}
