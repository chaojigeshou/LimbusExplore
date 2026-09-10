package com.limbus.limbusexplore.chaos;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 混乱值 capability，挂在所有 LivingEntity 上（玩家和怪物都有混乱条）。
 * 玩家死亡克隆保留；怪物随实体消亡。
 */
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class ChaosCapabilities {

    public static final Capability<Chaos> CHAOS =
            CapabilityManager.get(new CapabilityToken<>() {});

    private ChaosCapabilities() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(new ResourceLocation(LimbusExplore.MODID, "chaos"),
                    new ChaosProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getEntity().getCapability(CHAOS).ifPresent(chaos ->
                event.getOriginal().getCapability(CHAOS).ifPresent(chaos::copyFrom));
    }
}
