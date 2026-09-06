package com.limbus.limbusexplore.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// S2C：释放结果，result 是 EgoApi.ReleaseResult.ordinal()，客户端照着展示。
public class EgoReleaseResultPacket {

    private final String egoId;
    private final int result;

    public EgoReleaseResultPacket(String egoId, int result) {
        this.egoId = egoId;
        this.result = result;
    }

    public static void encode(EgoReleaseResultPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.egoId);
        buffer.writeVarInt(packet.result);
    }

    public static EgoReleaseResultPacket decode(FriendlyByteBuf buffer) {
        return new EgoReleaseResultPacket(buffer.readUtf(), buffer.readVarInt());
    }

    public static void handle(EgoReleaseResultPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.limbus.limbusexplore.client.EgoReleaseClient.onResult(packet.egoId, packet.result)));
    }
}
