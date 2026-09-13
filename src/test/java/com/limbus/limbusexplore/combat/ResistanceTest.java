package com.limbus.limbusexplore.combat;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResistanceTest {

    @Test
    void defaultsToNormalForEveryKind() {
        Resistance resistance = new Resistance();
        for (DamageKind kind : DamageKind.values()) {
            assertEquals(Resistance.NORMAL, resistance.get(kind));
        }
    }

    @Test
    void setNeverGoesBelowZero() {
        Resistance resistance = new Resistance();
        resistance.set(DamageKind.SLASH, -3f);
        assertEquals(Resistance.IMMUNE, resistance.get(DamageKind.SLASH));
    }

    @Test
    void overrideWinsUntilCleared() {
        Resistance resistance = new Resistance();
        resistance.set(DamageKind.BLUNT, Resistance.ENDURED);
        resistance.setOverride(DamageKind.BLUNT, Resistance.FATAL);

        assertEquals(Resistance.FATAL, resistance.get(DamageKind.BLUNT));
        assertEquals(Resistance.ENDURED, resistance.getBase(DamageKind.BLUNT));
        assertTrue(resistance.hasOverride());

        resistance.clearOverride();
        assertFalse(resistance.hasOverride());
        assertEquals(Resistance.ENDURED, resistance.get(DamageKind.BLUNT));
    }

    // 覆盖是临时的，存档不能把它写死成基础值（不然 EGO 结束就还原不回去了）
    @Test
    void saveKeepsOverrideOutOfBaseValue() {
        Resistance resistance = new Resistance();
        resistance.set(DamageKind.PIERCE, Resistance.WEAK);
        resistance.setOverride(DamageKind.PIERCE, Resistance.IMMUNE);
        resistance.markTagged();

        Resistance loaded = new Resistance();
        loaded.load(resistance.save());

        assertEquals(Resistance.WEAK, loaded.getBase(DamageKind.PIERCE));
        assertEquals(Resistance.IMMUNE, loaded.get(DamageKind.PIERCE));
        assertTrue(loaded.isTagged());
    }

    @Test
    void emptyTagLoadsAsNormal() {
        Resistance loaded = new Resistance();
        loaded.load(new CompoundTag());
        for (DamageKind kind : DamageKind.values()) {
            assertEquals(Resistance.NORMAL, loaded.get(kind));
        }
        assertFalse(loaded.isTagged());
    }

    @Test
    void copyFromCarriesBothLayers() {
        Resistance source = new Resistance();
        source.set(DamageKind.SLASH, Resistance.WEAK);
        source.setOverride(DamageKind.BLUNT, Resistance.ENDURED);
        source.markTagged();

        Resistance copy = new Resistance();
        copy.copyFrom(source);

        assertEquals(Resistance.WEAK, copy.get(DamageKind.SLASH));
        assertEquals(Resistance.ENDURED, copy.get(DamageKind.BLUNT));
        assertTrue(copy.isTagged());
    }

    @Test
    void tierConstantsMatchLibraryOfRuina() {
        assertEquals(2.0f, Resistance.FATAL);
        assertEquals(1.5f, Resistance.WEAK);
        assertEquals(1.0f, Resistance.NORMAL);
        assertEquals(0.5f, Resistance.ENDURED);
        assertEquals(0.0f, Resistance.IMMUNE);
    }
}
