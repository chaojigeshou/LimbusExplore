package com.limbus.limbusexplore.client.gui;

import com.limbus.limbusexplore.client.ClientChaos;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 混乱锁定界面：混乱 15 秒期间强制打开，吞掉所有输入（含 ESC），
 * 玩家在里面做不了任何事（不能移动/攻击/开背包/开 E.G.O 界面）。
 * 混乱结束由 ClientGameEvents 关闭。
 */
public class ChaosLockScreen extends Screen {

    public ChaosLockScreen() {
        super(Component.translatable("screen.limbusexplore.chaos_lock"));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        // 整屏压暗，留一点世界可见
        gui.fill(0, 0, width, height, 0xC0100810);

        int centerY = height / 2;
        gui.drawCenteredString(font, Component.translatable("screen.limbusexplore.chaos_lock"),
                width / 2, centerY - 30, 0xFFE04B4B);
        gui.drawCenteredString(font,
                Component.translatable("screen.limbusexplore.chaos_lock_countdown", ClientChaos.remainingSeconds()),
                width / 2, centerY, 0xFFE8C547);
        gui.drawCenteredString(font, Component.translatable("screen.limbusexplore.chaos_lock_hint"),
                width / 2, centerY + 24, 0xFFAAAAAA);
    }

    // 所有输入一律吞掉
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return true;
    }
}
