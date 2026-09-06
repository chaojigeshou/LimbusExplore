package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.ego.SinCost;
import com.limbus.limbusexplore.sin.SinType;

import java.util.EnumMap;

// 客户端镜像，只由同步包喂。服务端是唯一权威。
public final class ClientSinResources {

    private static final EnumMap<SinType, Integer> VALUES = new EnumMap<>(SinType.class);

    static {
        for (SinType type : SinType.values()) {
            VALUES.put(type, 0);
        }
    }

    private ClientSinResources() {
    }

    public static void set(int[] values) {
        if (values.length != SinType.values().length) {
            return;
        }
        int i = 0;
        for (SinType type : SinType.values()) {
            VALUES.put(type, Math.max(0, values[i++]));
        }
    }

    public static int get(SinType type) {
        return VALUES.getOrDefault(type, 0);
    }

    // 预检用的判断，真要扣还是得服务端来
    public static boolean canPayClient(SinCost[] costs) {
        for (SinCost cost : costs) {
            if (get(cost.sin()) < cost.amount()) {
                return false;
            }
        }
        return true;
    }
}
