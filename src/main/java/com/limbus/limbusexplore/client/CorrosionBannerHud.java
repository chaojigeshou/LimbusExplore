package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.LimbusExplore;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * 侵蚀全屏渲染：Voronoi(Worley) 侵蚀着色器。
 * 一瞬间（release 后 1.4s）：Mode=1，龟裂爆闪 + 中心脉冲，淡入淡出；
 * 持续（EGO 状态 30s）：Mode=0，细胞流动 + 边界呼吸闪烁。
 * shader 加载失败时回退到贴图 blit 全屏。
 */
public final class CorrosionBannerHud {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation FLASH =
            new ResourceLocation(LimbusExplore.MODID, "textures/hud/corrosion_flash.png");
    private static final ResourceLocation BAR =
            new ResourceLocation(LimbusExplore.MODID, "textures/hud/corrosion.png");

    // ShaderInstance 只认 minecraft:shaders/core/ 命名空间（硬编码），
    // 所以 shader 文件放在 assets/minecraft/shaders/core/ 下，加载用裸名即可。
    private static final String SHADER_NAME = "corrosion_erosion";

    private static final long FLASH_TOTAL = 1400;
    private static final long FADE_IN = 700;
    private static final long FADE_OUT = 700;

    private static long flashStart = -1;
    private static ShaderInstance shader;
    private static boolean shaderFailed;

    private CorrosionBannerHud() {
    }

    /** 侵蚀释放回包到达时调用，开始闪光。 */
    public static void flash() {
        flashStart = System.currentTimeMillis();
    }

    private static ShaderInstance shader() {
        if (shader == null && !shaderFailed) {
            try {
                shader = new ShaderInstance(Minecraft.getInstance().getResourceManager(),
                        SHADER_NAME, DefaultVertexFormat.POSITION);
            } catch (IOException e) {
                shaderFailed = true;
                LOGGER.error("Failed to load corrosion shader, falling back to texture blit", e);
            }
        }
        return shader;
    }

    // 贴图全屏（shader 不可用时的回退）
    private static void blitFull(GuiGraphics graphics, ResourceLocation texture, int screenWidth, int screenHeight) {
        graphics.fill(0, 0, screenWidth, screenHeight, 0x40000000);
        graphics.blit(texture, 0, 0, 0, 0, screenWidth, screenHeight, screenWidth, screenHeight);
    }

    public static void render(ForgeGui gui, GuiGraphics graphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        boolean flashActive = flashStart >= 0 && System.currentTimeMillis() - flashStart < FLASH_TOTAL;
        boolean barActive = ClientEgoState.isInState() && ClientEgoState.isCorroded();

        if (!flashActive && !barActive) {
            if (flashStart >= 0) {
                flashStart = -1;
            }
            return;
        }

        // 计算 uniforms
        float mode = flashActive ? 1f : 0f;
        float time;
        float intensity = 1f;
        if (flashActive) {
            float t = System.currentTimeMillis() - flashStart;
            if (t < FADE_IN) {
                intensity = t / (float) FADE_IN;
            } else if (t > FLASH_TOTAL - FADE_OUT) {
                intensity = (FLASH_TOTAL - t) / (float) FADE_OUT;
            } else {
                intensity = 1f;
            }
            time = t / 1000f;
        } else {
            time = 30f - ClientEgoState.remainingSeconds();
        }

        int flashTex = minecraft.getTextureManager().getTexture(FLASH).getId();
        int barTex = minecraft.getTextureManager().getTexture(BAR).getId();

        ShaderInstance s = shader();
        if (s == null) {
            // 回退：老的全屏贴图
            if (flashActive) {
                RenderSystem.setShaderColor(1f, 1f, 1f, intensity);
                blitFull(graphics, FLASH, screenWidth, screenHeight);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }
            if (barActive) {
                blitFull(graphics, BAR, screenWidth, screenHeight);
            }
            return;
        }

        // 上传 uniforms + sampler 单元，apply 应用 blend 并上传
        s.getUniform("Time").set(time);
        s.getUniform("Intensity").set(intensity);
        s.getUniform("Mode").set(mode);
        // 三维柏林噪声权重跟 EGO 类型走（activeEgo 的 noiseR/G/B）
        com.limbus.limbusexplore.ego.Ego activeEgo = ClientEgoState.activeEgo();
        s.getUniform("Weight").set(
                activeEgo != null ? activeEgo.noiseR : 0.5f,
                activeEgo != null ? activeEgo.noiseG : 0.5f,
                activeEgo != null ? activeEgo.noiseB : 0.5f);
        s.setSampler("SamplerFlash", 0);
        s.setSampler("SamplerBar", 1);
        RenderSystem.setShader(() -> s);
        s.apply();
        RenderSystem.setShaderTexture(0, FLASH);
        RenderSystem.setShaderTexture(1, BAR);

        // 全屏 quad（顶点 -1..1，vsh 负责转 UV 和裁剪坐标）
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        buffer.vertex(-1.0, -1.0, 0.0).endVertex();
        buffer.vertex(1.0, -1.0, 0.0).endVertex();
        buffer.vertex(1.0, 1.0, 0.0).endVertex();
        buffer.vertex(-1.0, 1.0, 0.0).endVertex();
        BufferUploader.drawWithShader(buffer.end());
    }
}
