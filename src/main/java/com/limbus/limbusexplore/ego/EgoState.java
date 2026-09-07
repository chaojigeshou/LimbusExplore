package com.limbus.limbusexplore.ego;

import net.minecraft.nbt.CompoundTag;

// EGO 状态的玩家数据：结束时间 + 释放的是哪个 EGO + 形态。
// 计时用绝对时间毫秒，跨维度、跨登录都继续走（读档时过期的自动作废）。
public final class EgoState {

    public static final long DURATION_MS = 30_000;

    private long endAtMillis;      // 0 = 不在状态
    private String activeEgoId = "";
    private boolean corroded;

    // 只给 EgoStateApi 用：避免每秒重复回调/同步，不保存
    private int lastNotifiedSecond = -1;

    public boolean isActive(long nowMillis) {
        return endAtMillis > nowMillis;
    }

    public long remainingMillis(long nowMillis) {
        return Math.max(0, endAtMillis - nowMillis);
    }

    /** 向上取整的剩余秒，HUD 显示用。 */
    public int remainingSeconds(long nowMillis) {
        return (int) Math.ceil(remainingMillis(nowMillis) / 1000.0);
    }

    public Ego activeEgo() {
        return Ego.byId(activeEgoId);
    }

    public boolean isCorroded() {
        return corroded;
    }

    public int lastNotifiedSecond() {
        return lastNotifiedSecond;
    }

    public void setLastNotifiedSecond(int second) {
        this.lastNotifiedSecond = second;
    }

    public void enter(Ego ego, boolean corroded, long nowMillis) {
        this.endAtMillis = nowMillis + DURATION_MS;
        this.activeEgoId = ego.id;
        this.corroded = corroded;
        this.lastNotifiedSecond = -1;
    }

    public void clear() {
        this.endAtMillis = 0;
        this.activeEgoId = "";
        this.corroded = false;
        this.lastNotifiedSecond = -1;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("endAt", endAtMillis);
        tag.putString("egoId", activeEgoId);
        tag.putBoolean("corroded", corroded);
        return tag;
    }

    public void load(CompoundTag tag) {
        endAtMillis = tag.getLong("endAt");
        activeEgoId = tag.getString("egoId");
        corroded = tag.getBoolean("corroded");
        lastNotifiedSecond = -1;
        // 加载时已过期的按无状态处理
        if (endAtMillis <= System.currentTimeMillis()) {
            clear();
        }
    }
}
