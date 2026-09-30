package com.limbus.limbusexplore.client.gui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EgoReleaseLayoutTest {
    @Test void wideScreenShowsFourAndPagesTheFifth() {
        var layout = new EgoReleaseLayout(640, 360);
        assertEquals(4, layout.capacity());
        assertEquals(2, layout.pageCount(5));
        assertEquals(1, layout.pageCount(0));
    }

    @Test void normalMinecraftWindowSizesKeepCardsAndRailInside() {
        for (int[] size : new int[][]{{320, 240}, {426, 240}, {640, 360}, {960, 540}, {854, 480}}) {
            var layout = new EgoReleaseLayout(size[0], size[1]);
            for (int count = 1; count <= layout.capacity(); count++) {
                for (int i = 0; i < count; i++) {
                    var card = layout.card(i, count);
                    assertTrue(card.x() >= 0);
                    assertTrue(card.x() + card.w() < layout.rail().x());
                    assertTrue(card.y() + card.h() < EgoReleaseLayout.FOOTER_Y);
                    var info = layout.info(i, count);
                    assertTrue(card.contains(info.x() + 1, info.y() + 1));
                }
            }
            assertTrue(layout.rail().x() + layout.rail().w() <= layout.viewWidth());
        }
    }

    @Test void scaledMouseHitsTheSameCardAndDetailsButton() {
        var layout = new EgoReleaseLayout(426, 240);
        var box = layout.info(1, 3);
        double mouseX = (box.x() + box.w() / 2.0) * layout.scale();
        double mouseY = (box.y() + box.h() / 2.0) * layout.scale();
        assertTrue(box.contains(layout.logical(mouseX), layout.logical(mouseY)));
        assertFalse(layout.info(0, 3).contains(layout.logical(mouseX), layout.logical(mouseY)));
    }

    @Test void narrowScreenUsesFewerCardsInsteadOfSquashingText() {
        assertEquals(2, new EgoReleaseLayout(320, 240).capacity());
        assertEquals(1, new EgoReleaseLayout(240, 300).capacity());
    }
}
