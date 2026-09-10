package com.limbus.limbusexplore.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// S2C：混乱值 + 混乱状态剩余 tick（0 = 不在混乱）。
public class ChaosSyncPacket {

    private final int value;
    private final int max;
    private final int chaosTicks;

    public ChaosSyncPacket(int value, int max, int chaosTicks) {
        this.value = value;
        this.max = max;
        this.chaosTicks = chaosTicks;
    }

    public static void encode(ChaosSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.value);
        buffer.writeVarInt(packet.max);
        buffer.writeVarInt(packet.chaosTicks);
    }

    public static ChaosSyncPacket decode(FriendlyByteBuf buffer) {
        return new ChaosSyncPacket(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(ChaosSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.limbus.limbusexplore.client.ClientChaos.set(
                        packet.value, packet.max, packet.chaosTicks)));
    }
}
