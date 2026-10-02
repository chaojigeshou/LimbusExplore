package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.config.ModConfig;
import com.limbus.limbusexplore.sanity.SanityApi;
import com.limbus.limbusexplore.sin.SinApi;
import com.limbus.limbusexplore.sin.SinType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 最小生存资源循环：击杀敌对生物得到 1 暴怒、3 理智，玩家/被动生物不提供奖励。 */
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class EgoProgression {
    private EgoProgression() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onKill(LivingDeathEvent event) {
        if (ModConfig.KILL_REWARDS_ENABLED.get() && event.getEntity() instanceof Enemy
                && event.getSource().getEntity() instanceof ServerPlayer player) {
            SinApi.add(player, SinType.WRATH, 1);
            SanityApi.add(player, 3);
        }
    }
}
