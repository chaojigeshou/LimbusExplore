package com.limbus.limbusexplore.combat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DamageResolutionTest {
    @Test void disablingDamageKindsKeepsStaggerEnabled() {
        assertEquals(new DamageResolution(10, 10), DamageResolution.resolve(10, 0.5f, 1, false, false, true));
    }
    @Test void immunityCannotCauseEvenOnePointOfStagger() {
        assertEquals(new DamageResolution(0, 0), DamageResolution.resolve(10, 0, 1, false, true, true));
        assertEquals(new DamageResolution(0, 0), DamageResolution.resolve(10, 0, 1, true, true, true));
    }
    @Test void resistanceAndStaggerMultiplierComposeWithoutNewStaggerDamage() {
        assertEquals(new DamageResolution(7.5f, 0), DamageResolution.resolve(10, 0.5f, 1, true, true, true));
    }
    @Test void chaosSwitchDoesNotDisableResistance() {
        assertEquals(new DamageResolution(5, 0), DamageResolution.resolve(10, 0.5f, 1, true, true, false));
    }
    @Test void bothDisabledPreserveIncomingDamage() {
        assertEquals(new DamageResolution(10, 0), DamageResolution.resolve(10, 2, 1, true, false, false));
    }
    @Test void zeroNegativeAndInvalidDamageAreHarmless() {
        for (float value : new float[]{0, -1, Float.NaN, Float.POSITIVE_INFINITY}) {
            assertEquals(new DamageResolution(0, 0), DamageResolution.resolve(value, 1, 1, false, true, true));
        }
    }
}
