package com.limbus.limbusexplore.combat;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.damagesource.DamageType;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 三系伤害判定入口。优先级（高 → 低）：
 *   1. 代码注册（其它 mod 有依赖时用 register* 方法）
 *   2. 数据包 tag（无需写代码）：weapons/xxx、attackers/xxx、damage/xxx
 *   3. 兜底推断（弹射物→突刺、爆炸→打击、近战按武器、未知→打击）
 * 判定失败一律不至于丢伤害：兜底永远返回一个系。
 */
public final class DamageKindApi {

    private static final Map<Item, DamageKind> ITEM_KINDS = new IdentityHashMap<>();
    private static final Map<EntityType<?>, DamageKind> ATTACKER_KINDS = new IdentityHashMap<>();
    private static final Map<DamageType, DamageKind> DAMAGE_TYPE_KINDS = new IdentityHashMap<>();

    private DamageKindApi() {
    }

    /** 给武器物品指定系（也可以直接用数据包 tag，见 DamageKind#weaponTag） */
    public static void registerItem(Item item, DamageKind kind) {
        ITEM_KINDS.put(item, kind);
    }

    /** 给攻击者实体类型指定系（怪物用什么系打人） */
    public static void registerAttacker(EntityType<?> type, DamageKind kind) {
        ATTACKER_KINDS.put(type, kind);
    }

    /** 给具体 DamageType 指定系（需要精确控制时用） */
    public static void registerDamageType(DamageType type, DamageKind kind) {
        DAMAGE_TYPE_KINDS.put(type, kind);
    }

    public static DamageKind resolve(DamageSource source, Entity attacker) {
        // 1. 代码注册 / 伤害类型 tag
        DamageKind kind = DAMAGE_TYPE_KINDS.get(source.type());
        if (kind == null) {
            kind = byDamageTag(source);
        }
        if (kind != null) {
            return kind;
        }

        // 2. 攻击者：手持物品（注册 / tag / 原版推断）
        if (attacker instanceof LivingEntity living) {
            ItemStack held = living.getMainHandItem();
            DamageKind heldKind = ITEM_KINDS.get(held.getItem());
            if (heldKind == null) {
                heldKind = byWeaponTag(held);
            }
            if (heldKind == null) {
                heldKind = guessByItem(held);
            }
            if (heldKind != null) {
                return heldKind;
            }
            // 攻击者实体注册 / tag
            DamageKind typeKind = ATTACKER_KINDS.get(living.getType());
            if (typeKind == null) {
                typeKind = byAttackerTag(living);
            }
            if (typeKind != null) {
                return typeKind;
            }
        } else if (attacker != null) {
            DamageKind typeKind = ATTACKER_KINDS.get(attacker.getType());
            if (typeKind == null) {
                typeKind = byAttackerTag(attacker);
            }
            if (typeKind != null) {
                return typeKind;
            }
        }

        // 3. 兜底推断
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            return DamageKind.PIERCE;
        }
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            return DamageKind.BLUNT;
        }
        if (attacker instanceof LivingEntity) {
            return DamageKind.SLASH;   // 空手也算近战
        }
        return DamageKind.BLUNT;       // 未知来源：打击
    }

    private static DamageKind byDamageTag(DamageSource source) {
        for (DamageKind kind : DamageKind.values()) {
            if (source.is(kind.damageTag())) {
                return kind;
            }
        }
        return null;
    }

    private static DamageKind byWeaponTag(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (DamageKind kind : DamageKind.values()) {
            if (stack.is(kind.weaponTag())) {
                return kind;
            }
        }
        return null;
    }

    private static DamageKind byAttackerTag(Entity entity) {
        for (DamageKind kind : DamageKind.values()) {
            if (entity.getType().is(kind.attackerTag())) {
                return kind;
            }
        }
        return null;
    }

    /** 原版物品兜底：没配 tag 时按常识归类 */
    private static DamageKind guessByItem(ItemStack stack) {
        Item item = stack.getItem();
        if (item == Items.AIR) {
            return DamageKind.BLUNT;   // 空手/拳
        }
        // 剑斧类 → 斩击
        if (item == Items.WOODEN_SWORD || item == Items.STONE_SWORD || item == Items.IRON_SWORD
                || item == Items.GOLDEN_SWORD || item == Items.DIAMOND_SWORD || item == Items.NETHERITE_SWORD
                || item == Items.WOODEN_AXE || item == Items.STONE_AXE || item == Items.IRON_AXE
                || item == Items.GOLDEN_AXE || item == Items.DIAMOND_AXE || item == Items.NETHERITE_AXE
                || item == Items.SHEARS) {
            return DamageKind.SLASH;
        }
        // 三叉戟/锄类突刺
        if (item == Items.TRIDENT || item == Items.WOODEN_HOE || item == Items.STONE_HOE
                || item == Items.IRON_HOE || item == Items.GOLDEN_HOE || item == Items.DIAMOND_HOE
                || item == Items.NETHERITE_HOE) {
            return DamageKind.PIERCE;
        }
        // 锤/镐/锹类打击
        if (item == Items.WOODEN_PICKAXE || item == Items.STONE_PICKAXE || item == Items.IRON_PICKAXE
                || item == Items.GOLDEN_PICKAXE || item == Items.DIAMOND_PICKAXE || item == Items.NETHERITE_PICKAXE
                || item == Items.WOODEN_SHOVEL || item == Items.STONE_SHOVEL || item == Items.IRON_SHOVEL
                || item == Items.GOLDEN_SHOVEL || item == Items.DIAMOND_SHOVEL || item == Items.NETHERITE_SHOVEL) {
            return DamageKind.BLUNT;
        }
        // 弓/弩 → 突刺（箭）
        if (item == Items.BOW || item == Items.CROSSBOW) {
            return DamageKind.PIERCE;
        }
        return null;
    }

    /** 玩家手持物的判定（UI/调试用） */
    public static DamageKind heldKind(Player player) {
        ItemStack held = player.getMainHandItem();
        DamageKind kind = ITEM_KINDS.get(held.getItem());
        if (kind != null) {
            return kind;
        }
        kind = byWeaponTag(held);
        return kind != null ? kind : guessByItem(held);
    }
}
