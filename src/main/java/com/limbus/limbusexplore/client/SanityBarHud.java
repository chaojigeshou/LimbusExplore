package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.sanity.Sanity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;

// 理智值进度条，固定在屏幕左上角。范围 [-45, 45]，正绿负红。
// 不受 H 键（资源栏显隐）影响，常驻。
public final class SanityBarHud {

    private static final int X = 6;
    private static final int Y = 6;
    private static final int WIDTH = 90;
    private static final int HEIGHT = 6;

    private SanityBarHud() {
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        // 侵蚀形态的 EGO 状态期间：理智条也隐藏
        if (ClientEgoState.isInState() && ClientEgoState.isCorroded()) {
            return;
        }

        int sanity = ClientSanity.get();
        double ratio = (sanity - Sanity.MIN) / (double) (Sanity.MAX - Sanity.MIN);
        int color = sanity < 0 ? 0xFFE04B4B : 0xFF7FBF7F;

        // 底 + 填充
        graphics.fill(X - 1, Y - 1, X + WIDTH + 1, Y + HEIGHT + 1, 0x66000000);
        graphics.fill(X, Y, X + (int) (WIDTH * ratio), Y + HEIGHT, color);

        // 数字
        graphics.drawString(minecraft.font,
                Component.translatable("sanity.limbusexplore.hud", (sanity >= 0 ? "+" : "") + sanity),
                X + WIDTH + 5, Y - 1, color, true);
    }
}
