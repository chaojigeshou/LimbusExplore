package com.limbus.limbusexplore.combat;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

/**
 * 三系伤害类型（图书馆/巴士的基础分类）。
 * 判定顺序与兼容其它 mod 的方式见 DamageKindApi。
 */
public enum DamageKind {

    SLASH("slash"),
    PIERCE("pierce"),
    BLUNT("blunt");

    public final String id;

    DamageKind(String id) {
        this.id = id;
    }

    public String displayKey() {
        return "damage_kind." + LimbusExplore.MODID + "." + id;
    }

    /** 武器物品 tag：数据包把任意 mod 的武器丢进对应 tag 就能被识别 */
    public TagKey<net.minecraft.world.item.Item> weaponTag() {
        return net.minecraft.tags.ItemTags.create(new ResourceLocation(LimbusExplore.MODID, "weapons/" + id));
    }

    /** 攻击者实体 tag：怪物用什么系打人（远程怪/爆炸怪都靠这个） */
    public TagKey<net.minecraft.world.entity.EntityType<?>> attackerTag() {
        return net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,
                new ResourceLocation(LimbusExplore.MODID, "attackers/" + id));
    }

    /** 伤害类型 tag：直接给 DamageType 分类（1.20.1 的 damage_type registry） */
    public TagKey<net.minecraft.world.damagesource.DamageType> damageTag() {
        return net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.DAMAGE_TYPE,
                new ResourceLocation(LimbusExplore.MODID, "damage/" + id));
    }

    public static DamageKind byId(String id) {
        for (DamageKind kind : values()) {
            if (kind.id.equalsIgnoreCase(id)) {
                return kind;
            }
        }
        return null;
    }
}
