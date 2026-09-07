package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.ego.Ego;

// EGO 状态的客户端镜像，只由 EgoStateSyncPacket 更新，HUD 倒计时用。
public final class ClientEgoState {

    private static int remainingSeconds;
    private static String activeEgoId = "";
    private static boolean corroded;

    private ClientEgoState() {
    }

    public static void set(int remainingSeconds, String activeEgoId, boolean corroded) {
        ClientEgoState.remainingSeconds = remainingSeconds;
        ClientEgoState.activeEgoId = activeEgoId;
        ClientEgoState.corroded = corroded;
    }

    public static boolean isInState() {
        return remainingSeconds > 0;
    }

    public static int remainingSeconds() {
        return remainingSeconds;
    }

    public static Ego activeEgo() {
        return Ego.byId(activeEgoId);
    }

    public static boolean isCorroded() {
        return corroded;
    }
}
