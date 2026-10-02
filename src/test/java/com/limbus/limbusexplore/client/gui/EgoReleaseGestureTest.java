package com.limbus.limbusexplore.client.gui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EgoReleaseGestureTest {
    @Test void shortClickReleasesExactlyOnce() {
        var gesture = new EgoReleaseGesture();
        gesture.press(2, 1000);
        assertEquals(EgoReleaseGesture.Action.RELEASE, gesture.release(2, 1499));
        assertEquals(EgoReleaseGesture.Action.NONE, gesture.release(2, 1500));
    }
    @Test void holdImmediatelyCorrodesButMouseUpDoesNotRelease() {
        var gesture = new EgoReleaseGesture();
        gesture.press(3, 1000);
        assertEquals(-1, gesture.poll(1499, 3));
        assertEquals(3, gesture.poll(1500, 3));
        assertEquals(-1, gesture.poll(1600, 3));
        assertEquals(EgoReleaseGesture.Action.NONE, gesture.release(3, 1601));
    }
    @Test void holdWithoutRenderFramesStillOnlyCorrodes() {
        var gesture = new EgoReleaseGesture();
        gesture.press(0, 100);
        assertEquals(EgoReleaseGesture.Action.CORRODE, gesture.release(0, 900));
    }
    @Test void draggingOntoAnotherCardNeverReleasesIt() {
        var gesture = new EgoReleaseGesture();
        gesture.press(1, 100);
        assertEquals(-1, gesture.poll(900, 2));
        assertEquals(EgoReleaseGesture.Action.NONE, gesture.release(2, 950));
    }
    @Test void pagingOrOpeningDetailsCancelsThePress() {
        var gesture = new EgoReleaseGesture();
        gesture.press(1, 100);
        gesture.cancel();
        assertEquals(-1, gesture.slot());
        assertEquals(EgoReleaseGesture.Action.NONE, gesture.release(1, 200));
    }
}
