package com.limbus.limbusexplore.sin;

import com.limbus.limbusexplore.ego.SinCost;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SinPaymentTest {
    @Test void repeatedSinCostsAreSummedBeforeAnyDebit() {
        SinResources sins = new SinResources();
        sins.set(SinType.WRATH, 3);
        sins.set(SinType.PRIDE, 10);
        SinCost[] costs = {new SinCost(SinType.PRIDE, 1), new SinCost(SinType.WRATH, 2), new SinCost(SinType.WRATH, 2)};
        assertFalse(sins.consumeAll(costs, false));
        assertEquals(3, sins.get(SinType.WRATH));
        assertEquals(10, sins.get(SinType.PRIDE));
        assertTrue(sins.consumeAll(costs, true));
        assertEquals(-1, sins.get(SinType.WRATH));
    }
    @Test void earnedResourcesPayDebtInsteadOfErasingIt() {
        SinResources sins = new SinResources();
        sins.forceAdd(SinType.WRATH, -5);
        sins.add(SinType.WRATH, 1);
        assertEquals(-4, sins.get(SinType.WRATH));
    }
    @Test void overflowAndNegativeCostsCannotCreateFreeResources() {
        SinResources sins = new SinResources();
        sins.set(SinType.WRATH, Integer.MAX_VALUE);
        sins.add(SinType.WRATH, 1);
        assertEquals(Integer.MAX_VALUE, sins.get(SinType.WRATH));
        assertThrows(IllegalArgumentException.class, () -> new SinCost(SinType.WRATH, -1));
    }
}
