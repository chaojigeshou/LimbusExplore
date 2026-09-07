package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.LimbusExplore;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/**
 * 侵蚀横幅：屏幕顶部中央。
 * 释放侵蚀 EGO 瞬间：corrosion_flash（一闪，淡入淡出）；
 * EGO 状态期间（侵蚀形态）：corrosion 持续显示，和状态 30s 同步消失。
 */
public final class CorrosionBannerHud {

    private static final ResourceLocation FLASH =
            new ResourceLocation(LimbusExplore.MODID, "textures/hud/corrosion_flash.png");
    private static final ResourceLocation BAR =
            new ResourceLocation(LimbusExplore.MODID, "textures/hud/corrosion.png");

    private static final int WIDTH = 224;
    private static final int HEIGHT = 14;
    private static final int Y = 28;

    /** 闪光时长和淡入淡出区间（毫秒）。 */
    private static final long FLASH_TOTAL = 800;
    private static final long FADE_IN = 150;
    private static final long FADE_OUT = 350;

    private static long flashStart = -1;

    private CorrosionBannerHud() {
    }

    /** 侵蚀释放回包到达时调用，开始闪光。 */
    public static void flash() {
        flashStart = System.currentTimeMillis();
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        int x = (screenWidth - WIDTH) / 2;

        // 持续横幅：侵蚀形态的 EGO 状态期间显示
        if (ClientEgoState.isInState() && ClientEgoState.isCorroded()) {
            graphics.blit(BAR, x, Y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
        }

        // 瞬间闪光：淡入 → 保持 → 淡出
        if (flashStart >= 0) {
            long t = System.currentTimeMillis() - flashStart;
            if (t < FLASH_TOTAL && ClientEgoState.isCorroded()) {
                float alpha;
                if (t < FADE_IN) {
                    alpha = t / (float) FADE_IN;
                } else if (t > FLASH_TOTAL - FADE_OUT) {
                    alpha = (FLASH_TOTAL - t) / (float) FADE_OUT;
                } else {
                    alpha = 1f;
                }
                RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
                graphics.blit(FLASH, x, Y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            } else {
                flashStart = -1;
            }
        }
    }
}
