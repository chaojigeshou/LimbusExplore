package com.limbus.limbusexplore.combat;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.chaos.ChaosApi;
import com.limbus.limbusexplore.config.ModConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 伤害结算的唯一入口（服务端）：
 *   1. 判定本次伤害属于哪一系（斩/突/打）
 *   2. 乘上目标该系抗性（乘算，不覆盖别的 mod 改过的数值）
 *   3. 按结果喂混乱值（混乱伤害同样吃抗性）
 * 混乱中被打：伤害放大且不再累计混乱值（那部分也在混乱模块里）。
 */
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class CombatHandler {

    /** 混乱伤害系数：混乱伤害 = 结算后伤害 × 系数 */
    private static final double STAGGER_COEFFICIENT = 1.0;

    private CombatHandler() {
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }
        if (!ModConfig.DAMAGE_KIND_ENABLED.get()) {
            return;
        }

        DamageSource source = event.getSource();
        Entity attackerEntity = source.getEntity();
        LivingEntity attacker = attackerEntity instanceof LivingEntity living ? living : null;
        DamageKind kind = DamageKindApi.resolve(source, attackerEntity);

        float raw = event.getAmount();
        if (ChaosApi.isInChaos(target)) {
            // 混乱就是挨打窗口：伤害放大，不再累计混乱值
            event.setAmount(raw * ChaosApi.CHAOS_DAMAGE_MULTIPLIER);
            return;
        }

        float finalDamage = CombatFormula.finalDamage(attacker, target, kind, raw);
        event.setAmount(finalDamage);
        ChaosApi.damage(target, (int) Math.max(1, Math.round(finalDamage * STAGGER_COEFFICIENT)));
    }
}
