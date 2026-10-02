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

@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class EgoLoadoutCapabilities {

    public static final Capability<EgoLoadout> EGO_LOADOUT =
            CapabilityManager.get(new CapabilityToken<>() {});

    private EgoLoadoutCapabilities() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            EgoLoadoutProvider provider = new EgoLoadoutProvider();
            event.addCapability(new ResourceLocation(LimbusExplore.MODID, "ego_loadout"), provider);
            event.addListener(provider::invalidate);
        }
    }

}
