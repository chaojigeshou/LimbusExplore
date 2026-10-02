package com.limbus.limbusexplore.net;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.chaos.ChaosApi;
import com.limbus.limbusexplore.chaos.ChaosCapabilities;
import com.limbus.limbusexplore.combat.ResistanceCapabilities;
import com.limbus.limbusexplore.ego.EgoLoadoutCapabilities;
import com.limbus.limbusexplore.ego.EgoStateApi;
import com.limbus.limbusexplore.ego.EgoStateCapabilities;
import com.limbus.limbusexplore.sanity.SanityCapabilities;
import com.limbus.limbusexplore.sin.SinCapabilities;
import com.limbus.limbusexplore.sin.SinType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** 跨模块生命周期的唯一协调点，避免六个 Clone 监听相互失效旧实体的能力。 */
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class PlayerDataLifecycle {
    private PlayerDataLifecycle() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EgoStateApi.end(player);
            ChaosApi.reset(player);
        }
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        Player copy = event.getEntity();
        if (copy.level().isClientSide) return;
        Player original = event.getOriginal();
        original.reviveCaps();
        try {
            original.getCapability(SinCapabilities.SIN_RESOURCES).ifPresent(old ->
                    copy.getCapability(SinCapabilities.SIN_RESOURCES).ifPresent(data -> {
                        data.copyFrom(old);
                        if (event.isWasDeath()) {
                            for (SinType type : SinType.values()) if (data.get(type) < 0) data.set(type, 0);
                        }
                    }));
            original.getCapability(SanityCapabilities.SANITY).ifPresent(old ->
                    copy.getCapability(SanityCapabilities.SANITY).ifPresent(data -> data.copyFrom(old)));
            original.getCapability(EgoLoadoutCapabilities.EGO_LOADOUT).ifPresent(old ->
                    copy.getCapability(EgoLoadoutCapabilities.EGO_LOADOUT).ifPresent(data -> data.copyFrom(old)));
            original.getCapability(ResistanceCapabilities.RESISTANCE).ifPresent(old ->
                    copy.getCapability(ResistanceCapabilities.RESISTANCE).ifPresent(data -> {
                        data.copyFrom(old);
                        data.clearOverride(); // 下一次同步从仍有效的 EGO 状态重建，绝不复制孤立覆盖。
                    }));
            if (!event.isWasDeath()) {
                original.getCapability(EgoStateCapabilities.EGO_STATE).ifPresent(old ->
                        copy.getCapability(EgoStateCapabilities.EGO_STATE).ifPresent(data -> data.load(old.save())));
                original.getCapability(ChaosCapabilities.CHAOS).ifPresent(old ->
                        copy.getCapability(ChaosCapabilities.CHAOS).ifPresent(data -> data.copyFrom(old)));
            } else {
                copy.getCapability(EgoStateCapabilities.EGO_STATE).ifPresent(data -> data.clear());
                copy.getCapability(ChaosCapabilities.CHAOS).ifPresent(data -> data.reset());
            }
        } finally {
            original.invalidateCaps();
        }
    }
}
