package com.limbus.limbusexplore.client;

import java.util.HashMap;
import java.util.Map;

/**
 * 客户端记录"哪些生物正在混乱"（怪物头顶要画混乱图）。
 * 服务端进入/结束混乱时广播 EntityChaosPacket，这里按 tick 倒数，到点自动清掉。
 */
public final class ClientEntityChaos {

    private static final Map<Integer, Integer> TICKS = new HashMap<>();

    private ClientEntityChaos() {
    }

    public static void set(int entityId, int chaosTicks) {
        if (chaosTicks <= 0) {
            TICKS.remove(entityId);
        } else {
            TICKS.put(entityId, chaosTicks);
        }
    }

    public static boolean isInChaos(int entityId) {
        Integer ticks = TICKS.get(entityId);
        return ticks != null && ticks > 0;
    }

    public static int ticks(int entityId) {
        return TICKS.getOrDefault(entityId, 0);
    }

    /** 每客户端 tick 调一次 */
    public static void tick() {
        if (TICKS.isEmpty()) {
            return;
        }
        TICKS.entrySet().removeIf(entry -> {
            int left = entry.getValue() - 1;
            entry.setValue(left);
            return left <= 0;
        });
    }

    public static void clear() {
        TICKS.clear();
    }
}
