package com.limbus.limbusexplore.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// C2S：客户端想释放哪个 EGO，只带 egoId。判定全在服务端。
public class EgoReleasePacket {

    private final String egoId;

    public EgoReleasePacket(String egoId) {
        this.egoId = egoId;
    }

    public static void encode(EgoReleasePacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.egoId);
    }

    public static EgoReleasePacket decode(FriendlyByteBuf buffer) {
        return new EgoReleasePacket(buffer.readUtf());
    }

    public static void handle(EgoReleasePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) {
                return;
            }
            EgoReleaseService.handle(player, packet.egoId);
        });
    }
}
