package com.limbus.limbusexplore.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// C2S：客户端释放请求。egoId + corroded（长按切过侵蚀态就是 true）。
// 判定全在服务端，结果回 EgoReleaseResultPacket。
public class EgoReleasePacket {

    private final String egoId;
    private final boolean corroded;

    public EgoReleasePacket(String egoId, boolean corroded) {
        this.egoId = egoId;
        this.corroded = corroded;
    }

    public static void encode(EgoReleasePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.egoId);
        buffer.writeBoolean(packet.corroded);
    }

    public static EgoReleasePacket decode(FriendlyByteBuf buffer) {
        return new EgoReleasePacket(buffer.readUtf(), buffer.readBoolean());
    }

    public static void handle(EgoReleasePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }
            EgoReleaseService.handle(player, packet.egoId, packet.corroded);
        });
    }
}
