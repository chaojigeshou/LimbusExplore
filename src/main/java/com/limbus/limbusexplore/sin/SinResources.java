package com.limbus.limbusexplore.sin;

import net.minecraft.nbt.CompoundTag;

import java.util.EnumMap;
import com.limbus.limbusexplore.ego.SinCost;

// 服务端存资源的地方。正常加减不允许负数（add / set 都夹在 0 以上），
// 只有侵蚀释放走 forceAdd 能透支成负的，等 EGO 状态结束由 SinApi.normalizeNegative 归 0。
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
        // 获得资源先偿还侵蚀欠债，不能用一次 +1 抹掉整笔负债。
        values.put(type, (int) Math.max(get(type) < 0 ? Integer.MIN_VALUE : 0,
                Math.min(Integer.MAX_VALUE, (long) get(type) + amount)));
    }

    /** 不设下限的扣减，侵蚀消耗用（透支为负，等状态结束归 0）。 */
    public void forceAdd(SinType type, int amount) {
        values.put(type, (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, (long) get(type) + amount)));
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

    /** 按数组写回，数组顺序就是 SinType.values()。存的是原值，负的透支额也得原样读回来。 */
    public void fromArray(int[] array) {
        if (array.length != SinType.values().length) {
            return;
        }
        int i = 0;
        for (SinType type : SinType.values()) {
            values.put(type, array[i++]);
        }
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putIntArray("values", toArray());
        return tag;
    }

    public void load(CompoundTag tag) {
        // 先清零：档里缺字段或者是旧的坏格式，就当全 0，别留着上一份的残值
        clear();
        fromArray(tag.getIntArray("values"));
    }

    private static EnumMap<SinType, Long> totals(SinCost[] costs) {
        EnumMap<SinType, Long> total = new EnumMap<>(SinType.class);
        for (SinCost cost : costs) total.merge(cost.sin(), (long) cost.amount(), Long::sum);
        return total;
    }

    public boolean canPay(SinCost[] costs) {
        return totals(costs).entrySet().stream().allMatch(entry -> get(entry.getKey()) >= entry.getValue());
    }

    public boolean consumeAll(SinCost[] costs, boolean forced) {
        EnumMap<SinType, Long> total = totals(costs);
        if (!forced && !canPay(costs)) return false;
        total.forEach((sin, amount) -> values.put(sin,
                (int) Math.max(Integer.MIN_VALUE, (long) get(sin) - amount)));
        return true;
    }
}
