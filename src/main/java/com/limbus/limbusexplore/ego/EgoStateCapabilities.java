package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// EGO 状态的 capability 挂载；死亡清理/非死亡克隆统一由 PlayerDataLifecycle 处理。
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class EgoStateCapabilities {

    public static final Capability<EgoState> EGO_STATE =
            CapabilityManager.get(new CapabilityToken<>() {});

    private EgoStateCapabilities() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation(LimbusExplore.MODID, "ego_state"),
                    new EgoStateProvider());
        }
    }

}
