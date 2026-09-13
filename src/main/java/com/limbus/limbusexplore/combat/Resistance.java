package com.limbus.limbusexplore.combat;

import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;

/**
 * 三系抗性容器（倍率语义，和图书馆/巴士一致）：
 *   2.0 致命 / 1.5 弱点 / 1.0 普通 / 0.5 耐性 / 0.0 免疫
 * 玩家和怪物共用，默认全普通。
 * 分两层：values 是基础值（数据包 tag / 常驻配置），override 是临时覆盖（EGO 之类），
 * 覆盖层清掉就自动回到基础值，不用存旧值。
 */
public final class Resistance {

    public static final float FATAL = 2.0f;
    public static final float WEAK = 1.5f;
    public static final float NORMAL = 1.0f;
    public static final float ENDURED = 0.5f;
    public static final float IMMUNE = 0.0f;

    private final EnumMap<DamageKind, Float> values = new EnumMap<>(DamageKind.class);
    private final EnumMap<DamageKind, Float> override = new EnumMap<>(DamageKind.class);
    // 实体 tag 默认值只套一次
    private boolean tagged;

    public Resistance() {
        for (DamageKind kind : DamageKind.values()) {
            values.put(kind, NORMAL);
        }
    }

    /** 实际生效的抗性：有覆盖看覆盖，没有看基础值。 */
    public float get(DamageKind kind) {
        Float covering = override.get(kind);
        return covering != null ? covering : getBase(kind);
    }

    /** 基础值，不看覆盖层。 */
    public float getBase(DamageKind kind) {
        return values.getOrDefault(kind, NORMAL);
    }

    public void set(DamageKind kind, float value) {
        values.put(kind, Math.max(0f, value));
    }

    public void setOverride(DamageKind kind, float value) {
        override.put(kind, Math.max(0f, value));
    }

    public void clearOverride() {
        override.clear();
    }

    public boolean hasOverride() {
        return !override.isEmpty();
    }

    public boolean isTagged() {
        return tagged;
    }

    public void markTagged() {
        tagged = true;
    }

    public void copyFrom(Resistance other) {
        for (DamageKind kind : DamageKind.values()) {
            values.put(kind, other.getBase(kind));
        }
        override.clear();
        for (DamageKind kind : DamageKind.values()) {
            if (other.override.containsKey(kind)) {
                override.put(kind, other.override.get(kind));
            }
        }
        this.tagged = other.tagged;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        for (DamageKind kind : DamageKind.values()) {
            // 存基础值，覆盖层单独存，别把临时覆盖写死进基础值
            tag.putFloat(kind.id, getBase(kind));
            if (override.containsKey(kind)) {
                tag.putFloat("override_" + kind.id, override.get(kind));
            }
        }
        tag.putBoolean("tagged", tagged);
        return tag;
    }

    public void load(CompoundTag tag) {
        for (DamageKind kind : DamageKind.values()) {
            if (tag.contains(kind.id)) {
                values.put(kind, tag.getFloat(kind.id));
            }
            if (tag.contains("override_" + kind.id)) {
                override.put(kind, tag.getFloat("override_" + kind.id));
            }
        }
        tagged = tag.getBoolean("tagged");
    }
}
