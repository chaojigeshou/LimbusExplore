package com.limbus.limbusexplore.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

/** 机械外壳的绘制原语，全部使用现有 GUI 管线；不依赖截图切片或额外纹理包。 */
final class EgoUiPainter {
    static final int INK = 0xFF0C0908;
    static final int PAPER = 0xFFE2C598;
    static final int MUTED = 0xFF9D8464;
    static final int GREEN = 0xFF83A657;
    static final int RED = 0xFFDD5A43;

    record Metal(int shadow, int edge, int face, int light, int soft) {}
    static final Metal BRASS = new Metal(0xFF180E07, 0xFF55320F, 0xFF81501C, 0xFFBC8C45, 0xFF2D1B0C);
    static final Metal BLOOD = new Metal(0xFF1A0605, 0xFF5D130F, 0xFF90251D, 0xFFC94C33, 0xFF2C0D0B);

    private EgoUiPainter() {}

    static Metal metal(boolean corroded) { return corroded ? BLOOD : BRASS; }

    static void bevel(GuiGraphics gui, int x, int y, int w, int h, int cut, int color) {
        gui.fill(x, y + cut, x + w, y + h - cut, color);
        for (int row = 0; row < cut; row++) {
            int inset = cut - row;
            gui.fill(x + inset, y + row, x + w - inset, y + row + 1, color);
            gui.fill(x + inset, y + h - row - 1, x + w - inset, y + h - row, color);
        }
    }

    static void panel(GuiGraphics gui, int x, int y, int w, int h, Metal m, int fill) {
        bevel(gui, x + 2, y + 3, w, h, 5, 0xA5000000);
        bevel(gui, x, y, w, h, 5, m.shadow);
        bevel(gui, x + 1, y + 1, w - 2, h - 2, 4, m.edge);
        bevel(gui, x + 3, y + 3, w - 6, h - 6, 3, m.light);
        bevel(gui, x + 4, y + 4, w - 8, h - 8, 2, m.face);
        bevel(gui, x + 6, y + 6, w - 12, h - 12, 2, fill);
        gui.fill(x + 9, y + h - 4, x + w - 9, y + h - 3, m.shadow);
    }

    static void rivet(GuiGraphics gui, int x, int y, Metal m) {
        gui.fill(x - 1, y - 1, x + 2, y + 2, m.shadow);
        gui.fill(x, y - 1, x + 1, y, m.light);
        gui.fill(x, y, x + 1, y + 1, INK);
    }

    static void line(GuiGraphics gui, float x1, float y1, float x2, float y2, int color) {
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1))));
        for (int i = 0; i <= steps; i++) {
            int x = Math.round(x1 + (x2 - x1) * i / steps);
            int y = Math.round(y1 + (y2 - y1) * i / steps);
            gui.fill(x, y, x + 1, y + 1, color);
        }
    }

    static void disk(GuiGraphics gui, int cx, int cy, int r, int color) {
        for (int y = -r; y <= r; y++) {
            int dx = (int) Math.sqrt(r * r - y * y);
            gui.fill(cx - dx, cy + y, cx + dx + 1, cy + y + 1, color);
        }
    }

    static void arc(GuiGraphics gui, int cx, int cy, int r, double start, double end, int color) {
        int steps = Math.max(8, (int) (Math.abs(end - start) * r));
        for (int i = 0; i < steps; i++) {
            double a = start + (end - start) * i / steps;
            double b = start + (end - start) * (i + 1) / steps;
            line(gui, cx + (float) Math.cos(a) * r, cy + (float) Math.sin(a) * r,
                    cx + (float) Math.cos(b) * r, cy + (float) Math.sin(b) * r, color);
        }
    }

    static void text(GuiGraphics gui, Font font, Component text, int x, int y, int color, float scale) {
        gui.pose().pushPose();
        gui.pose().translate(x, y, 0);
        gui.pose().scale(scale, scale, 1);
        gui.drawString(font, text, 0, 0, color, false);
        gui.pose().popPose();
    }

    static void centered(GuiGraphics gui, Font font, Component text, int cx, int y, int color, float scale) {
        text(gui, font, text, cx - Math.round(font.width(text) * scale / 2), y, color, scale);
    }

    static void icon(GuiGraphics gui, ResourceLocation texture, int x, int y, int size) {
        // blit 是立即绘制；先提交批量金属背景，防止它随后覆盖这一栏的第一个图标。
        gui.flush();
        gui.blit(texture, x, y, 0, 0, size, size, size, size);
    }

    /** UV 与圆形几何一起生成，真正裁切头像，四角不会覆盖金属边框。 */
    static void circularTexture(GuiGraphics gui, ResourceLocation texture, int cx, int cy, int r, float brightness) {
        gui.flush();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // GUI 投影会翻转 Y；圆形三角扇不能沿用世界渲染的背面剔除和深度状态。
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(brightness, brightness, brightness, 1);
        Matrix4f matrix = gui.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, cx, cy, 0).uv(0.5f, 0.5f).endVertex();
        for (int i = 0; i <= 96; i++) {
            double angle = i * Math.PI * 2 / 96;
            float u = (float) Math.cos(angle);
            float v = (float) Math.sin(angle);
            buffer.vertex(matrix, cx + u * r, cy + v * r, 0).uv(0.5f + u * 0.5f, 0.5f + v * 0.5f).endVertex();
        }
        BufferUploader.drawWithShader(buffer.end());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}
