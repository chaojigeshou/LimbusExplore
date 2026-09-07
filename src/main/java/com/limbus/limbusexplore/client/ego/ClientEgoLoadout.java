package com.limbus.limbusexplore.client.ego;

import com.google.gson.Gson;
import com.limbus.limbusexplore.ego.Ego;
import com.limbus.limbusexplore.ego.RiskLevel;
import net.minecraft.client.Minecraft;

import java.nio.file.Files;
import java.nio.file.Path;

// 客户端装备状态：5 槽，每个槽固定一个等级（1=ZAYIN .. 5=ALEPH），EGO 只能进对应槽。
// 自动持久化到 <游戏目录>/limbusexplore_loadout.json，重启客户端保留。
// 联机同步落地时换成服务端数据（本地文件先作为客户端存储用）。
public final class ClientEgoLoadout {

    public static final int SLOTS = 5;

    private static final Gson GSON = new Gson();

    private static final Ego[] EQUIPPED = new Ego[SLOTS];
    // 每个槽的待发形态：true = 已长按切到侵蚀态（右键取消），释放时按这个走
    private static final boolean[] CORRODED = new boolean[SLOTS];

    // 已装备列表的缓存，渲染每帧都用它；装备一变就置脏重算
    private static Ego[] cachedList = new Ego[0];
    private static boolean dirty = true;

    static {
        load();
    }

    private ClientEgoLoadout() {
    }

    private static Path storeFile() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("limbusexplore_loadout.json");
    }

    private static void load() {
        try {
            Path file = storeFile();
            if (!Files.exists(file)) {
                return;
            }
            String[] ids = GSON.fromJson(Files.readString(file), String[].class);
            if (ids == null || ids.length != SLOTS) {
                return;
            }
            for (int slot = 0; slot < SLOTS; slot++) {
                Ego ego = ids[slot] == null ? null : Ego.byId(ids[slot]);
                // 等级不匹配（配置被改过/枚举变了）就当空的，别硬塞
                if (ego != null && canEquip(slot, ego)) {
                    EQUIPPED[slot] = ego;
                } else {
                    EQUIPPED[slot] = null;
                }
            }
            dirty = true;
        } catch (Exception ignored) {
            // 文件损坏就按空配置走，不挡启动
        }
    }

    private static void save() {
        try {
            String[] ids = new String[SLOTS];
            for (int slot = 0; slot < SLOTS; slot++) {
                ids[slot] = EQUIPPED[slot] == null ? null : EQUIPPED[slot].id;
            }
            Files.writeString(storeFile(), GSON.toJson(ids));
        } catch (Exception ignored) {
        }
    }

    public static Ego get(int slot) {
        return EQUIPPED[slot];
    }

    public static boolean isCorroded(int slot) {
        return CORRODED[slot];
    }

    // 释放界面长按切换侵蚀态、右键取消都走这里（改不了装备本身，只改形态）
    public static void setCorroded(int slot, boolean corroded) {
        CORRODED[slot] = corroded;
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
                CORRODED[i] = false;
            }
        }
        EQUIPPED[slot] = ego;
        CORRODED[slot] = false;
        dirty = true;
        save();
        return true;
    }

    public static void unequip(int slot) {
        EQUIPPED[slot] = null;
        CORRODED[slot] = false;
        dirty = true;
        save();
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
