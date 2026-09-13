package com.limbus.limbusexplore.net;

import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 同步包的编解码往返。这里不碰 ModNetworking（那要建频道），
 * 只把 encode → decode → 再 encode 走一遍，比字节是否一致。
 * 加字段时忘了改 decode、或者把 int 写成 float，都会在这里挂。
 */
class PacketRoundTripTest {

    @Test
    void sinSyncPacket() {
        assertRoundTrip(new SinSyncPacket(new int[]{1, 2, 3, 4, 5, 6, 7}), SinSyncPacket::encode, SinSyncPacket::decode);
    }

    @Test
    void sinSyncPacketWithNegativeValues() {
        assertRoundTrip(new SinSyncPacket(new int[]{0, -3, 0, 12, -1, 0, 0}), SinSyncPacket::encode, SinSyncPacket::decode);
    }

    @Test
    void chaosSyncPacket() {
        assertRoundTrip(new ChaosSyncPacket(100, 100, 0), ChaosSyncPacket::encode, ChaosSyncPacket::decode);
        assertRoundTrip(new ChaosSyncPacket(0, 100, 300), ChaosSyncPacket::encode, ChaosSyncPacket::decode);
    }

    @Test
    void resistanceSyncPacket() {
        assertRoundTrip(new ResistanceSyncPacket(2.0f, 1.0f, 0.5f), ResistanceSyncPacket::encode, ResistanceSyncPacket::decode);
        assertRoundTrip(new ResistanceSyncPacket(0.0f, 1.5f, 0.0f), ResistanceSyncPacket::encode, ResistanceSyncPacket::decode);
    }

    @Test
    void egoStateSyncPacket() {
        assertRoundTrip(new EgoStateSyncPacket(0, "", false), EgoStateSyncPacket::encode, EgoStateSyncPacket::decode);
        assertRoundTrip(new EgoStateSyncPacket(30, "test_aleph_ego", true), EgoStateSyncPacket::encode, EgoStateSyncPacket::decode);
    }

    private static <T> void assertRoundTrip(T packet, Encoder<T> encoder, Function<FriendlyByteBuf, T> decoder) {
        FriendlyByteBuf written = new FriendlyByteBuf(Unpooled.buffer());
        encoder.encode(packet, written);
        byte[] original = ByteBufUtil.getBytes(written);
        assertTrue(original.length > 0, "编码不该是空的");

        T decoded = decoder.apply(new FriendlyByteBuf(written.copy()));
        FriendlyByteBuf rewritten = new FriendlyByteBuf(Unpooled.buffer());
        encoder.encode(decoded, rewritten);

        assertArrayEquals(original, ByteBufUtil.getBytes(rewritten), "解出来的东西再编一次应该一模一样");
    }

    @FunctionalInterface
    private interface Encoder<T> {
        void encode(T packet, FriendlyByteBuf buffer);
    }
}
