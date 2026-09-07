package com.limbus.limbusexplore.client.gui;

import com.limbus.limbusexplore.client.ClientEgoState;
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

// R 键打开。已装备的 EGO 竖排卡片（Z 最左 A 最右，空槽跳过）。
// 短按（<500ms）= 释放当前形态；长按（>=500ms）= 切成侵蚀态（卡片变红、左侧进度条从底到顶、轻微抖动）；
// 再短按释放的就是侵蚀版；右键取消侵蚀态。R/ESC 关闭。
// 注意：卡片序号是「可见顺序」，槽位号是 0..4，两者别搞混（setCorroded 要槽位号）。
public class EgoReleaseScreen extends Screen {

    private static final int ITEM_W = 120;
    private static final int ITEM_H = 150;
    private static final int ITEM_GAP = 10;
    private static final long LONG_PRESS_MS = 500;

    private int pressSlot = -1;
    private long pressStart;

    public EgoReleaseScreen() {
        super(Component.translatable("screen.limbusexplore.ego_release"));
    }

    private static Ego[] visibleEgos() {
        return ClientEgoLoadout.equippedList();
    }

    private static int cardY(int screenHeight) {
        return screenHeight / 2 - ITEM_H / 2 - 12;
    }

    // visibleIndex：该槽在可见序列里的第几张
    private static int cardX(int visibleIndex, int count, int screenWidth) {
        int total = count * ITEM_W + (count - 1) * ITEM_GAP;
        return screenWidth / 2 - total / 2 + visibleIndex * (ITEM_W + ITEM_GAP);
    }

    /** 命中哪张卡，返回槽位号；没点中返回 -1。 */
    private int slotAt(double mouseX, double mouseY) {
        int count = visibleEgos().length;
        int y = cardY(height);
        int visible = 0;
        for (int slot = 0; slot < ClientEgoLoadout.SLOTS; slot++) {
            if (ClientEgoLoadout.get(slot) == null) {
                continue;
            }
            int x = cardX(visible, count, width);
            if (mouseX >= x && mouseX < x + ITEM_W && mouseY >= y && mouseY < y + ITEM_H) {
                return slot;
            }
            visible++;
        }
        return -1;
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

    // 预检：状态、资源、理智。缺哪个提示哪个（服务端还会判一次，这里省去往返）
    private static boolean checkRelease(Minecraft minecraft, Ego ego, boolean corroded) {
        if (ClientEgoState.isInState()) {
            minecraft.player.displayClientMessage(
                    Component.translatable("ego.limbusexplore.in_ego_state"), true);
            return false;
        }
        if (corroded) {
            // 侵蚀态不查资源（透支），理智也不查下限
            return true;
        }
        for (SinCost cost : ego.costs) {
            int have = ClientSinResources.get(cost.sin());
            if (have < cost.amount()) {
                minecraft.player.displayClientMessage(
                        Component.translatable("ego.limbusexplore.resource_lack",
                                Component.translatable(cost.sin().displayKey()), cost.amount(), have), true);
                return false;
            }
        }
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

        Ego[] visible = visibleEgos();
        if (visible.length == 0) {
            gui.drawCenteredString(font, Component.translatable("screen.limbusexplore.ego_empty"),
                    width / 2, height / 2, 0xFF909090);
            return;
        }

        int y0 = cardY(height);
        boolean pressing = pressSlot >= 0 && System.currentTimeMillis() - pressStart < LONG_PRESS_MS;
        int visibleIndex = 0;
        for (int slot = 0; slot < ClientEgoLoadout.SLOTS; slot++) {
            Ego ego = ClientEgoLoadout.get(slot);
            if (ego == null) {
                continue;
            }
            int x = cardX(visibleIndex, visible.length, width);
            int y = y0;
            visibleIndex++;

            // 长按中的卡片轻微抖动（只晃视觉，命中判定仍用原坐标）
            if (slot == pressSlot && pressing) {
                x += (int) (Math.random() * 3) - 1;
                y += (int) (Math.random() * 3) - 1;
            }

            boolean corroded = ClientEgoLoadout.isCorroded(slot);
            boolean fits = ClientSinResources.canPayClient(ego.costs);
            boolean sanityFits = ClientSanity.get() - ego.sanityCost >= Sanity.MIN;
            boolean hover = mouseX >= x && mouseX < x + ITEM_W && mouseY >= y && mouseY < y + ITEM_H;

            // 卡片底 + 顶条：侵蚀态整体偏红
            gui.fill(x, y, x + ITEM_W, y + ITEM_H,
                    corroded ? (hover ? 0xF05A2A2A : 0xF0441F1F) : (hover ? 0xF03A3A3A : 0xF0282828));
            gui.fill(x, y, x + ITEM_W, y + 2, corroded ? 0xFFE04B4B : ego.level.color);

            int textColor = corroded ? 0xFFFF9E9E : (fits ? 0xFFFFFF : 0xFF909090);

            // 图片（居中）+ 名称 + 等级
            gui.blit(textureOf(minecraft, ego), x + (ITEM_W - 40) / 2, y + 8, 0, 0, 40, 40, 40, 40);
            gui.drawCenteredString(font, minecraft.font.plainSubstrByWidth(
                    Component.translatable(ego.displayKey()).getString(), ITEM_W - 12), x + ITEM_W / 2, y + 52, textColor);
            gui.drawCenteredString(font, Component.translatable(ego.level.displayKey()),
                    x + ITEM_W / 2, y + 64, ego.level.color);

            // 介绍（两行截断）
            String desc = Component.translatable(ego.descKey()).getString();
            String line1 = minecraft.font.plainSubstrByWidth(desc, ITEM_W - 12);
            String line2 = desc.length() > line1.length()
                    ? minecraft.font.plainSubstrByWidth(desc.substring(line1.length()), ITEM_W - 12) : "";
            gui.drawString(font, line1, x + (ITEM_W - font.width(line1)) / 2, y + 78, 0xFFBBBBBB, false);
            if (!line2.isEmpty()) {
                gui.drawString(font, line2, x + (ITEM_W - font.width(line2)) / 2, y + 88, 0xFFBBBBBB, false);
            }

            // 所需资源（组合，每个一行）；侵蚀态按 1.5 倍向上取整显示
            int costY = y + 104;
            for (SinCost cost : ego.costs) {
                int amount = corroded ? (cost.amount() * 3 + 1) / 2 : cost.amount();
                gui.blit(cost.sin().texture(), x + 14, costY, 0, 0, 14, 14, 16, 16);
                String label = Component.translatable(cost.sin().displayKey()).getString() + " ×" + amount;
                boolean enough = !corroded && minecraft.player != null
                        && ClientSinResources.get(cost.sin()) >= cost.amount();
                gui.drawString(font, label, x + 31, costY + 1, enough ? 0xFFD8D8D8 : 0xFFE04B4B, false);
                costY += 13;
            }

            // 理智消耗（侵蚀态 1.5 倍 + 允许扣穿），不够红
            int sanityCost = corroded ? (ego.sanityCost * 3 + 1) / 2 : ego.sanityCost;
            gui.drawCenteredString(font, "理智 -" + sanityCost, x + ITEM_W / 2, costY + 1,
                    (corroded || sanityFits) ? 0xFF9FD8FF : 0xFFE04B4B);

            // 侵蚀态角标
            if (corroded) {
                gui.drawString(font, "侵蚀", x + ITEM_W - 34, y + 4, 0xFFE04B4B, false);
            }

            // 长按进度条：横贯卡片宽度，从底部往上填
            if (slot == pressSlot && pressing) {
                float progress = (System.currentTimeMillis() - pressStart) / (float) LONG_PRESS_MS;
                int barH = (int) (progress * (ITEM_H - 10));
                gui.fill(x + 2, y + ITEM_H - 5 - barH, x + ITEM_W - 2, y + ITEM_H - 5, 0xFFE8C547);
            }
        }

        gui.drawCenteredString(font, Component.translatable("screen.limbusexplore.ego_release_hint"),
                width / 2, y0 + ITEM_H + 8, 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int slot = slotAt(mouseX, mouseY);
        if (slot < 0) {
            pressSlot = -1;
            return super.mouseClicked(mouseX, mouseY, button);
        }

        if (button == 1) {
            // 右键：取消侵蚀态
            ClientEgoLoadout.setCorroded(slot, false);
            return true;
        }
        if (button == 0) {
            pressSlot = slot;
            pressStart = System.currentTimeMillis();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        int slot = pressSlot;
        pressSlot = -1;
        if (button != 0 || slot < 0 || slot != slotAt(mouseX, mouseY)) {
            return super.mouseReleased(mouseX, mouseY, button);
        }

        long duration = System.currentTimeMillis() - pressStart;
        if (duration >= LONG_PRESS_MS) {
            // 长按：切成侵蚀态
            ClientEgoLoadout.setCorroded(slot, true);
            return true;
        }

        // 短按：按当前形态释放
        Ego ego = ClientEgoLoadout.get(slot);
        if (ego != null) {
            boolean corroded = ClientEgoLoadout.isCorroded(slot);
            if (checkRelease(Minecraft.getInstance(), ego, corroded)) {
                ModNetworking.sendToServer(new EgoReleasePacket(ego.id, corroded));
            }
        }
        return true;
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
