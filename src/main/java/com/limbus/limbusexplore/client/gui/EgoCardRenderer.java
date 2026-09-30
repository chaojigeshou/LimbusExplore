package com.limbus.limbusexplore.client.gui;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.client.ClientEgoLoadout;
import com.limbus.limbusexplore.client.ClientResistance;
import com.limbus.limbusexplore.client.ClientSanity;
import com.limbus.limbusexplore.client.ClientSinResources;
import com.limbus.limbusexplore.combat.DamageKind;
import com.limbus.limbusexplore.ego.Ego;
import com.limbus.limbusexplore.ego.EgoState;
import com.limbus.limbusexplore.ego.SinCost;
import com.limbus.limbusexplore.sanity.Sanity;
import com.limbus.limbusexplore.sin.SinType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.limbus.limbusexplore.client.gui.EgoUiPainter.*;

/** 纯客户端呈现：从真实定义与镜像读取，不制造侵蚀概率、硬币、攻击容量等不存在的数据。 */
final class EgoCardRenderer {
    private static final ResourceLocation SANITY_ICON =
            new ResourceLocation(LimbusExplore.MODID, "textures/hud/sanity_pos.png");
    private final Font font;
    // 跟随 Screen 生命周期；F3+T 后重新打开界面即可识别新增立绘，不再需要重启游戏。
    private final Map<String, Boolean> portraits = new HashMap<>();

    EgoCardRenderer(Font font) { this.font = font; }

    static Component label(String key, Object... args) {
        return Component.translatable("screen.limbusexplore.industrial." + key, args);
    }

    static int cost(int base, boolean corroded) {
        return corroded ? (int) (((long) base * 3 + 1) / 2) : base;
    }

    static EnumMap<SinType, Integer> costs(Ego ego, boolean corroded) {
        EnumMap<SinType, Integer> values = new EnumMap<>(SinType.class);
        for (SinCost entry : ego.costs()) values.merge(entry.sin(), cost(entry.amount(), corroded), Integer::sum);
        return values;
    }

    private ResourceLocation damageIcon(DamageKind kind) {
        return new ResourceLocation(LimbusExplore.MODID, "textures/hud/damage_" + kind.id + ".png");
    }

    void card(GuiGraphics gui, EgoReleaseLayout.Box box, Ego ego, boolean selected,
              boolean infoHover, float heldProgress) {
        int x = box.x(), y = box.y();
        boolean corroded = ClientEgoLoadout.isCorroded(ego.level.ordinal());
        Metal m = metal(corroded);
        boolean affordable = ClientSinResources.canPayClient(ego.costs())
                && (long) ClientSanity.get() - ego.sanityCost >= Sanity.MIN;

        // 分层切角外壳、左上折耳、中央半圆风险铭牌。
        if (selected) bevel(gui, x - 2, y + 15, box.w() + 4, box.h() - 13, 6, m.light());
        panel(gui, x, y + 17, box.w(), box.h() - 17, m, 0xFF100B08);
        bevel(gui, x, y, 43, 23, 3, m.edge());
        bevel(gui, x + 2, y + 2, 38, 19, 2, INK);
        line(gui, x + 36, y + 1, x + 53, y + 17, m.light());
        line(gui, x + 38, y + 5, x + 51, y + 18, m.face());
        disk(gui, x + 68, y + 18, 14, m.edge());
        disk(gui, x + 68, y + 18, 12, INK);
        arc(gui, x + 68, y + 18, 10, Math.PI, Math.PI * 2, m.light());
        centered(gui, font, Component.literal(ego.level.id.toUpperCase(Locale.ROOT)),
                x + 68, y + 12, m.light(), 0.56f);
        icon(gui, SANITY_ICON, x + 7, y + 5, 9);
        text(gui, font, Component.literal(Integer.toString(cost(ego.sanityCost, corroded))),
                x + 19, y + 5, PAPER, 0.95f);

        bevel(gui, x + 84, y + 2, 44, 17, 2, m.light());
        bevel(gui, x + 85, y + 3, 42, 15, 1, infoHover ? m.soft() : INK);
        gui.fill(x + 87, y + 4, x + 125, y + 5, PAPER);
        centered(gui, font, label("more"), x + 106, y + 6, PAPER, 0.75f);
        gui.fill(x + 6, y + 21, x + 126, y + 22, m.face());

        bevel(gui, x + 8, y + 25, 96, 12, 2, m.edge());
        bevel(gui, x + 9, y + 26, 94, 10, 1, INK);
        text(gui, font, Component.literal(corroded ? "!" : ">"), x + 13, y + 27,
                corroded ? RED : GREEN, 0.8f);
        text(gui, font, label(corroded ? "corrosion_mode" : "awakening_mode"),
                x + 22, y + 27, corroded ? RED : GREEN, 0.72f);

        portrait(gui, ego, x + 57, y + 92, 47, m, corroded || affordable ? 1f : 0.55f);

        // 原作式右侧七罪孽竖列，零费用也保留位置；不足标红但不伪造禁止侵蚀。
        // 费用栏使用薄框，不能套用主卡片的 6px 内缩，否则数字挤到金属边上。
        bevel(gui, x + 105, y + 39, 25, 119, 4, m.edge());
        bevel(gui, x + 106, y + 40, 23, 117, 3, m.light());
        bevel(gui, x + 107, y + 41, 21, 115, 2, m.face());
        bevel(gui, x + 108, y + 42, 19, 113, 2, INK);
        EnumMap<SinType, Integer> fees = costs(ego, corroded);
        int row = 0;
        for (SinType sin : SinType.values()) {
            int cy = y + 45 + row++ * 15;
            int fee = fees.getOrDefault(sin, 0);
            icon(gui, sin.texture(), x + 109, cy, 10);
            int color = fee == 0 ? 0xFF756653 : ClientSinResources.get(sin) < fee ? RED : PAPER;
            text(gui, font, Component.literal(Integer.toString(fee)), x + 121, cy + 1, color, 0.7f);
        }

        bevel(gui, x + 13, y + 143, 91, 14, 2, m.face());
        bevel(gui, x + 14, y + 144, 89, 12, 1, INK);
        String name = font.plainSubstrByWidth(Component.translatable(ego.displayKey()).getString(), 106);
        centered(gui, font, Component.literal(name), x + 58, y + 146, PAPER, 0.8f);

        // 数值取现有 30 秒状态和抗性覆盖，不借用原作硬币/等级的虚假统计。
        gui.fill(x + 9, y + 164, x + 123, y + 165, m.edge());
        text(gui, font, Component.literal((EgoState.DURATION_MS / 1000) + "s"), x + 16, y + 171, PAPER, 1.25f);
        icon(gui, damageIcon(ego.resistanceKind), x + 70, y + 170, 13);
        text(gui, font, Component.literal("×" + rate(ego.resistanceRate)), x + 86, y + 172, m.light(), 0.92f);
        text(gui, font, label("duration"), x + 16, y + 186, MUTED, 0.62f);
        text(gui, font, Component.translatable(ego.resistanceKind.displayKey()), x + 70, y + 186, MUTED, 0.62f);

        gui.fill(x + 10, y + 197, x + 122, y + 198, m.edge());
        text(gui, font, label(corroded ? "corrosion" : "awakening"), x + 13, y + 202,
                corroded ? RED : GREEN, 0.66f);
        Component skill = Component.translatable(corroded ? ego.corrosionKey() : ego.awakeningKey());
        wrapped(gui, skill, x + 13, y + 212, 106, 2, PAPER, 0.69f);
        for (int rivetY : new int[]{43, 160, 194, 226}) {
            rivet(gui, x + 5, y + rivetY, m);
            rivet(gui, x + 126, y + rivetY, m);
        }
        for (int i = 0; i < 13; i++) {
            // 确定性浅刮痕，不使用逐帧随机噪声造成闪烁。
            int sx = x + 10 + (i * 29 + ego.id.hashCode() & 0x7fffffff) % 109;
            int sy = y + 22 + (i * 47) % 207;
            gui.fill(sx, sy, sx + 2, sy + 1, 0x146D543A);
        }
        if (!corroded && !affordable) {
            centered(gui, font, label("insufficient"), x + 58, y + 127, RED, 0.6f);
        }
        if (heldProgress > 0) {
            gui.fill(x + 7, y + 231, x + 125, y + 233, 0xFF260C08);
            gui.fill(x + 7, y + 231, x + 7 + (int) (118 * heldProgress), y + 233, RED);
            arc(gui, x + 57, y + 92, 49, -Math.PI / 2,
                    -Math.PI / 2 + Math.PI * 2 * heldProgress, RED);
        }
    }

    private void portrait(GuiGraphics gui, Ego ego, int cx, int cy, int radius, Metal m, float brightness) {
        disk(gui, cx, cy, radius + 2, m.shadow());
        disk(gui, cx, cy, radius + 1, m.light());
        disk(gui, cx, cy, radius, m.face());
        disk(gui, cx, cy, radius - 2, INK);
        boolean hasArt = portraits.computeIfAbsent(ego.id,
                id -> Minecraft.getInstance().getResourceManager().getResource(ego.texture()).isPresent());
        if (hasArt) {
            circularTexture(gui, ego.texture(), cx, cy, radius - 3, brightness);
        } else {
            // 专属立绘缺省时是明确的“档案徽记”，不是把罪孽图标拉伸成一张假立绘。
            for (int i = 0; i < 24; i++) {
                double a = i * Math.PI / 12;
                line(gui, cx + (float) Math.cos(a) * 16, cy + (float) Math.sin(a) * 16,
                        cx + (float) Math.cos(a) * (radius - 5), cy + (float) Math.sin(a) * (radius - 5),
                        i % 3 == 0 ? 0xFF322A20 : 0xFF1E1A16);
            }
            for (int i = -5; i <= 5; i++) {
                int sx = cx + i * 6;
                int roof = cy + 12 - Math.floorMod(ego.id.hashCode() + i * 17, 23);
                int bottom = cy + (int) Math.sqrt((radius - 6) * (radius - 6) - i * i * 36);
                gui.fill(sx, roof, sx + 4, bottom, 0xFF191917);
                gui.fill(sx + 1, roof + 4, sx + 2, roof + 6, 0xFF4D4431);
            }
            disk(gui, cx, cy - 2, 20, m.soft());
            disk(gui, cx, cy - 2, 18, INK);
            circularTexture(gui, ego.sin.texture(), cx, cy - 2, 15, brightness * 0.88f);
            centered(gui, font, label("archive"), cx, cy + 25, MUTED, 0.58f);
        }
        arc(gui, cx, cy, radius - 1, Math.PI * 1.08, Math.PI * 1.72, m.light());
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            rivet(gui, cx + (int) (Math.cos(a) * radius), cy + (int) (Math.sin(a) * radius), m);
        }
    }

    void resources(GuiGraphics gui, EgoReleaseLayout layout) {
        if (!layout.railVisible()) return;
        EgoReleaseLayout.Box box = layout.rail();
        panel(gui, box.x(), box.y(), box.w(), box.h(), BRASS, 0xEE100B08);
        centered(gui, font, label("resources"), box.x() + 18, box.y() - 13, MUTED, 0.72f);
        int i = 0;
        for (SinType sin : SinType.values()) {
            int y = box.y() + 10 + i++ * 25;
            icon(gui, sin.texture(), box.x() + 6, y, 13);
            int value = ClientSinResources.get(sin);
            String display = value > 999 ? "999+" : value < -99 ? "-99+" : Integer.toString(value);
            text(gui, font, Component.literal(display), box.x() + 20, y + 2, value < 0 ? RED : PAPER, 0.82f);
            gui.fill(box.x() + 7, y + 20, box.x() + 29, y + 21, BRASS.soft());
        }
    }

    void footer(GuiGraphics gui, EgoReleaseLayout layout, Ego selected) {
        if (selected == null) return;
        int x = Math.max(12, (layout.viewWidth() - 468) / 2);
        int y = EgoReleaseLayout.FOOTER_Y;
        Metal m = metal(ClientEgoLoadout.isCorroded(selected.level.ordinal()));
        panel(gui, x, y, 34, 34, m, INK);
        circularTexture(gui, selected.sin.texture(), x + 17, y + 17, 10, 0.95f);
        text(gui, font, label("resistance"), x + 42, y + 1, m.light(), 0.8f);
        text(gui, font, Component.translatable(selected.level.displayKey()), x + 42, y + 14, selected.level.color, 0.75f);
        text(gui, font, Component.translatable(selected.displayKey()), x + 42, y + 25, PAPER, 0.7f);

        int dataX = x + 159;
        int room = layout.viewWidth() - dataX - 12;
        int gap = Math.min(96, Math.max(54, room / 3));
        for (DamageKind kind : DamageKind.values()) {
            icon(gui, damageIcon(kind), dataX, y + 4, 15);
            float current = ClientResistance.get(kind);
            float next = kind == selected.resistanceKind ? selected.resistanceRate : current;
            text(gui, font, Component.literal(rate(current)), dataX + 19, y + 3, PAPER, 0.95f);
            text(gui, font, Component.literal("→ " + rate(next)), dataX + 19, y + 16,
                    next < current ? GREEN : next > current ? RED : MUTED, 0.8f);
            text(gui, font, Component.translatable(kind.displayKey()), dataX + 1, y + 29, MUTED, 0.56f);
            dataX += gap;
        }
    }

    void details(GuiGraphics gui, EgoReleaseLayout layout, Ego ego) {
        EgoReleaseLayout.Box box = layout.details();
        int x = box.x(), y = box.y(), w = box.w();
        Metal m = metal(ClientEgoLoadout.isCorroded(ego.level.ordinal()));
        gui.fill(0, 0, layout.viewWidth(), EgoReleaseLayout.VIEW_H, 0xD9000000);
        panel(gui, x, y, w, box.h(), m, 0xFF110C09);
        text(gui, font, label("dossier"), x + 15, y + 13, m.light(), 0.75f);
        text(gui, font, Component.translatable(ego.displayKey()), x + 15, y + 29, PAPER, 1.15f);
        text(gui, font, Component.translatable(ego.level.displayKey()), x + 15, y + 46, ego.level.color, 0.8f);
        gui.fill(x + 14, y + 62, x + w - 14, y + 63, m.face());
        EgoReleaseLayout.Box close = layout.closeDetails();
        bevel(gui, close.x(), close.y(), close.w(), close.h(), 2, m.edge());
        centered(gui, font, Component.literal("×"), close.x() + close.w() / 2, close.y() + 3, PAPER, 1f);
        int textY = y + 75;
        for (String[] section : new String[][]{
                {"awakening", ego.awakeningKey()}, {"corrosion", ego.corrosionKey()}, {"passive", ego.passiveKey()}}) {
            text(gui, font, label(section[0]), x + 16, textY, section[0].equals("corrosion") ? RED : GREEN, 0.85f);
            wrapped(gui, Component.translatable(section[1]), x + 16, textY + 14, w - 35, 3, PAPER, 0.9f);
            textY += 51;
        }
        gui.fill(x + 15, y + 237, x + w - 15, y + 238, m.edge());
        wrapped(gui, label("details_note"), x + 16, y + 248, w - 33, 2, MUTED, 0.72f);
    }

    private void wrapped(GuiGraphics gui, Component text, int x, int y, int width, int maxLines, int color, float scale) {
        var lines = font.split(text, (int) (width / scale));
        gui.pose().pushPose();
        gui.pose().translate(x, y, 0);
        gui.pose().scale(scale, scale, 1);
        for (int i = 0; i < Math.min(lines.size(), maxLines); i++) {
            gui.drawString(font, lines.get(i), 0, i * 11, color, false);
        }
        gui.pose().popPose();
    }

    private static String rate(float value) { return String.format(Locale.ROOT, "%.2f", value); }
}
