package com.limbus.limbusexplore.client.gui;

import com.limbus.limbusexplore.client.ClientSanity;
import com.limbus.limbusexplore.client.ClientSinResources;
import com.limbus.limbusexplore.client.ego.ClientEgoLoadout;
import com.limbus.limbusexplore.ego.Ego;
import com.limbus.limbusexplore.ego.SinCost;
import com.limbus.limbusexplore.net.EgoReleasePacket;
import com.limbus.limbusexplore.net.ModNetworking;
import com.limbus.limbusexplore.sanity.Sanity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

// R 键打开。已装备的 EGO 横排卡片（Z 最左 A 最右，空槽跳过），
// 卡片：名称、图片、等级、介绍、所需资源组合、理智消耗；左键释放，R/ESC 关闭。
public class EgoReleaseScreen extends Screen {

    private static final int ITEM_W = 124;
    private static final int ITEM_H = 88;
    private static final int ITEM_GAP = 6;

    public EgoReleaseScreen() {
        super(Component.translatable("screen.limbusexplore.ego_release"));
    }

    private static Ego[] visibleEgos() {
        return ClientEgoLoadout.equippedList();
    }

    private static int cardY(int screenHeight) {
        return screenHeight / 2 - ITEM_H / 2 - 12;
    }

    private static int cardX(int index, int count, int screenWidth) {
        int total = count * ITEM_W + (count - 1) * ITEM_GAP;
        return screenWidth / 2 - total / 2 + index * (ITEM_W + ITEM_GAP);
    }

    // 正式图没放之前回退罪孽图标。结果缓存起来：渲染每帧都画，别再每帧查资源包
    // （注意：F3+T 热重载资源包后新图要重进游戏才生效）
    private static final java.util.Map<String, ResourceLocation> TEXTURES = new java.util.HashMap<>();

    private static ResourceLocation textureOf(Minecraft minecraft, Ego ego) {
        return TEXTURES.computeIfAbsent(ego.id, id -> {
            ResourceLocation texture = ego.texture();
            return minecraft.getResourceManager().getResource(texture).isEmpty() ? ego.sin.texture() : texture;
        });
    }

    // 预检：缺哪个资源提示哪个（服务端还会判一次，这里只是省去往返）
    private static boolean checkPay(Minecraft minecraft, Ego ego) {
        for (SinCost cost : ego.costs) {
            int have = ClientSinResources.get(cost.sin());
            if (have < cost.amount()) {
                minecraft.player.displayClientMessage(
                        Component.translatable("ego.limbusexplore.resource_lack",
                                Component.translatable(cost.sin().displayKey()), cost.amount(), have), true);
                return false;
            }
        }
        return true;
    }

    // 预检理智：扣完要留在 -45 以上
    private static boolean checkSanity(Minecraft minecraft, Ego ego) {
        int have = ClientSanity.get();
        if (have - ego.sanityCost >= Sanity.MIN) {
            return true;
        }
        minecraft.player.displayClientMessage(
                Component.translatable("ego.limbusexplore.sanity_lack", ego.sanityCost, have), true);
        return false;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);

        Minecraft minecraft = Minecraft.getInstance();
        gui.drawCenteredString(font, title, width / 2, cardY(height) - 22, 0xFFFFFF);

        Ego[] egos = visibleEgos();
        if (egos.length == 0) {
            gui.drawCenteredString(font, Component.translatable("screen.limbusexplore.ego_empty"),
                    width / 2, height / 2, 0xFF909090);
            return;
        }

        int y = cardY(height);
        for (int i = 0; i < egos.length; i++) {
            Ego ego = egos[i];
            int x = cardX(i, egos.length, width);
            boolean fits = ClientSinResources.canPayClient(ego.costs);
            boolean sanityFits = ClientSanity.get() - ego.sanityCost >= Sanity.MIN;
            boolean hover = mouseX >= x && mouseX < x + ITEM_W && mouseY >= y && mouseY < y + ITEM_H;

            // 卡片底 + 等级色顶条
            gui.fill(x, y, x + ITEM_W, y + ITEM_H, hover ? 0xF03A3A3A : 0xF0282828);
            gui.fill(x, y, x + ITEM_W, y + 2, ego.level.color);

            int textColor = fits ? 0xFFFFFF : 0xFF909090;

            // 图片 + 名称 + 等级
            gui.blit(textureOf(minecraft, ego), x + 5, y + 5, 0, 0, 32, 32, 32, 32);
            gui.drawString(font, minecraft.font.plainSubstrByWidth(
                    Component.translatable(ego.displayKey()).getString(), ITEM_W - 48), x + 42, y + 7, textColor, false);
            gui.drawString(font, Component.translatable(ego.level.displayKey()), x + 42, y + 19, ego.level.color, false);

            // 介绍（两行截断）
            String desc = Component.translatable(ego.descKey()).getString();
            String line1 = minecraft.font.plainSubstrByWidth(desc, ITEM_W - 12);
            String line2 = desc.length() > line1.length()
                    ? minecraft.font.plainSubstrByWidth(desc.substring(line1.length()), ITEM_W - 12) : "";
            gui.drawString(font, line1, x + 6, y + 40, 0xFFBBBBBB, false);
            if (!line2.isEmpty()) {
                gui.drawString(font, line2, x + 6, y + 50, 0xFFBBBBBB, false);
            }

            // 所需资源（组合），缺的标红
            int costX = x + 5;
            int costY = y + 63;
            for (SinCost cost : ego.costs) {
                gui.blit(cost.sin().texture(), costX, costY, 0, 0, 14, 14, 16, 16);
                String label = Component.translatable(cost.sin().displayKey()).getString() + " ×" + cost.amount();
                boolean enough = minecraft.player != null && ClientSinResources.get(cost.sin()) >= cost.amount();
                gui.drawString(font, label, costX + 17, costY + 1, enough ? 0xFFD8D8D8 : 0xFFE04B4B, false);
                costX += 17 + font.width(label) + 8;
            }

            // 理智消耗，不够红
            gui.drawString(font, "理智 -" + ego.sanityCost, x + 5, y + 75,
                    sanityFits ? 0xFF9FD8FF : 0xFFE04B4B, false);
        }

        gui.drawCenteredString(font, Component.translatable("screen.limbusexplore.ego_release_hint"),
                width / 2, y + ITEM_H + 8, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        Minecraft minecraft = Minecraft.getInstance();
        Ego[] egos = visibleEgos();
        int y = cardY(height);
        for (int i = 0; i < egos.length; i++) {
            int x = cardX(i, egos.length, width);
            if (mouseX >= x && mouseX < x + ITEM_W && mouseY >= y && mouseY < y + ITEM_H) {
                Ego ego = egos[i];
                if (checkPay(minecraft, ego) && checkSanity(minecraft, ego)) {
                    ModNetworking.sendToServer(new EgoReleasePacket(ego.id));
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_R) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
