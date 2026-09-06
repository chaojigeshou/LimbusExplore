package com.limbus.limbusexplore.client.ego;

import com.limbus.limbusexplore.ego.Ego;
import com.limbus.limbusexplore.ego.RiskLevel;

// 客户端装备状态：5 槽，每个槽固定一个等级（1=ZAYIN .. 5=ALEPH），EGO 只能进对应槽。
// 目前重启即清空，联机同步落地时换成服务端数据。
public final class ClientEgoLoadout {

    public static final int SLOTS = 5;

    private static final Ego[] EQUIPPED = new Ego[SLOTS];

    private ClientEgoLoadout() {
    }

    public static Ego get(int slot) {
        return EQUIPPED[slot];
    }

    /** 槽位对应的固定等级，槽 0..4 → ZAYIN..ALEPH。 */
    public static RiskLevel levelOfSlot(int slot) {
        return RiskLevel.values()[slot];
    }

    public static boolean canEquip(int slot, Ego ego) {
        return ego.level == levelOfSlot(slot);
    }

    // 等级不匹配返回 false；EGO 已经装在哪格会自动腾出来，不重复装
    public static boolean equip(int slot, Ego ego) {
        if (!canEquip(slot, ego)) {
            return false;
        }
        for (int i = 0; i < SLOTS; i++) {
            if (EQUIPPED[i] == ego) {
                EQUIPPED[i] = null;
            }
        }
        EQUIPPED[slot] = ego;
        return true;
    }

    public static void unequip(int slot) {
        EQUIPPED[slot] = null;
    }
}
