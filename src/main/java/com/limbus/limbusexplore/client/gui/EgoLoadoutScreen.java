package com.limbus.limbusexplore.client.gui;

import com.limbus.limbusexplore.client.ego.ClientEgoLoadout;
import com.limbus.limbusexplore.ego.Ego;
import com.limbus.limbusexplore.ego.RiskLevel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

// G 键打开：上排 5 个槽（等级固定 Z~A），下排可选 EGO 列表。
// 只读 ClientEgoLoadout，未来的服务端同步替换的是数据层，这里不动。
public class EgoLoadoutScreen extends Screen {

    private static final int PANEL_W = 384;
    private static final int PANEL_H = 232;

    private static final int SLOT_Y = 28;
    private static final int SLOT_W = 68;
    private static final int SLOT_H = 44;
    private static final int SLOT_GAP = 4;

    private static final int EGO_Y = 82;
    private static final int EGO_H = 22;

    private int panelX;
    private int panelY;
    private int selectedSlot;

    public EgoLoadoutScreen() {
        super(Component.translatable("screen.limbusexplore.ego_loadout"));
    }

    @Override
    protected void init() {
        panelX = (width - PANEL_W) / 2;
        panelY = (height - PANEL_H) / 2;
    }

    private int slotX(int slot) {
        return panelX + 12 + slot * (SLOT_W + SLOT_GAP);
    }

    private int egoY(int index) {
        return panelY + EGO_Y + index * EGO_H;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);

        // 面板
        gui.fill(panelX, panelY, panelX + PANEL_W, panelY + PANEL_H, 0xF0181818);
        gui.fill(panelX, panelY, panelX + PANEL_W, panelY + 1, 0xFF7E7E7E);
        gui.fill(panelX, panelY + PANEL_H - 1, panelX + PANEL_W, panelY + PANEL_H, 0xFF7E7E7E);
        gui.fill(panelX, panelY, panelX + 1, panelY + PANEL_H, 0xFF7E7E7E);
        gui.fill(panelX + PANEL_W - 1, panelY, panelX + PANEL_W, panelY + PANEL_H, 0xFF7E7E7E);

        // 标题
        gui.drawCenteredString(font, title, panelX + PANEL_W / 2, panelY + 8, 0xFFFFFF);

        // 装备槽（上排，等级固定 Z~A）
        for (int slot = 0; slot < ClientEgoLoadout.SLOTS; slot++) {
            int x = slotX(slot);
            int y = panelY + SLOT_Y;
            boolean selected = slot == selectedSlot;
            gui.fill(x, y, x + SLOT_W, y + SLOT_H, 0xF0262626);
            gui.fill(x, y, x + SLOT_W, y + 1, selected ? 0xFFFFFFFF : 0xFF555555);
            gui.fill(x, y + SLOT_H - 1, x + SLOT_W, y + SLOT_H, selected ? 0xFFFFFFFF : 0xFF555555);
            gui.fill(x, y, x + 1, y + SLOT_H, selected ? 0xFFFFFFFF : 0xFF555555);
            gui.fill(x + SLOT_W - 1, y, x + SLOT_W, y + SLOT_H, selected ? 0xFFFFFFFF : 0xFF555555);

            RiskLevel level = ClientEgoLoadout.levelOfSlot(slot);
            gui.drawString(font, Component.translatable(level.displayKey()), x + 4, y + 2, level.color, false);

            Ego equipped = ClientEgoLoadout.get(slot);
            if (equipped != null) {
                gui.blit(equipped.sin.texture(), x + 5, y + 19, 0, 0, 16, 16, 16, 16);
                gui.drawString(font, Component.translatable(equipped.displayKey()), x + 24, y + 23, 0xFFFFFF, false);
            } else {
                gui.drawString(font, Component.translatable("screen.limbusexplore.empty_slot"), x + 6, y + 24, 0x707070, false);
            }
        }

        // EGO 列表（下排，与选中槽等级不匹配的灰显）
        Ego[] egos = Ego.values();
        for (int i = 0; i < egos.length; i++) {
            int x = panelX + 12;
            int y = egoY(i);
            boolean hover = mouseX >= x && mouseX < x + PANEL_W - 24 && mouseY >= y && mouseY < y + EGO_H;
            boolean fits = ClientEgoLoadout.canEquip(selectedSlot, egos[i]);
            gui.fill(x, y, x + PANEL_W - 24, y + EGO_H, hover ? 0xF03A3A3A : 0xF0252525);
            gui.drawString(font, Component.translatable(egos[i].level.displayKey()), x + 4, y + 6, egos[i].level.color, false);
            gui.blit(egos[i].sin.texture(), x + 46, y + 3, 0, 0, 16, 16, 16, 16);
            gui.drawString(font, Component.translatable(egos[i].displayKey()), x + 66, y + 6,
                    fits ? 0xFFFFFF : 0xFF707070, false);
        }

        // 操作提示
        gui.drawString(font, Component.translatable("screen.limbusexplore.ego_hint"),
                panelX + 12, panelY + PANEL_H - 14, 0xFFAAAAAA, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // 槽位：左键选中，右键卸下
        for (int slot = 0; slot < ClientEgoLoadout.SLOTS; slot++) {
            int x = slotX(slot);
            int y = panelY + SLOT_Y;
            if (mouseX >= x && mouseX < x + SLOT_W && mouseY >= y && mouseY < y + SLOT_H) {
                if (button == 1) {
                    ClientEgoLoadout.unequip(slot);
                } else {
                    selectedSlot = slot;
                }
                return true;
            }
        }

        // EGO 列表：左键装备到选中槽（等级匹配才成功）
        Ego[] egos = Ego.values();
        for (int i = 0; i < egos.length; i++) {
            int x = panelX + 12;
            int y = egoY(i);
            if (mouseX >= x && mouseX < x + PANEL_W - 24 && mouseY >= y && mouseY < y + EGO_H) {
                if (button == 0) {
                    ClientEgoLoadout.equip(selectedSlot, egos[i]);
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
