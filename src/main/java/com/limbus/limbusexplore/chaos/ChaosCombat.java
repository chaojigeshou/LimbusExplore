package com.limbus.limbusexplore.chaos;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 混乱值的 tick 驱动与行动限制（伤害结算在 combat/CombatHandler，那边统一判三系+抗性）。
 */
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class ChaosCombat {

    private ChaosCombat() {
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
        if (!event.getEntity().level().isClientSide && ChaosApi.isInChaos(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}
