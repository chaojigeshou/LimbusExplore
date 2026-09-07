package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.LimbusExplore;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/**
 * 侵蚀横幅：全屏覆盖。
 * 释放侵蚀 EGO 瞬间：corrosion_flash（一闪，淡入淡出后消失）；
 * EGO 状态期间（侵蚀形态）：corrosion 持续显示，和状态 30s 同步消失。
 */
public final class CorrosionBannerHud {

    private static final ResourceLocation FLASH =
            new ResourceLocation(LimbusExplore.MODID, "textures/hud/corrosion_flash.png");
    private static final ResourceLocation BAR =
            new ResourceLocation(LimbusExplore.MODID, "textures/hud/corrosion.png");

    /** 闪光时长和淡入淡出区间（毫秒）：淡入要慢，渐显过渡。 */
    private static final long FLASH_TOTAL = 1400;
    private static final long FADE_IN = 700;
    private static final long FADE_OUT = 700;

    private static long flashStart = -1;

    private CorrosionBannerHud() {
    }

    /** 侵蚀释放回包到达时调用，开始闪光。 */
    public static void flash() {
        flashStart = System.currentTimeMillis();
    }

    // 全屏拉伸贴图，前面垫一层半透明黑，避免太刺眼
    private static void blitFull(GuiGraphics graphics, ResourceLocation texture, int screenWidth, int screenHeight) {
        graphics.fill(0, 0, screenWidth, screenHeight, 0x40000000);
        graphics.blit(texture, 0, 0, 0, 0, screenWidth, screenHeight, screenWidth, screenHeight);
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        // 持续横幅：侵蚀形态的 EGO 状态期间全屏显示
        if (ClientEgoState.isInState() && ClientEgoState.isCorroded()) {
            blitFull(graphics, BAR, screenWidth, screenHeight);
        }

        // 瞬间闪光：淡入 → 淡出。只依赖 flashStart 自己计时，
        // 不依赖状态包先到（两个 S2C 包同通道保序通常没问题，但别赌顺序）
        if (flashStart >= 0) {
            long t = System.currentTimeMillis() - flashStart;
            if (t < FLASH_TOTAL) {
                float alpha;
                if (t < FADE_IN) {
                    alpha = t / (float) FADE_IN;
                } else if (t > FLASH_TOTAL - FADE_OUT) {
                    alpha = (FLASH_TOTAL - t) / (float) FADE_OUT;
                } else {
                    alpha = 1f;
                }
                RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
                blitFull(graphics, FLASH, screenWidth, screenHeight);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            } else {
                flashStart = -1;
            }
        }
    }
}
