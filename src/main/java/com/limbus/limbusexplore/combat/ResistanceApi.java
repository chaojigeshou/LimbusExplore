package com.limbus.limbusexplore.combat;

import com.limbus.limbusexplore.net.ModNetworking;
import com.limbus.limbusexplore.net.ResistanceSyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * 三系抗性的业务入口。
 * 数值语义：2.0 致命 / 1.5 弱点 / 1.0 普通 / 0.5 耐性 / 0.0 免疫。
 * 未配置的实体一律 1.0；实体 tag（数据包）只在第一次查询时套用一次，
 * 之后运行时改过（技能/EGO）的值不会被 tag 覆盖。
 */
public final class ResistanceApi {

    private ResistanceApi() {
    }

    public static Resistance of(LivingEntity entity) {
        return entity.getCapability(ResistanceCapabilities.RESISTANCE).orElse(null);
    }

    public static float get(LivingEntity entity, DamageKind kind) {
        Resistance resistance = of(entity);
        if (resistance == null) {
            return Resistance.NORMAL;
        }
        applyTags(entity, resistance);
        return resistance.get(kind);
    }

    public static void set(LivingEntity entity, DamageKind kind, float value) {
        Resistance resistance = of(entity);
        if (resistance == null) {
            return;
        }
        applyTags(entity, resistance);
        resistance.set(kind, value);
        sync(entity);
    }

    /** 直接套用一整套抗性（比如 EGO 覆盖）。 */
    public static void apply(LivingEntity entity, float slash, float pierce, float blunt) {
        Resistance resistance = of(entity);
        if (resistance == null) {
            return;
        }
        applyTags(entity, resistance);
        resistance.set(DamageKind.SLASH, slash);
        resistance.set(DamageKind.PIERCE, pierce);
        resistance.set(DamageKind.BLUNT, blunt);
        sync(entity);
    }

    public static void reset(LivingEntity entity) {
        Resistance resistance = of(entity);
        if (resistance == null) {
            return;
        }
        for (DamageKind kind : DamageKind.values()) {
            resistance.set(kind, Resistance.NORMAL);
        }
        resistance.markTagged();
        sync(entity);
    }

    /** 受击时用：倍率（未知实体返回 1.0） */
    public static float multiplier(LivingEntity entity, DamageKind kind) {
        return get(entity, kind);
    }

    /** 数据包 tag 默认值：fatal / weak / endured / immune，每个系一套 */
    private static void applyTags(LivingEntity entity, Resistance resistance) {
        if (resistance.isTagged()) {
            return;
        }
        resistance.markTagged();
        for (DamageKind kind : DamageKind.values()) {
            if (entity.getType().is(ResistanceCapabilities.tierTag(kind, "immune"))) {
                resistance.set(kind, Resistance.IMMUNE);
            } else if (entity.getType().is(ResistanceCapabilities.tierTag(kind, "fatal"))) {
                resistance.set(kind, Resistance.FATAL);
            } else if (entity.getType().is(ResistanceCapabilities.tierTag(kind, "weak"))) {
                resistance.set(kind, Resistance.WEAK);
            } else if (entity.getType().is(ResistanceCapabilities.tierTag(kind, "endured"))) {
                resistance.set(kind, Resistance.ENDURED);
            }
        }
    }

    public static void sync(LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        Resistance resistance = of(entity);
        if (resistance == null) {
            return;
        }
        ModNetworking.sendToPlayer(player, new ResistanceSyncPacket(
                resistance.get(DamageKind.SLASH),
                resistance.get(DamageKind.PIERCE),
                resistance.get(DamageKind.BLUNT)));
    }
}
