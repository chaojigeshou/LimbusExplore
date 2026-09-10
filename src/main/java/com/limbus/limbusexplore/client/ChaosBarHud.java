package com.limbus.limbusexplore.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/** 混乱条：快捷栏上方居中，黄条；混乱中变红闪烁。 */
public final class ChaosBarHud {

    private static final int WIDTH = 182;
    private static final int HEIGHT = 5;

    private ChaosBarHud() {
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        int x = screenWidth / 2 - WIDTH / 2;
        int y = screenHeight - 50;
        int max = Math.max(1, ClientChaos.getMax());
        int filled = (int) (WIDTH * Math.max(0, Math.min(max, ClientChaos.get())) / (float) max);

        // 底 + 填充
        graphics.fill(x - 1, y - 1, x + WIDTH + 1, y + HEIGHT + 1, 0x80000000);
        int color = ClientChaos.isInChaos()
                ? (((System.currentTimeMillis() / 200) % 2 == 0) ? 0xFFE04B4B : 0xFF9E2B2B)
                : 0xFFE8C547;
        graphics.fill(x, y, x + filled, y + HEIGHT, color);
    }
}
