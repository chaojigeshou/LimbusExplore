package com.limbus.limbusexplore.chaos;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 混乱值的伤害挂钩与 tick 驱动（FORGE 总线，只处理服务端）：
 * 受击 → 按伤害类型折算混乱伤害 → 扣混乱值；
 * 混乱中被打 → 伤害放大；混乱中玩家禁止攻击。
 */
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class ChaosCombat {

    /** 混乱伤害系数：混乱伤害 = 原伤害 × 系数 × 类型倍率 */
    private static final double COEFFICIENT = 1.0;
    private static final double TYPE_SLASH = 1.0;    // 近战
    private static final double TYPE_PIERCE = 0.8;   // 弹射物
    private static final double TYPE_BLUNT = 1.2;    // 爆炸/其它

    private ChaosCombat() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }
        if (ChaosApi.isInChaos(target)) {
            // 混乱状态就是挨打窗口：伤害放大，且不再累计混乱值
            event.setAmount(event.getAmount() * ChaosApi.CHAOS_DAMAGE_MULTIPLIER);
            return;
        }
        ChaosApi.damage(target, staggerDamage(event));
    }

    private static int staggerDamage(LivingHurtEvent event) {
        double type = TYPE_BLUNT;
        var source = event.getSource();
        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            type = TYPE_PIERCE;
        } else if (source.getDirectEntity() instanceof LivingEntity) {
            type = TYPE_SLASH;
        }
        double amount = event.getAmount() * COEFFICIENT * type;
        return (int) Math.max(1, Math.round(amount));
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        Chaos chaos = ChaosApi.of(entity);
        if (chaos == null) {
            return;
        }
        // 快速跳过：不在混乱且混乱值满的实体（绝大多数）不做任何事
        if (!chaos.isInChaos() && chaos.get() >= Chaos.MAX) {
            return;
        }
        ChaosApi.tick(entity);
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && ChaosApi.isInChaos(player)) {
            event.setCanceled(true);
        }
    }
}
