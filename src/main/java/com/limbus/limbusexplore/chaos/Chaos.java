package com.limbus.limbusexplore.chaos;

import net.minecraft.nbt.CompoundTag;

/**
 * 混乱值容器（玩家和怪物共用）。
 * 满值起步，受击按混乱伤害扣减；扣到 0 进入混乱状态，持续 CHAOS_TICKS，
 * 结束后混乱值回满重新累积。不受击时缓慢自然回复。
 */
public final class Chaos {

    public static final int MAX = 100;
    /** 自然回复：每 N tick 回 1 点 */
    public static final int REGEN_INTERVAL = 40;
    /** 混乱状态持续 15 秒 */
    public static final int CHAOS_TICKS = 20 * 15;

    private int value = MAX;
    private int chaosTicks;      // >0 表示混乱中
    private int regenTimer;
    private int notifiedSecond = -1;   // 混乱中每秒回调一次，不持久化
    // 混乱锁定位置（服务端定身用，不持久化）
    private double lockX, lockY, lockZ;
    private boolean hasLock;

    public int getNotifiedSecond() {
        return notifiedSecond;
    }

    public void setNotifiedSecond(int second) {
        this.notifiedSecond = second;
    }

    public int get() {
        return value;
    }

    public int getMax() {
        return MAX;
    }

    public boolean isInChaos() {
        return chaosTicks > 0;
    }

    public int getChaosTicks() {
        return chaosTicks;
    }

    public int getRemainingSeconds() {
        return (chaosTicks + 19) / 20;
    }

    public void set(int value) {
        this.value = Math.max(0, Math.min(MAX, value));
    }

    /** 扣混乱值；返回是否因此扣到 0（需要进入混乱） */
    public boolean damage(int amount) {
        if (amount <= 0 || isInChaos()) {
            return false;
        }
        value = Math.max(0, value - amount);
        return value <= 0;
    }

    public void enterChaos() {
        chaosTicks = CHAOS_TICKS;
    }

    public void tickChaos() {
        if (chaosTicks > 0) {
            chaosTicks--;
        }
    }

    /** 混乱结束后回满 */
    public void refill() {
        value = MAX;
    }

    public void reset() {
        value = MAX;
        chaosTicks = 0;
        regenTimer = 0;
        hasLock = false;
    }

    /** 非混乱状态下的缓慢自然回复；返回 true 表示自动回复该同步了 */
    public boolean tickRegen() {
        if (isInChaos() || value >= MAX) {
            regenTimer = 0;
            return false;
        }
        if (++regenTimer >= REGEN_INTERVAL) {
            regenTimer = 0;
            value = Math.min(MAX, value + 1);
            return true;
        }
        return false;
    }

    public void setLock(double x, double y, double z) {
        this.lockX = x;
        this.lockY = y;
        this.lockZ = z;
        this.hasLock = true;
    }

    public boolean hasLock() {
        return hasLock;
    }

    public double getLockX() {
        return lockX;
    }

    public double getLockY() {
        return lockY;
    }

    public double getLockZ() {
        return lockZ;
    }

    public void clearLock() {
        hasLock = false;
    }

    public void copyFrom(Chaos other) {
        this.value = other.value;
        this.chaosTicks = other.chaosTicks;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("value", value);
        tag.putInt("chaosTicks", chaosTicks);
        return tag;
    }

    public void load(CompoundTag tag) {
        value = tag.contains("value") ? tag.getInt("value") : MAX;
        chaosTicks = tag.getInt("chaosTicks");
        if (value <= 0) {
            value = MAX;   // 读档时别直接卡在 0
        }
    }
}
