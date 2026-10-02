package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.ego.Ego;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientEgoLoadoutTest {

    @AfterEach
    void disconnect() {
        ClientEgoLoadout.clear();
    }

    @Test
    void snapshotUpdatesEquipmentAndResetsCorrosionSelection() {
        ClientEgoLoadout.applySnapshot(new String[]{Ego.TEST_ZAYIN.id, "", "", "", ""});
        ClientEgoLoadout.setCorroded(0, true);
        assertTrue(ClientEgoLoadout.isCorroded(0));
        ClientEgoLoadout.applySnapshot(new String[]{"", Ego.TEST_TETH.id, "", "", ""});
        assertNull(ClientEgoLoadout.get(0));
        assertFalse(ClientEgoLoadout.isCorroded(0));
        assertArrayEquals(new Ego[]{Ego.TEST_TETH}, ClientEgoLoadout.equippedList());
    }

    @Test
    void disconnectDropsEquipmentAndDoesNotSendBeforeFirstSnapshot() {
        ClientEgoLoadout.applySnapshot(new String[]{Ego.TEST_ZAYIN.id, "", "", "", ""});
        ClientEgoLoadout.setCorroded(0, true);
        ClientEgoLoadout.clear();
        assertNull(ClientEgoLoadout.get(0));
        assertEquals(0, ClientEgoLoadout.equippedList().length);
        assertFalse(ClientEgoLoadout.isCorroded(0));
        assertFalse(ClientEgoLoadout.equip(0, Ego.TEST_ZAYIN));
    }

    @Test void onlyServerUnlockSnapshotEnablesNewEgo() {
        ClientEgoLoadout.applySnapshot(new String[]{Ego.EMBER_WATCH.id, "", "", "", ""});
        assertFalse(ClientEgoLoadout.isUnlocked(Ego.EMBER_WATCH));
        assertNull(ClientEgoLoadout.get(0));
        ClientEgoLoadout.applySnapshot(new String[]{Ego.EMBER_WATCH.id, "", "", "", ""},
                new String[]{Ego.EMBER_WATCH.id});
        assertEquals(Ego.EMBER_WATCH, ClientEgoLoadout.get(0));
        ClientEgoLoadout.clear();
        assertFalse(ClientEgoLoadout.isUnlocked(Ego.EMBER_WATCH));
    }
}
