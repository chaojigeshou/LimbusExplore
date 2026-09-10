package com.limbus.limbusexplore.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

// S2C：玩家自己的三系抗性（用于 UI 显示与预判）
public class ResistanceSyncPacket {

    private final float slash;
    private final float pierce;
    private final float blunt;

    public ResistanceSyncPacket(float slash, float pierce, float blunt) {
        this.slash = slash;
        this.pierce = pierce;
        this.blunt = blunt;
    }

    public static void encode(ResistanceSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeFloat(packet.slash);
        buffer.writeFloat(packet.pierce);
        buffer.writeFloat(packet.blunt);
    }

    public static ResistanceSyncPacket decode(FriendlyByteBuf buffer) {
        return new ResistanceSyncPacket(buffer.readFloat(), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(ResistanceSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.limbus.limbusexplore.client.ClientResistance.set(
                        packet.slash, packet.pierce, packet.blunt)));
    }
}
