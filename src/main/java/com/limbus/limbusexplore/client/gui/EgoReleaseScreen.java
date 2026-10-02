package com.limbus.limbusexplore.client.gui;

import com.limbus.limbusexplore.client.ClientChaos;
import com.limbus.limbusexplore.client.ClientEgoLoadout;
import com.limbus.limbusexplore.client.ClientEgoState;
import com.limbus.limbusexplore.client.ClientSanity;
import com.limbus.limbusexplore.client.ClientSinResources;
import com.limbus.limbusexplore.ego.Ego;
import com.limbus.limbusexplore.ego.SinCost;
import com.limbus.limbusexplore.net.EgoReleasePacket;
import com.limbus.limbusexplore.net.ModNetworking;
import com.limbus.limbusexplore.sanity.Sanity;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

import static com.limbus.limbusexplore.client.gui.EgoUiPainter.*;

/** R 键释放界面：工业档案卡。外观与布局可替换，仍只向服务端发送既有释放请求。 */
public class EgoReleaseScreen extends Screen {
    private final EgoReleaseGesture gesture = new EgoReleaseGesture();
    private EgoReleaseLayout layout;
    private EgoCardRenderer renderer;
    private Ego pressedEgo;
    private Ego detailsEgo;
    private int page;
    private int selectedSlot = -1;

    public EgoReleaseScreen() {
        super(Component.translatable("screen.limbusexplore.ego_release"));
    }

    @Override
    protected void init() {
        layout = new EgoReleaseLayout(width, height);
        renderer = new EgoCardRenderer(font);
        gesture.cancel();
    }

    private Ego[] visibleEgos() {
        Ego[] all = ClientEgoLoadout.equippedList();
        page = Math.max(0, Math.min(page, layout.pageCount(all.length) - 1));
        int from = page * layout.capacity();
        return Arrays.copyOfRange(all, from, Math.min(all.length, from + layout.capacity()));
    }

    private int indexAt(double x, double y, Ego[] visible) {
        for (int i = 0; i < visible.length; i++) {
            if (layout.card(i, visible.length).contains(x, y)) return i;
        }
        return -1;
    }

    private int slotAt(double x, double y) {
        Ego[] visible = visibleEgos();
        int index = indexAt(x, y, visible);
        return index < 0 ? -1 : visible[index].level.ordinal();
    }

    @Override
    @SuppressWarnings("deprecation") // 1.20.1 的批处理入口；避免圆环/铆钉的每个像素单独 flush。
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui);
        gui.drawManaged(() -> renderArchive(gui, mouseX, mouseY));
    }

    private void renderArchive(GuiGraphics gui, int mouseX, int mouseY) {
        gui.fill(0, 0, width, height, 0xC0000000);
        gui.fillGradient(0, 0, width, height / 2, 0xDD000000, 0x08000000);
        gui.fillGradient(0, height * 2 / 3, width, height, 0x08000000, 0xEC000000);
        double mx = layout.logical(mouseX), my = layout.logical(mouseY);
        Ego[] visible = visibleEgos();
        int hover = indexAt(mx, my, visible);
        if (detailsEgo == null && hover >= 0) selectedSlot = visible[hover].level.ordinal();
        if (visible.length > 0 && Arrays.stream(visible).noneMatch(ego -> ego.level.ordinal() == selectedSlot)) {
            selectedSlot = visible[0].level.ordinal();
        }

        // 切换装备或打开详情后，旧的按下动作不能误释放新的同等级条目。
        if (gesture.slot() >= 0 && ClientEgoLoadout.get(gesture.slot()) != pressedEgo) gesture.cancel();
        int held = detailsEgo == null ? gesture.poll(Util.getMillis(), slotAt(mx, my)) : -1;
        if (held >= 0) ClientEgoLoadout.setCorroded(held, true);

        gui.pose().pushPose();
        gui.pose().scale(layout.scale(), layout.scale(), 1);
        int vw = layout.viewWidth();
        text(gui, font, Component.literal("E.G.O  /  RELEASE ARCHIVE"), 14, 12, BRASS.light(), 0.7f);
        text(gui, font, EgoCardRenderer.label("header"), 14, 25, MUTED, 0.63f);
        Component sanity = EgoCardRenderer.label("sanity", ClientSanity.get());
        text(gui, font, sanity, vw - Math.round(font.width(sanity) * 0.7f) - 14, 14,
                ClientSanity.get() < 0 ? RED : PAPER, 0.7f);
        gui.fill(14, 35, vw - 14, 36, 0x804B3019);

        // 细弧线只作侵蚀状态的背景呼应，不盖住卡面或加入新战斗机制。
        for (int i = 0; i < visible.length; i++) {
            if (ClientEgoLoadout.isCorroded(visible[i].level.ordinal())) {
                int cx = layout.card(i, visible.length).x() + 66;
                arc(gui, cx - 32, 135, 111, Math.PI * 1.08, Math.PI * 1.87, 0x7047140F);
            }
        }

        if (visible.length == 0) {
            centered(gui, font, Component.translatable("screen.limbusexplore.ego_empty"),
                    vw / 2, 158, PAPER, 1f);
            centered(gui, font, EgoCardRenderer.label("equip_first"), vw / 2, 179, MUTED, 0.85f);
        } else {
            for (int i = 0; i < visible.length; i++) {
                int slot = visible[i].level.ordinal();
                renderer.card(gui, layout.card(i, visible.length), visible[i], slot == selectedSlot,
                        detailsEgo == null && layout.info(i, visible.length).contains(mx, my),
                        gesture.slot() == slot ? gesture.progress(Util.getMillis()) : 0);
            }
            renderer.footer(gui, layout, ClientEgoLoadout.get(selectedSlot));
        }
        renderer.resources(gui, layout);
        int pages = layout.pageCount(ClientEgoLoadout.equippedList().length);
        if (pages > 1) {
            pageButton(gui, layout.previousPage(), "‹", page > 0, mx, my);
            pageButton(gui, layout.nextPage(), "›", page + 1 < pages, mx, my);
            centered(gui, font, Component.literal((page + 1) + " / " + pages), vw / 2, 339, PAPER, 0.8f);
        }
        Component hint = EgoCardRenderer.label("controls");
        centered(gui, font, hint, vw / 2, pages > 1 ? 354 : 343, MUTED,
                Math.min(0.64f, (vw - 20) / (float) Math.max(1, font.width(hint))));
        if (detailsEgo != null) {
            gui.flush(); // 弹窗遮罩必须在下层卡牌/文字已经提交之后绘制。
            renderer.details(gui, layout, detailsEgo);
        }
        gui.pose().popPose();
    }

    private void pageButton(GuiGraphics gui, EgoReleaseLayout.Box box, String caption, boolean enabled, double mx, double my) {
        bevel(gui, box.x(), box.y(), box.w(), box.h(), 2,
                enabled && box.contains(mx, my) ? BRASS.face() : BRASS.soft());
        centered(gui, font, Component.literal(caption), box.x() + box.w() / 2, box.y() + 3,
                enabled ? PAPER : MUTED, 1f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double x = layout.logical(mouseX), y = layout.logical(mouseY);
        if (detailsEgo != null) {
            if (button == 0 && (layout.closeDetails().contains(x, y) || !layout.details().contains(x, y))) {
                detailsEgo = null;
            }
            return true; // 详情窗不会把点击传给下层卡牌。
        }
        if (button == 0 && layout.pageCount(ClientEgoLoadout.equippedList().length) > 1) {
            if (layout.previousPage().contains(x, y)) { changePage(-1); return true; }
            if (layout.nextPage().contains(x, y)) { changePage(1); return true; }
        }
        Ego[] visible = visibleEgos();
        int index = indexAt(x, y, visible);
        gesture.cancel();
        if (index < 0) return super.mouseClicked(mouseX, mouseY, button);
        Ego ego = visible[index];
        selectedSlot = ego.level.ordinal();
        if (button == 0 && layout.info(index, visible.length).contains(x, y)) {
            detailsEgo = ego;
        } else if (button == 1) {
            ClientEgoLoadout.setCorroded(selectedSlot, false);
        } else if (button == 0) {
            pressedEgo = ego;
            gesture.press(selectedSlot, Util.getMillis());
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button != 0 || detailsEgo != null) return true;
        int slot = gesture.slot();
        Ego current = ClientEgoLoadout.get(slot);
        EgoReleaseGesture.Action action = gesture.release(slotAt(layout.logical(mouseX), layout.logical(mouseY)), Util.getMillis());
        if (current == null || current != pressedEgo) return true;
        if (action == EgoReleaseGesture.Action.CORRODE) ClientEgoLoadout.setCorroded(slot, true);
        if (action == EgoReleaseGesture.Action.RELEASE) {
            boolean corroded = ClientEgoLoadout.isCorroded(slot);
            if (checkRelease(current, corroded)) {
                ModNetworking.sendToServer(new EgoReleasePacket(current.id, corroded));
                onClose();
            }
        }
        return true;
    }

    private boolean checkRelease(Ego ego, boolean corroded) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return false;
        Component error = null;
        if (ClientChaos.isInChaos()) error = Component.translatable("ego.limbusexplore.in_chaos");
        else if (ClientEgoState.isInState()) error = Component.translatable("ego.limbusexplore.in_ego_state");
        else if (!corroded) {
            for (SinCost entry : ego.costs()) {
                if (ClientSinResources.get(entry.sin()) < entry.amount()) {
                    error = Component.translatable("ego.limbusexplore.resource_lack",
                            Component.translatable(entry.sin().displayKey()), entry.amount(), ClientSinResources.get(entry.sin()));
                    break;
                }
            }
            if (error == null && (long) ClientSanity.get() - ego.sanityCost < Sanity.MIN) {
                error = Component.translatable("ego.limbusexplore.sanity_lack", ego.sanityCost, ClientSanity.get());
            }
        }
        if (error != null) mc.player.displayClientMessage(error, true);
        return error == null;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (detailsEgo != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_R) detailsEgo = null;
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT) {
            changePage(keyCode == GLFW.GLFW_KEY_RIGHT ? 1 : -1);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_R) { onClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void changePage(int delta) {
        gesture.cancel();
        page = Math.max(0, Math.min(layout.pageCount(ClientEgoLoadout.equippedList().length) - 1, page + delta));
        Ego[] visible = visibleEgos();
        selectedSlot = visible.length == 0 ? -1 : visible[0].level.ordinal();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (detailsEgo == null && delta != 0) changePage(delta < 0 ? 1 : -1);
        return true;
    }
}
