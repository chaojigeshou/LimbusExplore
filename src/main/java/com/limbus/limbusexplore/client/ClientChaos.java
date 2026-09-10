package com.limbus.limbusexplore.client;

/**
 * 混乱值的客户端镜像，只由 ChaosSyncPacket 更新。
 * 混乱状态期间客户端锁定（ChaosLockScreen），这里提供状态给 HUD / 锁定界面读。
 */
public final class ClientChaos {

    private static int value = 100;
    private static int max = 100;
    private static int chaosTicks;

    private ClientChaos() {
    }

    public static void set(int value, int max, int chaosTicks) {
        ClientChaos.value = value;
        ClientChaos.max = max;
        ClientChaos.chaosTicks = chaosTicks;
    }

    public static int get() {
        return value;
    }

    public static int getMax() {
        return max;
    }

    public static boolean isInChaos() {
        return chaosTicks > 0;
    }

    public static int remainingSeconds() {
        return (chaosTicks + 19) / 20;
    }
}
