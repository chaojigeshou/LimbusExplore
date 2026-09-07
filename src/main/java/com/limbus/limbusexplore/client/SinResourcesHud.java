package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.ego.Ego;
import com.limbus.limbusexplore.sin.SinType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;

// 屏幕左侧竖排：顶部一行理智，下面七个罪孽图标。H 键整体显隐。
public final class SinResourcesHud {

    private static final int LEFT = 6;
    private static final int ICON_SIZE = 16;
    private static final int ROW_HEIGHT = 22; // 图标 16 + 6 间隔

    private static boolean visible = true;

    private SinResourcesHud() {
    }

    public static void toggleVisible() {
        visible = !visible;
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        if (!visible) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        // 侵蚀形态的 EGO 状态期间：隐藏资源栏（H 键的按钮恢复后可用）
        if (ClientEgoState.isInState() && ClientEgoState.isCorroded()) {
            return;
        }

        SinType[] sins = SinType.values();
        int y = (screenHeight - ROW_HEIGHT * sins.length) / 2;

        // EGO 状态行（30s 倒计时）
        if (ClientEgoState.isInState()) {
            Ego ego = ClientEgoState.activeEgo();
            if (ego != null) {
                graphics.drawString(minecraft.font,
                        Component.translatable("ego.limbusexplore.state_hud",
                                Component.translatable(ego.displayKey()), ClientEgoState.remainingSeconds()),
                        LEFT + 1, y - 14, ClientEgoState.isCorroded() ? 0xFFE04B4B : 0xFFE8C547, true);
            }
        }

        for (SinType sin : sins) {
            // 图标底衬，地形太亮的时候看得清
            graphics.fill(LEFT - 2, y - 2, LEFT + ICON_SIZE + 2, y + ICON_SIZE + 2, 0x66000000);
            graphics.blit(sin.texture(), LEFT, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            y += ROW_HEIGHT;
        }
    }
}
