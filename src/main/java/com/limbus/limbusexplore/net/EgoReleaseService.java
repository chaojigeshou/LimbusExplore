package com.limbus.limbusexplore.net;

import com.limbus.limbusexplore.ego.Ego;
import com.limbus.limbusexplore.ego.EgoApi;
import com.limbus.limbusexplore.ego.EgoLoadoutApi;
import net.minecraft.server.level.ServerPlayer;

// 释放请求的服务端处理。EgoApi 在扣资源前验证服务端装备，不能靠伪造 egoId 绕过。
public final class EgoReleaseService {

    private EgoReleaseService() {
    }

    public static void handle(ServerPlayer player, String egoId, boolean corroded) {
        if (!player.isAlive()) {
            return;
        }
        Ego ego = Ego.byId(egoId);
        if (ego == null) {
            return;
        }
        EgoApi.ReleaseResult result = corroded
                ? EgoApi.releaseCorrosion(player, ego)
                : EgoApi.release(player, ego);
        if (result == EgoApi.ReleaseResult.NOT_EQUIPPED) {
            EgoLoadoutApi.sync(player);
        }
        ModNetworking.sendToPlayer(player, new EgoReleaseResultPacket(ego.id, result.ordinal()));
    }
}
