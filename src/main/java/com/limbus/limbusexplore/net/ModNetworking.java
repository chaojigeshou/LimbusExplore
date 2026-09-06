package com.limbus.limbusexplore.net;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

// 全 mod 只有一个频道。加新包的时候在 register() 里加一行注册，然后用 sendToXxx 发。
// 注意给每个包标对方向，S2C 和 C2S 弄混了客户端收不到也不会报错，只有日志里一声警告。
public final class ModNetworking {

    private static final String PROTOCOL = "1";

    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(LimbusExplore.MODID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private static int nextId = 0;

    private ModNetworking() {
    }

    public static void register() {
        CHANNEL.registerMessage(nextId++, SinSyncPacket.class,
                SinSyncPacket::encode, SinSyncPacket::decode, SinSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(nextId++, SanitySyncPacket.class,
                SanitySyncPacket::encode, SanitySyncPacket::decode, SanitySyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(nextId++, EgoReleasePacket.class,
                EgoReleasePacket::encode, EgoReleasePacket::decode, EgoReleasePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER));
        CHANNEL.registerMessage(nextId++, EgoReleaseResultPacket.class,
                EgoReleaseResultPacket::encode, EgoReleaseResultPacket::decode, EgoReleaseResultPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static <T> void sendToPlayer(ServerPlayer player, T packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
