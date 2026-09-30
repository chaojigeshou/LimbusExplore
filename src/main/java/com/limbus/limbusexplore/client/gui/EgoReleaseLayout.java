package com.limbus.limbusexplore.client.gui;

/** 绘制和鼠标命中共享同一逻辑坐标；不依赖 Minecraft，可独立测试各种 GUI 缩放。 */
public final class EgoReleaseLayout {
    public static final int CARD_W = 132;
    public static final int CARD_H = 236;
    public static final int GAP = 12;
    public static final int TOP = 43;
    public static final int FOOTER_Y = 295;
    public static final int VIEW_H = 360;

    public record Box(int x, int y, int w, int h) {
        public boolean contains(double px, double py) {
            return px >= x && py >= y && px < x + w && py < y + h;
        }
    }

    private final float scale;
    private final int viewWidth;
    private final int capacity;
    private final boolean railVisible;

    public EgoReleaseLayout(int width, int height) {
        scale = Math.max(0.1f, height / (float) VIEW_H);
        viewWidth = Math.max(1, (int) Math.floor(width / scale));
        railVisible = viewWidth >= 260;
        int available = viewWidth - 24 - (railVisible ? 52 : 0);
        capacity = Math.max(1, Math.min(4, (available + GAP) / (CARD_W + GAP)));
    }

    public float scale() { return scale; }
    public int viewWidth() { return viewWidth; }
    public int capacity() { return capacity; }
    public boolean railVisible() { return railVisible; }
    public double logical(double coordinate) { return coordinate / scale; }

    public int pageCount(int equippedCount) {
        return Math.max(1, (equippedCount + capacity - 1) / capacity);
    }

    public Box card(int visibleIndex, int visibleCount) {
        int area = viewWidth - (railVisible ? 52 : 0);
        int total = visibleCount * CARD_W + Math.max(0, visibleCount - 1) * GAP;
        return new Box((area - total) / 2 + visibleIndex * (CARD_W + GAP), TOP, CARD_W, CARD_H);
    }

    public Box info(int visibleIndex, int visibleCount) {
        Box card = card(visibleIndex, visibleCount);
        return new Box(card.x + 85, card.y + 4, 41, 13);
    }

    public Box rail() { return new Box(viewWidth - 48, TOP + 29, 36, 195); }
    public Box previousPage() { return new Box(viewWidth / 2 - 62, 334, 24, 17); }
    public Box nextPage() { return new Box(viewWidth / 2 + 38, 334, 24, 17); }
    public Box details() {
        int w = Math.min(364, viewWidth - 24);
        return new Box((viewWidth - w) / 2, 37, w, 278);
    }
    public Box closeDetails() {
        Box box = details();
        return new Box(box.x + box.w - 27, box.y + 8, 18, 16);
    }
}
