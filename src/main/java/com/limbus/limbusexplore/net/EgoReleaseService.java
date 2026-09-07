package com.limbus.limbusexplore.net;

import com.limbus.limbusexplore.ego.Ego;
import com.limbus.limbusexplore.ego.EgoApi;
import net.minecraft.server.level.ServerPlayer;

// 释放请求的服务端处理。目前只按客户端给的 egoId 判定资源；
// 等装备数据搬到服务端后，在这里补一步「这个 EGO 确实在玩家槽位上」的校验。
public final class EgoReleaseService {

    private EgoReleaseService() {
    }

    public static void handle(ServerPlayer player, String egoId, boolean corroded) {
        Ego ego = Ego.byId(egoId);
        if (ego == null) {
            return;
        }
        EgoApi.ReleaseResult result = corroded
                ? EgoApi.releaseCorrosion(player, ego)
                : EgoApi.release(player, ego);
        ModNetworking.sendToPlayer(player, new EgoReleaseResultPacket(ego.id, result.ordinal()));
    }
}
