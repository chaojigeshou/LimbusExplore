package com.limbus.limbusexplore.combat;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 三系伤害判定入口。判定顺序：
 *   1. 弹射物 → 突刺；爆炸 → 打击（跟手持武器无关）
 *   2. 代码注册的 DamageType / 数据包 damage tag
 *   3. 玩家：看手持物（下面这张表）
 *   4. 其它生物：先看实体注册 / attackers tag（给怪物配的），再看手持物
 *   5. 兜底：打击
 *
 * 手持物表（其它 mod 可以用 tag 或 API 覆盖）：
 *   空手 / 任意普通物品 → 打击
 *   剑、斧               → 斩击
 *   三叉戟、弓、弩        → 突刺
 *   镐、锹、锄            → 打击
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
        // 1. 弹射物 / 爆炸先判，跟手里拿什么无关
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            return DamageKind.PIERCE;
        }
        if (source.is(DamageTypeTags.IS_EXPLOSION)) {
            return DamageKind.BLUNT;
        }

        // 2. 伤害类型：代码注册，其次数据包 tag
        DamageKind kind = DAMAGE_TYPE_KINDS.get(source.type());
        if (kind == null) {
            kind = byDamageTag(source);
        }
        if (kind != null) {
            return kind;
        }

        // 3. 玩家：手持物说了算（没拿武器就是打击）
        if (attacker instanceof Player) {
            return heldKindOf(attacker);
        }

        // 4. 怪物/其它生物：实体配置优先，其次手持物，最后打击
        if (attacker instanceof LivingEntity living) {
            DamageKind byEntity = ATTACKER_KINDS.get(living.getType());
            if (byEntity == null) {
                byEntity = byAttackerTag(living);
            }
            return byEntity != null ? byEntity : heldKindOf(living);
        }

        // 5. 非生物攻击者（船、矿车之类）
        if (attacker != null) {
            DamageKind byEntity = ATTACKER_KINDS.get(attacker.getType());
            if (byEntity == null) {
                byEntity = byAttackerTag(attacker);
            }
            if (byEntity != null) {
                return byEntity;
            }
        }

        return DamageKind.BLUNT;
    }

    /** 手持物判定：注册 → 物品 tag → 原版表 → 打击 */
    public static DamageKind heldKindOf(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return DamageKind.BLUNT;
        }
        ItemStack stack = living.getMainHandItem();
        DamageKind kind = ITEM_KINDS.get(stack.getItem());
        if (kind == null) {
            kind = byWeaponTag(stack);
        }
        if (kind == null) {
            kind = guessByItem(stack);
        }
        return kind != null ? kind : DamageKind.BLUNT;
    }

    /** 玩家手持物的判定（UI / 调试用） */
    public static DamageKind heldKind(Player player) {
        return heldKindOf(player);
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

    /** 原版物品表；返回 null 表示"没特殊系"→ 调用方给打击 */
    private static DamageKind guessByItem(ItemStack stack) {
        Item item = stack.getItem();
        // 剑、斧 → 斩击
        if (item == Items.WOODEN_SWORD || item == Items.STONE_SWORD || item == Items.IRON_SWORD
                || item == Items.GOLDEN_SWORD || item == Items.DIAMOND_SWORD || item == Items.NETHERITE_SWORD
                || item == Items.WOODEN_AXE || item == Items.STONE_AXE || item == Items.IRON_AXE
                || item == Items.GOLDEN_AXE || item == Items.DIAMOND_AXE || item == Items.NETHERITE_AXE) {
            return DamageKind.SLASH;
        }
        // 三叉戟、弓、弩 → 突刺
        if (item == Items.TRIDENT || item == Items.BOW || item == Items.CROSSBOW) {
            return DamageKind.PIERCE;
        }
        // 镐、锹、锄 → 打击
        if (item == Items.WOODEN_PICKAXE || item == Items.STONE_PICKAXE || item == Items.IRON_PICKAXE
                || item == Items.GOLDEN_PICKAXE || item == Items.DIAMOND_PICKAXE || item == Items.NETHERITE_PICKAXE
                || item == Items.WOODEN_SHOVEL || item == Items.STONE_SHOVEL || item == Items.IRON_SHOVEL
                || item == Items.GOLDEN_SHOVEL || item == Items.DIAMOND_SHOVEL || item == Items.NETHERITE_SHOVEL
                || item == Items.WOODEN_HOE || item == Items.STONE_HOE || item == Items.IRON_HOE
                || item == Items.GOLDEN_HOE || item == Items.DIAMOND_HOE || item == Items.NETHERITE_HOE) {
            return DamageKind.BLUNT;
        }
        return null;   // 空手与其它物品一律走默认打击
    }
}
