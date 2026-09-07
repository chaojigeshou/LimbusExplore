package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.LimbusExplore;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

// MOD 总线、仅客户端：只做「注册」——overlay 和按键都在这。
// 按键的实际逻辑在 ClientGameEvents（FORGE 总线），别混。
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    static final KeyMapping TOGGLE_SIN_HUD = new KeyMapping(
            "key.limbusexplore.toggle_sin_hud",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "key.categories.limbusexplore");

    static final KeyMapping OPEN_EGO_LOADOUT = new KeyMapping(
            "key.limbusexplore.open_ego_loadout",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G, // 默认 L 被 vanilla「进度」占着，G 没冲突
            "key.categories.limbusexplore");

    static final KeyMapping RELEASE_EGO = new KeyMapping(
            "key.limbusexplore.release_ego",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.limbusexplore");

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("sin_resources", SinResourcesHud::render);
        event.registerAboveAll("sanity_bar", SanityBarHud::render);
        event.registerAboveAll("corrosion_banner", CorrosionBannerHud::render);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_SIN_HUD);
        event.register(OPEN_EGO_LOADOUT);
        event.register(RELEASE_EGO);
    }
}
