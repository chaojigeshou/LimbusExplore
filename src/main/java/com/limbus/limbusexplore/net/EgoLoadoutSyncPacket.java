package com.limbus.limbusexplore.net;

import com.limbus.limbusexplore.ego.EgoLoadout;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** S2C：固定五槽的完整快照，空字符串表示空槽。 */
public final class EgoLoadoutSyncPacket {

    private final String[] ids;
    private final String[] unlocked;

    public EgoLoadoutSyncPacket(String[] ids) {
        this(ids, new String[0]);
    }

    public EgoLoadoutSyncPacket(String[] ids, String[] unlocked) {
        if (ids.length != EgoLoadout.SLOTS) {
            throw new IllegalArgumentException("Expected " + EgoLoadout.SLOTS + " loadout slots");
        }
        this.ids = ids.clone();
        this.unlocked = unlocked.clone();
    }

    public static void encode(EgoLoadoutSyncPacket packet, FriendlyByteBuf buffer) {
        for (String id : packet.ids) {
            buffer.writeUtf(id, EgoLoadout.MAX_ID_LENGTH);
        }
        buffer.writeVarInt(packet.unlocked.length);
        for (String id : packet.unlocked) buffer.writeUtf(id, EgoLoadout.MAX_ID_LENGTH);
    }

    public static EgoLoadoutSyncPacket decode(FriendlyByteBuf buffer) {
        String[] ids = new String[EgoLoadout.SLOTS];
        for (int slot = 0; slot < ids.length; slot++) {
            ids[slot] = buffer.readUtf(EgoLoadout.MAX_ID_LENGTH);
        }
        int count = buffer.readVarInt();
        if (count < 0 || count > 1024) throw new io.netty.handler.codec.DecoderException("Invalid unlock count");
        String[] unlocked = new String[count];
        for (int i = 0; i < count; i++) unlocked[i] = buffer.readUtf(EgoLoadout.MAX_ID_LENGTH);
        return new EgoLoadoutSyncPacket(ids, unlocked);
    }

    public static void handle(EgoLoadoutSyncPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.limbus.limbusexplore.client.ClientEgoLoadout.applySnapshot(packet.ids, packet.unlocked)));
        ctx.setPacketHandled(true);
    }
}
