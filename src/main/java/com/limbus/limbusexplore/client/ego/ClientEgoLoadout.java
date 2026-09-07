package com.limbus.limbusexplore.client.ego;

import com.limbus.limbusexplore.ego.Ego;
import com.limbus.limbusexplore.ego.RiskLevel;

// 客户端装备状态：5 槽，每个槽固定一个等级（1=ZAYIN .. 5=ALEPH），EGO 只能进对应槽。
// 目前重启即清空，联机同步落地时换成服务端数据。
public final class ClientEgoLoadout {

    public static final int SLOTS = 5;

    private static final Ego[] EQUIPPED = new Ego[SLOTS];

    // 已装备列表的缓存，渲染每帧都用它；装备一变就置脏重算
    private static Ego[] cachedList = new Ego[0];
    private static boolean dirty = true;

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
        dirty = true;
        return true;
    }

    public static void unequip(int slot) {
        EQUIPPED[slot] = null;
        dirty = true;
    }

    /** 只含非空槽、按槽位顺序（Z 最左）的已装备列表。渲染频繁调用，内部有缓存。 */
    public static Ego[] equippedList() {
        if (dirty) {
            int count = 0;
            for (Ego ego : EQUIPPED) {
                if (ego != null) {
                    count++;
                }
            }
            Ego[] list = new Ego[count];
            int i = 0;
            for (Ego ego : EQUIPPED) {
                if (ego != null) {
                    list[i++] = ego;
                }
            }
            cachedList = list;
            dirty = false;
        }
        return cachedList;
    }
}
