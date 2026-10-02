package com.limbus.limbusexplore.ego;

import net.minecraft.nbt.CompoundTag;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

/** 玩家装备数据。槽位固定对应风险等级；不依赖客户端或网络。 */
public final class EgoLoadout {

    public static final int SLOTS = RiskLevel.values().length;
    public static final int MAX_ID_LENGTH = 128;

    private final Ego[] equipped = new Ego[SLOTS];
    private final Set<String> unlocked = new HashSet<>();

    public boolean isUnlocked(Ego ego) {
        return ego != null && (!ego.requiresUnlock() || unlocked.contains(ego.id));
    }

    public boolean unlock(Ego ego) {
        return ego != null && ego.requiresUnlock() && unlocked.add(ego.id);
    }

    public String[] unlockedIds() {
        return unlocked.stream().sorted().toArray(String[]::new);
    }

    public void setUnlockedIds(String[] ids) {
        unlocked.clear();
        for (String id : ids) {
            Ego ego = Ego.byId(id);
            if (ego != null && ego.requiresUnlock()) unlocked.add(id);
        }
        for (int slot = 0; slot < SLOTS; slot++) {
            if (!isUnlocked(equipped[slot])) equipped[slot] = null;
        }
    }

    public static boolean isValidSlot(int slot) {
        return slot >= 0 && slot < SLOTS;
    }

    public static boolean canEquip(int slot, Ego ego) {
        return isValidSlot(slot) && ego != null && ego.level == RiskLevel.values()[slot];
    }

    public Ego get(int slot) {
        return isValidSlot(slot) ? equipped[slot] : null;
    }

    /** null 表示卸下；非法槽位或等级不匹配时不改变原装备。 */
    public boolean set(int slot, Ego ego) {
        if (!isValidSlot(slot) || (ego != null && (!canEquip(slot, ego) || !isUnlocked(ego)))) {
            return false;
        }
        equipped[slot] = ego;
        return true;
    }

    public boolean contains(Ego ego) {
        return isUnlocked(ego) && equipped[ego.level.ordinal()] == ego;
    }

    public void copyFrom(EgoLoadout other) {
        System.arraycopy(other.equipped, 0, equipped, 0, SLOTS);
        unlocked.clear();
        unlocked.addAll(other.unlocked);
    }

    public String[] toIds() {
        String[] ids = new String[SLOTS];
        for (int slot = 0; slot < SLOTS; slot++) {
            ids[slot] = equipped[slot] == null ? "" : equipped[slot].id;
        }
        return ids;
    }

    /** 全量替换；存档或网络中未知、错等级的 ID 视为空槽。 */
    public void fromIds(String[] ids) {
        Arrays.fill(equipped, null);
        for (int slot = 0; slot < Math.min(ids.length, SLOTS); slot++) {
            set(slot, Ego.byId(ids[slot]));
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag unlocks = new ListTag();
        for (String id : unlockedIds()) unlocks.add(StringTag.valueOf(id));
        tag.put("unlocked", unlocks);
        for (int slot = 0; slot < SLOTS; slot++) {
            if (equipped[slot] != null) {
                tag.putString(RiskLevel.values()[slot].id, equipped[slot].id);
            }
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        ListTag unlocks = tag.getList("unlocked", Tag.TAG_STRING);
        String[] unlockedIds = new String[unlocks.size()];
        for (int i = 0; i < unlockedIds.length; i++) unlockedIds[i] = unlocks.getString(i);
        setUnlockedIds(unlockedIds);
        String[] ids = new String[SLOTS];
        for (int slot = 0; slot < SLOTS; slot++) {
            ids[slot] = tag.getString(RiskLevel.values()[slot].id);
        }
        fromIds(ids);
    }
}
