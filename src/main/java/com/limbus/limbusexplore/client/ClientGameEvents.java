package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.client.gui.EgoLoadoutScreen;
import com.limbus.limbusexplore.client.gui.EgoReleaseScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// FORGE 总线、仅客户端：按键轮询和 tick 都放这。
// 三个按键都是点击型（consumeClick），按住判别那套不需要。
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID, value = Dist.CLIENT)
public final class ClientGameEvents {

    private ClientGameEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        if (ClientModEvents.TOGGLE_SIN_HUD.consumeClick()) {
            SinResourcesHud.toggleVisible();
        }
        if (ClientModEvents.OPEN_EGO_LOADOUT.consumeClick() && minecraft.screen == null) {
            minecraft.setScreen(new EgoLoadoutScreen());
        }
        if (ClientModEvents.RELEASE_EGO.consumeClick() && minecraft.screen == null) {
            minecraft.setScreen(new EgoReleaseScreen());
        }
    }
}
