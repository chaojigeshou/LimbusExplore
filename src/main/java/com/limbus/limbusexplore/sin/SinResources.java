package com.limbus.limbusexplore.sin;

import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;

// 服务端存资源的地方，负值不允许出现（侵蚀透支那套以后再说）。
// 客户端不碰这个类，它只看同步包喂过来的 ClientSinResources。
public final class SinResources {

    private final EnumMap<SinType, Integer> values = new EnumMap<>(SinType.class);

    public SinResources() {
        for (SinType type : SinType.values()) {
            values.put(type, 0);
        }
    }

    public int get(SinType type) {
        return values.getOrDefault(type, 0);
    }

    public void add(SinType type, int amount) {
        values.put(type, Math.max(0, get(type) + amount));
    }

    /** 不设下限的扣减，侵蚀消耗用（透支为负，等状态结束归 0）。 */
    public void forceAdd(SinType type, int amount) {
        values.put(type, get(type) + amount);
    }

    public void set(SinType type, int amount) {
        values.put(type, Math.max(0, amount));
    }

    public void clear() {
        for (SinType type : SinType.values()) {
            values.put(type, 0);
        }
    }

    public void copyFrom(SinResources other) {
        for (SinType type : SinType.values()) {
            values.put(type, other.get(type));
        }
    }

    public int[] toArray() {
        int[] array = new int[SinType.values().length];
        int i = 0;
        for (SinType type : SinType.values()) {
            array[i++] = get(type);
        }
        return array;
    }

    public void fromArray(int[] array) {
        if (array.length != SinType.values().length) {
            return;
        }
        int i = 0;
        for (SinType type : SinType.values()) {
            set(type, array[i++]);
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putIntArray("values", toArray());
        return tag;
    }

    public void load(CompoundTag tag) {
        fromArray(tag.getIntArray("values"));
    }
}
