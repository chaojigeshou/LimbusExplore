package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// EGO 状态的 capability 挂载。状态是短期 buff，死亡重生的克隆做不做都行，
// 这里跟其他数据一样克隆，省得重生瞬间出状态丢失的观感问题。
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

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getEntity().getCapability(EGO_STATE).ifPresent(state ->
                event.getOriginal().getCapability(EGO_STATE).ifPresent(original -> {
                    state.load(original.save());
                }));
    }
}
