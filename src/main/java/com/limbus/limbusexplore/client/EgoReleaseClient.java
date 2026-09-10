package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.ego.Ego;
import com.limbus.limbusexplore.ego.EgoApi;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;

// 服务端判定完的回包走到这，成功失败都在这里提示
public final class EgoReleaseClient {

    private EgoReleaseClient() {
    }

    public static void onResult(String egoId, int resultOrdinal) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        Ego ego = Ego.byId(egoId);
        if (ego == null) {
            return;
        }

        EgoApi.ReleaseResult result = EgoApi.ReleaseResult.values()[resultOrdinal];
        if (result == EgoApi.ReleaseResult.CORRODED) {
            // 侵蚀释放：红色火焰粒子 + 提示 + 顶部侵蚀闪光横幅
            minecraft.player.displayClientMessage(
                    Component.translatable("ego.limbusexplore.corroded",
                            Component.translatable(ego.displayKey())), true);
            CorrosionBannerHud.flash();
            for (int i = 0; i < 14; i++) {
                double spread = minecraft.player.getRandom().nextGaussian() * 0.45;
                minecraft.player.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                        minecraft.player.getX() + spread,
                        minecraft.player.getY() + 1.4 + minecraft.player.getRandom().nextGaussian() * 0.4,
                        minecraft.player.getZ() + spread, 0, 0.15, 0);
            }
        } else if (result == EgoApi.ReleaseResult.RELEASED) {
            // 普通释放：actionbar + 一圈粒子
            minecraft.player.displayClientMessage(
                    Component.translatable("ego.limbusexplore.released",
                            Component.translatable(ego.displayKey())), true);
            for (int i = 0; i < 8; i++) {
                double spread = minecraft.player.getRandom().nextGaussian() * 0.35;
                minecraft.player.level().addParticle(ParticleTypes.END_ROD,
                        minecraft.player.getX() + spread,
                        minecraft.player.getY() + 1.6 + minecraft.player.getRandom().nextGaussian() * 0.2,
                        minecraft.player.getZ() + spread, 0, 0.1, 0);
            }
        } else if (result == EgoApi.ReleaseResult.IN_EGO_STATE) {
            minecraft.player.displayClientMessage(
                    Component.translatable("ego.limbusexplore.in_ego_state"), true);
        } else if (result == EgoApi.ReleaseResult.IN_CHAOS) {
            minecraft.player.displayClientMessage(
                    Component.translatable("ego.limbusexplore.in_chaos"), true);
        } else if (result == EgoApi.ReleaseResult.SANITY_LACK) {
            minecraft.player.displayClientMessage(
                    Component.translatable("ego.limbusexplore.sanity_lack",
                            ego.sanityCost, com.limbus.limbusexplore.client.ClientSanity.get()), true);
        } else {
            minecraft.player.displayClientMessage(
                    Component.translatable("ego.limbusexplore.sin_lack"), true);
        }
    }
}
