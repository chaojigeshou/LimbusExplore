package com.limbus.limbusexplore.combat;

import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;

/**
 * 三系抗性容器（倍率语义，和图书馆/巴士一致）：
 *   2.0 致命 / 1.5 弱点 / 1.0 普通 / 0.5 耐性 / 0.0 免疫
 * 玩家和怪物共用，默认全普通。
 */
public final class Resistance {

    public static final float FATAL = 2.0f;
    public static final float WEAK = 1.5f;
    public static final float NORMAL = 1.0f;
    public static final float ENDURED = 0.5f;
    public static final float IMMUNE = 0.0f;

    private final EnumMap<DamageKind, Float> values = new EnumMap<>(DamageKind.class);
    // 实体 tag 默认值只套一次
    private boolean tagged;

    public Resistance() {
        for (DamageKind kind : DamageKind.values()) {
            values.put(kind, NORMAL);
        }
    }

    public float get(DamageKind kind) {
        return values.getOrDefault(kind, NORMAL);
    }

    public void set(DamageKind kind, float value) {
        values.put(kind, Math.max(0f, value));
    }

    public boolean isTagged() {
        return tagged;
    }

    public void markTagged() {
        tagged = true;
    }

    public void copyFrom(Resistance other) {
        for (DamageKind kind : DamageKind.values()) {
            values.put(kind, other.get(kind));
        }
        this.tagged = other.tagged;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (DamageKind kind : DamageKind.values()) {
            tag.putFloat(kind.id, get(kind));
        }
        tag.putBoolean("tagged", tagged);
        return tag;
    }

    public void load(CompoundTag tag) {
        for (DamageKind kind : DamageKind.values()) {
            if (tag.contains(kind.id)) {
                values.put(kind, tag.getFloat(kind.id));
            }
        }
        tagged = tag.getBoolean("tagged");
    }
}
