package com.limbus.limbusexplore.net;

import com.limbus.limbusexplore.ego.Ego;
import com.limbus.limbusexplore.ego.EgoLoadout;
import com.limbus.limbusexplore.ego.EgoLoadoutApi;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** C2S：单槽修改请求。只有空字符串表示卸下，未知 ID 不能清空原装备。 */
public record EgoEquipPacket(int slot, String egoId) {

    public static void encode(EgoEquipPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.slot);
        buffer.writeUtf(packet.egoId, EgoLoadout.MAX_ID_LENGTH);
    }

    public static EgoEquipPacket decode(FriendlyByteBuf buffer) {
        return new EgoEquipPacket(buffer.readVarInt(), buffer.readUtf(EgoLoadout.MAX_ID_LENGTH));
    }

    public static void handle(EgoEquipPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            Ego ego = Ego.byId(packet.egoId);
            if (!packet.egoId.isEmpty() && ego == null) {
                EgoLoadoutApi.sync(player);
                return;
            }
            if (!EgoLoadoutApi.set(player, packet.slot, ego)) {
                player.displayClientMessage(Component.translatable("ego.limbusexplore.loadout_rejected"), true);
            }
        });
        ctx.setPacketHandled(true);
    }
}
