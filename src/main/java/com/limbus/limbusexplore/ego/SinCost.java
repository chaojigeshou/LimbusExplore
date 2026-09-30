package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.sin.SinType;

// 一次消耗：多少数量的哪种罪孽。一个 EGO 的成本由多个 SinCost 组成。
public record SinCost(SinType sin, int amount) {
    public SinCost {
        if (sin == null || amount < 0) {
            throw new IllegalArgumentException("Sin costs must have a type and non-negative amount");
        }
    }

    public SinCost scaledForCorrosion() {
        return new SinCost(sin, Math.toIntExact(((long) amount * 3 + 1) / 2));
    }
}
