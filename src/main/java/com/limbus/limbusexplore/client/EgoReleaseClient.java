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
        if (result == EgoApi.ReleaseResult.RELEASED) {
            // 先给个反馈：actionbar + 一圈粒子，正式特效后面替换
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
