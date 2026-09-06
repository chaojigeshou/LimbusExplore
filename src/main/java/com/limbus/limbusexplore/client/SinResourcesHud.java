package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.sin.SinType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;

// 屏幕左侧竖排：顶部一行理智，下面七个罪孽图标。H 键整体显隐。
public final class SinResourcesHud {

    private static final int LEFT = 6;
    private static final int ICON_SIZE = 16;
    private static final int ROW_HEIGHT = 18;

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

        SinType[] sins = SinType.values();
        int y = (screenHeight - ROW_HEIGHT * sins.length) / 2;

        // 理智值单独一行，正绿负红
        int sanity = ClientSanity.get();
        graphics.drawString(minecraft.font,
                Component.translatable("sanity.limbusexplore.hud", String.format("%+d", sanity)),
                LEFT + 1, y - 14, sanity < 0 ? 0xFFE04B4B : 0xFF7FBF7F, true);

        for (SinType sin : sins) {
            // 图标底衬，地形太亮的时候看得清
            graphics.fill(LEFT - 2, y - 2, LEFT + ICON_SIZE + 2, y + ICON_SIZE + 2, 0x66000000);
            graphics.blit(sin.texture(), LEFT, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            y += ROW_HEIGHT;
        }
    }
}
