package com.limbus.limbusexplore.combat;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 三系抗性 capability，挂在所有 LivingEntity 上（玩家 + 怪物）。
 * 实体的默认抗性由数据包 tag 决定（见 ResistanceApi#applyTags）：
 *   #limbusexplore:resist/slash/fatal、weak、endured、immune（pierce、blunt 同理）
 */
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class ResistanceCapabilities {

    public static final Capability<Resistance> RESISTANCE =
            CapabilityManager.get(new CapabilityToken<>() {});

    private ResistanceCapabilities() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(new ResourceLocation(LimbusExplore.MODID, "resistance"),
                    new ResistanceProvider());
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getEntity().getCapability(RESISTANCE).ifPresent(resistance ->
                event.getOriginal().getCapability(RESISTANCE).ifPresent(resistance::copyFrom));
    }

    /** 实体 tag：某个系 + 某个分级的 tag */
    public static TagKey<EntityType<?>> tierTag(DamageKind kind, String tier) {
        return TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,
                new ResourceLocation(LimbusExplore.MODID, "resist/" + kind.id + "/" + tier));
    }
}
