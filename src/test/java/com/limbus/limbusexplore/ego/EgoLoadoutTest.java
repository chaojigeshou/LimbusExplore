package com.limbus.limbusexplore.ego;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EgoLoadoutTest {

    @Test
    void startsEmptyAndDoesNotAuthorizeAnyEgo() {
        EgoLoadout loadout = new EgoLoadout();
        assertEquals(5, EgoLoadout.SLOTS);
        for (Ego ego : Ego.values()) {
            assertFalse(loadout.contains(ego));
            assertNull(loadout.get(ego.level.ordinal()));
        }
        assertFalse(loadout.contains(null));
    }

    @Test
    void eachEgoOnlyFitsItsOwnLevel() {
        EgoLoadout loadout = new EgoLoadout();
        for (Ego ego : Ego.values()) {
            loadout.unlock(ego);
            for (int slot = 0; slot < EgoLoadout.SLOTS; slot++) {
                assertEquals(ego.level.ordinal() == slot, EgoLoadout.canEquip(slot, ego));
            }
            assertTrue(loadout.set(ego.level.ordinal(), ego));
            assertTrue(loadout.contains(ego));
        }
    }

    @Test
    void invalidRequestsPreserveExistingEquipment() {
        EgoLoadout loadout = new EgoLoadout();
        loadout.set(0, Ego.TEST_ZAYIN);
        assertFalse(loadout.set(-1, Ego.TEST_ZAYIN));
        assertFalse(loadout.set(EgoLoadout.SLOTS, null));
        assertFalse(loadout.set(Integer.MAX_VALUE, Ego.TEST_ALEPH));
        assertFalse(loadout.set(0, Ego.TEST_ALEPH));
        assertEquals(Ego.TEST_ZAYIN, loadout.get(0));
        assertNull(loadout.get(-1));
        assertFalse(EgoLoadout.canEquip(0, null));
    }

    @Test
    void unequippingImmediatelyRevokesAuthorization() {
        EgoLoadout loadout = new EgoLoadout();
        loadout.set(0, Ego.TEST_ZAYIN);
        assertTrue(loadout.contains(Ego.TEST_ZAYIN));
        assertTrue(loadout.set(0, null));
        assertFalse(loadout.contains(Ego.TEST_ZAYIN));
    }

    @Test
    void nbtRoundTripKeepsAllSlots() {
        EgoLoadout original = new EgoLoadout();
        for (Ego ego : Ego.values()) {
            original.unlock(ego);
            original.set(ego.level.ordinal(), ego);
        }
        EgoLoadout loaded = new EgoLoadout();
        loaded.load(original.save());
        assertArrayEquals(original.toIds(), loaded.toIds());
    }

    @Test
    void oldSaveWithoutLoadoutStartsEmpty() {
        EgoLoadout loadout = new EgoLoadout();
        loadout.set(0, Ego.TEST_ZAYIN);
        loadout.load(new CompoundTag());
        assertFalse(loadout.contains(Ego.TEST_ZAYIN));
        assertArrayEquals(new String[]{"", "", "", "", ""}, loadout.toIds());
    }

    @Test
    void corruptOrRemovedIdsCannotCreateWrongLevelEquipment() {
        CompoundTag tag = new CompoundTag();
        tag.putString("zayin", Ego.TEST_ALEPH.id);
        tag.putString("teth", "removed_ego");
        tag.putInt("he", 123);
        tag.putString("waw", Ego.TEST_WAW.id);
        EgoLoadout loadout = new EgoLoadout();
        loadout.load(tag);
        assertArrayEquals(new String[]{"", "", "", Ego.TEST_WAW.id, ""}, loadout.toIds());
    }

    @Test
    void cloneCopiesDataWithoutSharingMutableSlots() {
        EgoLoadout original = new EgoLoadout();
        original.set(0, Ego.TEST_ZAYIN);
        EgoLoadout copy = new EgoLoadout();
        copy.copyFrom(original);
        original.set(0, null);
        assertTrue(copy.contains(Ego.TEST_ZAYIN));
        copy.set(1, Ego.TEST_TETH);
        assertFalse(original.contains(Ego.TEST_TETH));
    }

    @Test
    void snapshotsAreCopiesAndReplaceAllSlots() {
        EgoLoadout loadout = new EgoLoadout();
        String[] snapshot = {Ego.TEST_ZAYIN.id, "", "", "", ""};
        loadout.fromIds(snapshot);
        snapshot[0] = "";
        String[] exported = loadout.toIds();
        exported[0] = "";
        assertTrue(loadout.contains(Ego.TEST_ZAYIN));
        loadout.fromIds(new String[]{null});
        assertArrayEquals(new String[]{"", "", "", "", ""}, loadout.toIds());
    }

    @Test
    void snapshotIgnoresExtraSlots() {
        EgoLoadout loadout = new EgoLoadout();
        loadout.fromIds(new String[]{"", "", "", "", Ego.TEST_ALEPH.id, Ego.TEST_ZAYIN.id});
        assertTrue(loadout.contains(Ego.TEST_ALEPH));
        assertFalse(loadout.contains(Ego.TEST_ZAYIN));
    }

    @Test void lockedEgoCannotBeEquippedOrSmuggledInViaSnapshot() {
        EgoLoadout loadout = new EgoLoadout();
        assertFalse(loadout.set(0, Ego.EMBER_WATCH));
        loadout.fromIds(new String[]{Ego.EMBER_WATCH.id});
        assertFalse(loadout.contains(Ego.EMBER_WATCH));
        assertTrue(loadout.unlock(Ego.EMBER_WATCH));
        assertFalse(loadout.unlock(Ego.EMBER_WATCH));
        assertTrue(loadout.set(0, Ego.EMBER_WATCH));
    }

    @Test void unlocksSurviveNbtAndCloneButNotAnEmptySave() {
        EgoLoadout original = new EgoLoadout();
        original.unlock(Ego.EMBER_WATCH);
        original.set(0, Ego.EMBER_WATCH);
        EgoLoadout loaded = new EgoLoadout();
        loaded.load(original.save());
        EgoLoadout clone = new EgoLoadout();
        clone.copyFrom(loaded);
        assertTrue(clone.contains(Ego.EMBER_WATCH));
        loaded.load(new CompoundTag());
        assertFalse(loaded.isUnlocked(Ego.EMBER_WATCH));
        assertTrue(clone.isUnlocked(Ego.EMBER_WATCH));
    }
}
