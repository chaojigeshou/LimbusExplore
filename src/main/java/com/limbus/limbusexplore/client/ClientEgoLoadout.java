package com.limbus.limbusexplore.client;

import com.limbus.limbusexplore.ego.Ego;
import com.limbus.limbusexplore.ego.EgoLoadout;
import com.limbus.limbusexplore.ego.RiskLevel;
import com.limbus.limbusexplore.net.EgoEquipPacket;
import com.limbus.limbusexplore.net.ModNetworking;
import net.minecraft.client.Minecraft;

import java.util.Arrays;

// 仅保存服务端装备快照；装备/卸下只发送请求，收到 S2C 后才更新界面。
// 旧 limbusexplore_loadout.json 不再读取或写入，避免跨存档和跨服务器共享装备。
public final class ClientEgoLoadout {

    public static final int SLOTS = EgoLoadout.SLOTS;

    private static final EgoLoadout LOADOUT = new EgoLoadout();
    private static boolean ready;
    // 每个槽的待发形态：true = 已长按切到侵蚀态（右键取消），释放时按这个走
    private static final boolean[] CORRODED = new boolean[SLOTS];

    // 已装备列表的缓存，渲染每帧都用它；装备一变就置脏重算
    private static Ego[] cachedList = new Ego[0];
    private static boolean dirty = true;

    private ClientEgoLoadout() {
    }

    public static void applySnapshot(String[] ids) {
        applySnapshot(ids, new String[0]);
    }

    public static void applySnapshot(String[] ids, String[] unlocked) {
        LOADOUT.setUnlockedIds(unlocked);
        LOADOUT.fromIds(ids);
        Arrays.fill(CORRODED, false);
        dirty = true;
        ready = true;
    }

    /** 断开连接时清空，下一服务器的首次快照到达前禁止发送装备请求。 */
    public static void clear() {
        LOADOUT.setUnlockedIds(new String[0]);
        LOADOUT.fromIds(new String[0]);
        Arrays.fill(CORRODED, false);
        cachedList = new Ego[0];
        dirty = true;
        ready = false;
    }

    public static Ego get(int slot) {
        return LOADOUT.get(slot);
    }

    public static boolean isCorroded(int slot) {
        return EgoLoadout.isValidSlot(slot) && CORRODED[slot];
    }

    // 释放界面长按切换侵蚀态、右键取消都走这里（改不了装备本身，只改形态）
    public static void setCorroded(int slot, boolean corroded) {
        if (LOADOUT.get(slot) != null) {
            CORRODED[slot] = corroded;
        }
    }

    /** 槽位对应的固定等级，槽 0..4 → ZAYIN..ALEPH。 */
    public static RiskLevel levelOfSlot(int slot) {
        return RiskLevel.values()[slot];
    }

    public static boolean canEquip(int slot, Ego ego) {
        return EgoLoadout.canEquip(slot, ego) && LOADOUT.isUnlocked(ego);
    }

    public static boolean isUnlocked(Ego ego) {
        return LOADOUT.isUnlocked(ego);
    }

    // 返回 true 仅代表已发送请求；实际装备结果由服务端快照决定。
    public static boolean equip(int slot, Ego ego) {
        if (!ready || !canEquip(slot, ego) || Minecraft.getInstance().getConnection() == null) {
            return false;
        }
        ModNetworking.sendToServer(new EgoEquipPacket(slot, ego.id));
        return true;
    }

    public static void unequip(int slot) {
        if (ready && EgoLoadout.isValidSlot(slot) && Minecraft.getInstance().getConnection() != null) {
            ModNetworking.sendToServer(new EgoEquipPacket(slot, ""));
        }
    }

    /** 只含非空槽、按槽位顺序（Z 最左）的已装备列表。渲染频繁调用，内部有缓存。 */
    public static Ego[] equippedList() {
        if (dirty) {
            int count = 0;
            for (int slot = 0; slot < SLOTS; slot++) {
                if (LOADOUT.get(slot) != null) {
                    count++;
                }
            }
            Ego[] list = new Ego[count];
            int i = 0;
            for (int slot = 0; slot < SLOTS; slot++) {
                Ego ego = LOADOUT.get(slot);
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
