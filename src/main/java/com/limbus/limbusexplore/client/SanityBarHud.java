package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;

/**
 * 理智图标：固定在快捷栏最右侧。
 * 0 以上用 sanity_pos（蓝图），0 以下用 sanity_neg（红图），数字画在图片中心。
 * 不受 H 键（资源栏显隐）影响，常驻。
 */
public final class SanityBarHud {

    private static final ResourceLocation POS =
            new ResourceLocation(LimbusExplore.MODID, "textures/hud/sanity_pos.png");
    private static final ResourceLocation NEG =
            new ResourceLocation(LimbusExplore.MODID, "textures/hud/sanity_neg.png");

    private static final int SIZE = 24;

    private SanityBarHud() {
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        // 侵蚀形态的 EGO 状态期间：理智图标也隐藏
        if (ClientEgoState.isInState() && ClientEgoState.isCorroded()) {
            return;
        }

        int sanity = ClientSanity.get();
        // 快捷栏中心水平 = width/2，右端 + 边距；垂直对齐快捷栏中心
        int x = screenWidth / 2 + 103;
        int y = screenHeight - 35;

        // 底衬 + 图标（16 源放大到 24）
        graphics.fill(x - 2, y - 2, x + SIZE + 2, y + SIZE + 2, 0x66000000);
        graphics.blit(sanity >= 0 ? POS : NEG, x, y, 0, 0, SIZE, SIZE, SIZE, SIZE);

        // 图片中心画数字（drawCenteredString 没有阴影重载，手动居中）
        String text = (sanity >= 0 ? "+" : "") + sanity;
        graphics.drawString(minecraft.font, text,
                x + (SIZE - minecraft.font.width(text)) / 2, y + 7, 0xFFFFFF, true);
    }
}
