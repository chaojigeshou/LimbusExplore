package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.combat.DamageKind;

/** 自己三系抗性的客户端镜像（UI 用）。 */
public final class ClientResistance {

    private static float slash = 1f;
    private static float pierce = 1f;
    private static float blunt = 1f;

    private ClientResistance() {
    }

    public static void set(float slash, float pierce, float blunt) {
        ClientResistance.slash = slash;
        ClientResistance.pierce = pierce;
        ClientResistance.blunt = blunt;
    }

    public static float get(DamageKind kind) {
        return switch (kind) {
            case SLASH -> slash;
            case PIERCE -> pierce;
            case BLUNT -> blunt;
        };
    }
}
