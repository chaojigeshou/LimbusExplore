package com.limbus.limbusexplore.preview;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.client.*;
import com.limbus.limbusexplore.client.gui.EgoReleaseLayout;
import com.limbus.limbusexplore.client.gui.EgoReleaseScreen;
import com.limbus.limbusexplore.ego.Ego;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 只在 -PegoUiPreview 的独立客户端中加载，不进入发行 JAR。
 * 使用真实 GUI 绘制与实际点击入口截图，不连接服务器，不触及 run/ 的玩家世界。
 */
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID, value = Dist.CLIENT)
public final class EgoUiPreview {
    private static boolean started;
    private static boolean ready;
    private static int frames;
    private static int stage;

    private static EgoReleaseScreen previewScreen() {
        return new EgoReleaseScreen() {
            @Override
            public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
                // 截图保持固定选中项，不受桌面鼠标恰好停在哪里影响。
                super.render(gui, -100, -100, partialTick);
            }
        };
    }

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (!Boolean.getBoolean("limbusexplore.uiPreview") || event.phase != TickEvent.Phase.END || started) return;
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof TitleScreen) || mc.getOverlay() != null) return;
        started = true;
        mc.options.languageCode = "zh_cn";
        mc.getLanguageManager().setSelected("zh_cn");
        mc.reloadResourcePacks().thenRunAsync(() -> {
            mc.options.guiScale().set(3);
            mc.options.pauseOnLostFocus = false;
            mc.getWindow().setWindowed(1600, 900);
            mc.resizeDisplay();
            ClientEgoLoadout.applySnapshot(new String[]{Ego.EMBER_WATCH.id, Ego.TEST_TETH.id,
                    Ego.TEST_HE.id, Ego.TEST_WAW.id, ""}, new String[]{Ego.EMBER_WATCH.id});
            ClientEgoLoadout.setCorroded(2, true);
            ClientEgoLoadout.setCorroded(3, true);
            ClientSinResources.set(new int[]{6, 2, 4, 2, 0, 1, 0});
            ClientSanity.set(23);
            ClientEgoState.set(0, "", false);
            ClientResistance.set(1, 1.5f, 1);
            mc.setScreen(previewScreen());
            ready = true;
        }, mc);
    }

    @SubscribeEvent
    public static void onScreenRendered(ScreenEvent.Render.Post event) {
        if (!ready || !(event.getScreen() instanceof EgoReleaseScreen screen)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getOverlay() != null || ++frames < 35) return;
        frames = 0;
        String[] names = {"ego-industrial-wide", "ego-industrial-details", "ego-industrial-narrow",
                "ego-industrial-narrow-page2", "ego-industrial-empty"};
        try {
            Path directory = Path.of(System.getProperty("limbusexplore.uiPreviewOutput"));
            Files.createDirectories(directory);
            event.getGuiGraphics().flush();
            try (NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
                image.writeToFile(directory.resolve(names[stage] + ".png"));
            }
            System.out.println("[EGO_UI_PREVIEW] captured " + names[stage]);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to capture EGO GUI preview", exception);
        }
        EgoReleaseLayout layout = new EgoReleaseLayout(screen.width, screen.height);
        switch (stage++) {
            case 0 -> {
                var info = layout.info(0, Math.min(4, layout.capacity()));
                screen.mouseClicked((info.x() + 3) * layout.scale(), (info.y() + 3) * layout.scale(), 0);
            }
            case 1 -> {
                mc.getWindow().setWindowed(960, 720);
                mc.resizeDisplay();
                mc.setScreen(previewScreen());
            }
            case 2 -> screen.mouseScrolled(0, 0, -1);
            case 3 -> {
                ClientEgoLoadout.clear();
                mc.setScreen(previewScreen());
            }
            default -> {
                ready = false;
                mc.execute(mc::stop);
            }
        }
    }
}
