package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.sanity.Sanity;

// 理智值的客户端镜像，只由 SanitySyncPacket 更新。
public final class ClientSanity {

    private static int value;

    private ClientSanity() {
    }

    public static void set(int value) {
        ClientSanity.value = Math.max(Sanity.MIN, Math.min(Sanity.MAX, value));
    }

    public static int get() {
        return value;
    }
}
