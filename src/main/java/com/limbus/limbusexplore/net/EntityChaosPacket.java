package com.limbus.limbusexplore.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * S2C：某个生物进入/离开混乱（怪物头顶的"混乱"图靠它显示）。
 * chaosTicks = 0 表示结束；广播给追踪该实体的玩家。
 */
public class EntityChaosPacket {

    private final int entityId;
    private final int chaosTicks;

    public EntityChaosPacket(int entityId, int chaosTicks) {
        this.entityId = entityId;
        this.chaosTicks = chaosTicks;
    }

    public static void encode(EntityChaosPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.entityId);
        buffer.writeVarInt(packet.chaosTicks);
    }

    public static EntityChaosPacket decode(FriendlyByteBuf buffer) {
        return new EntityChaosPacket(buffer.readVarInt(), buffer.readVarInt());
    }

    public static void handle(EntityChaosPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> com.limbus.limbusexplore.client.ClientEntityChaos.set(
                        packet.entityId, packet.chaosTicks)));
    }

    /** 发给所有能看到这个实体的玩家 */
    public static void broadcast(Entity entity, int chaosTicks) {
        com.limbus.limbusexplore.net.ModNetworking.sendToTracking(entity,
                new EntityChaosPacket(entity.getId(), chaosTicks));
    }
}
