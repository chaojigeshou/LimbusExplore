package com.limbus.limbusexplore.sanity;

import net.minecraft.nbt.CompoundTag;

// 理智值。范围 [-45, 45]，0 是起点，-45 视为崩溃（判定在 SanityApi.isInChaos，先用别处）。
public final class Sanity {

    public static final int MIN = -45;
    public static final int MAX = 45;

    private int value;

    public int get() {
        return value;
    }

    public void add(int amount) {
        set(value + amount);
    }

    public void set(int value) {
        this.value = Math.max(MIN, Math.min(MAX, value));
    }

    public void reset() {
        this.value = 0;
    }

    // 扣完能不能待在合法范围里（不跌破 MIN）
    public boolean canConsume(int amount) {
        return value - amount >= MIN;
    }

    public boolean consume(int amount) {
        if (!canConsume(amount)) {
            return false;
        }
        value -= amount;
        return true;
    }

    public void copyFrom(Sanity other) {
        this.value = other.value;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("value", value);
        return tag;
    }

    public void load(CompoundTag tag) {
        value = tag.getInt("value");
    }
}
