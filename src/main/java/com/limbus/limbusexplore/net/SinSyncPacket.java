package com.limbus.limbusexplore.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// S2C：全量同步七种罪孽资源，数组顺序就是 SinType.values()。
public class SinSyncPacket {

    private final int[] values;

    public SinSyncPacket(int[] values) {
        this.values = values;
    }

    public static void encode(SinSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarIntArray(packet.values);
    }

    public static SinSyncPacket decode(FriendlyByteBuf buffer) {
        return new SinSyncPacket(buffer.readVarIntArray());
    }

    public static void handle(SinSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.limbus.limbusexplore.client.ClientSinResources.set(packet.values)));
    }
}
