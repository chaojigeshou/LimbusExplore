package com.limbus.limbusexplore.client.gui;

/** 一次按下只可能产生一次动作：短按释放、长按切侵蚀。翻页/弹窗时统一取消。 */
public final class EgoReleaseGesture {
    public static final long HOLD_MS = 500;
    public enum Action { NONE, RELEASE, CORRODE }
    private int slot = -1;
    private long start;
    private boolean consumed;

    public void press(int slot, long now) {
        this.slot = slot;
        start = now;
        consumed = false;
    }

    public int slot() { return slot; }

    public float progress(long now) {
        return slot < 0 || consumed ? 0 : Math.min(1, Math.max(0, (now - start) / (float) HOLD_MS));
    }

    /** 按住达到阈值时立即切换，松开时不会额外触发释放。 */
    public int poll(long now, int hoveredSlot) {
        if (slot >= 0 && !consumed && hoveredSlot == slot && now - start >= HOLD_MS) {
            consumed = true;
            return slot;
        }
        return -1;
    }

    public Action release(int hoveredSlot, long now) {
        Action action = Action.NONE;
        if (slot >= 0 && slot == hoveredSlot && !consumed) {
            action = now - start >= HOLD_MS ? Action.CORRODE : Action.RELEASE;
        }
        cancel();
        return action;
    }

    public void cancel() {
        slot = -1;
        consumed = false;
    }
}
