package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.config.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * 混乱图渲染：混乱中的生物头顶浮一张"陷入混乱"（billboard，永远面向玩家）。
 * 数据来自 ClientEntityChaos（服务端广播），最后 1 秒淡出。
 */
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID, value = Dist.CLIENT)
public final class ChaosMarkRenderer {

    private static final ResourceLocation MARK =
            new ResourceLocation(LimbusExplore.MODID, "textures/entity/chaos_mark.png");

    /** 图的世界尺寸（方块）与头顶抬高 */
    private static final float SIZE = 1.3f;
    private static final float ABOVE = 0.55f;

    private ChaosMarkRenderer() {
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        // 官方绕过入口：客户端配置里能关掉这张图
        if (!ModConfig.CHAOS_MARK_ENABLED.get()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        RenderType type = RenderType.entityTranslucentEmissive(MARK);
        boolean drew = false;

        for (Entity entity : minecraft.level.entitiesForRendering()) {
            int ticks = ClientEntityChaos.ticks(entity.getId());
            if (ticks <= 0) {
                continue;
            }
            float alpha = ticks >= 20 ? 1f : ticks / 20f;
            int a = (int) (Math.max(0f, Math.min(1f, alpha)) * 255);

            pose.pushPose();
            pose.translate(entity.getX() - cam.x,
                    entity.getY() + entity.getBbHeight() + ABOVE - cam.y,
                    entity.getZ() - cam.z);
            // billboard：先对齐相机朝向（图始终正对玩家），再翻 180° 让正面朝外
            pose.mulPose(event.getCamera().rotation());
            pose.mulPose(Axis.YP.rotationDegrees(180f));

            Matrix4f matrix = pose.last().pose();
            VertexConsumer vc = buffer.getBuffer(type);
            float h = SIZE / 2f;
            vc.vertex(matrix, -h, -h, 0f).color(255, 255, 255, a).uv(0f, 1f)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                    .normal(0f, 0f, 1f).endVertex();
            vc.vertex(matrix, h, -h, 0f).color(255, 255, 255, a).uv(1f, 1f)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                    .normal(0f, 0f, 1f).endVertex();
            vc.vertex(matrix, h, h, 0f).color(255, 255, 255, a).uv(1f, 0f)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                    .normal(0f, 0f, 1f).endVertex();
            vc.vertex(matrix, -h, h, 0f).color(255, 255, 255, a).uv(0f, 0f)
                    .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                    .normal(0f, 0f, 1f).endVertex();
            pose.popPose();
            drew = true;
        }

        if (drew) {
            buffer.endBatch(type);
        }
    }
}
