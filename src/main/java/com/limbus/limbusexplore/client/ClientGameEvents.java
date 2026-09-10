package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.client.gui.ChaosLockScreen;
import com.limbus.limbusexplore.client.gui.EgoLoadoutScreen;
import com.limbus.limbusexplore.client.gui.EgoReleaseScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// FORGE 总线、仅客户端：按键轮询和 tick 都放这。
// 混乱状态优先级最高：强制锁屏，期间不响应任何按键。
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

        // 混乱锁定：混乱期间强制打开锁屏（吞掉所有输入），结束自动关掉
        if (ClientChaos.isInChaos()) {
            if (!(minecraft.screen instanceof ChaosLockScreen)) {
                minecraft.setScreen(new ChaosLockScreen());
            }
            return;
        }
        if (minecraft.screen instanceof ChaosLockScreen) {
            minecraft.setScreen(null);
        }

        if (ClientModEvents.TOGGLE_SIN_HUD.consumeClick() && minecraft.screen == null) {
            SinResourcesHud.toggleVisible();
        }
        if (ClientModEvents.OPEN_EGO_LOADOUT.consumeClick() && minecraft.screen == null) {
            if (ClientEgoState.isInState()) {
                minecraft.player.displayClientMessage(
                        Component.translatable("screen.limbusexplore.locked_by_state"), true);
            } else {
                minecraft.setScreen(new EgoLoadoutScreen());
            }
        }
        if (ClientModEvents.RELEASE_EGO.consumeClick() && minecraft.screen == null) {
            if (ClientEgoState.isInState()) {
                minecraft.player.displayClientMessage(
                        Component.translatable("screen.limbusexplore.locked_by_state"), true);
            } else {
                minecraft.setScreen(new EgoReleaseScreen());
            }
        }
    }
}
