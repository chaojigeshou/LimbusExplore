package com.limbus.limbusexplore.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// S2C：EGO 状态（剩余秒数 / 释放的 EGO / 是否侵蚀态）。每秒同步一次做倒计时。
public class EgoStateSyncPacket {

    private final int remainingSeconds;
    private final String egoId;
    private final boolean corroded;

    public EgoStateSyncPacket(int remainingSeconds, String egoId, boolean corroded) {
        this.remainingSeconds = remainingSeconds;
        this.egoId = egoId;
        this.corroded = corroded;
    }

    public static void encode(EgoStateSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.remainingSeconds);
        buffer.writeUtf(packet.egoId);
        buffer.writeBoolean(packet.corroded);
    }

    public static EgoStateSyncPacket decode(FriendlyByteBuf buffer) {
        return new EgoStateSyncPacket(buffer.readVarInt(), buffer.readUtf(), buffer.readBoolean());
    }

    public static void handle(EgoStateSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.limbus.limbusexplore.client.ClientEgoState.set(
                        packet.remainingSeconds, packet.egoId, packet.corroded)));
    }
}
