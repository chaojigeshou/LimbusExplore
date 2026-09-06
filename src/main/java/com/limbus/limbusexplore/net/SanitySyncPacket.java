package com.limbus.limbusexplore.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// S2C：理智值全量同步。
public class SanitySyncPacket {

    private final int value;

    public SanitySyncPacket(int value) {
        this.value = value;
    }

    public static void encode(SanitySyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.value);
    }

    public static SanitySyncPacket decode(FriendlyByteBuf buffer) {
        return new SanitySyncPacket(buffer.readVarInt());
    }

    public static void handle(SanitySyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.limbus.limbusexplore.client.ClientSanity.set(packet.value)));
    }
}
