package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.LimbusExplore;

// 脑叶公司五级：ZAYIN < TETH < HE < WAW < ALEPH。
// 枚举顺序就是装备槽 1~5 的顺序，别重排。颜色是订死的，见 lang 旁边的 risk 键。
public enum RiskLevel {

    ZAYIN("zayin", 0xFF4CAF50),
    TETH("teth", 0xFFF2C94C),
    HE("he", 0xFF4A86E8),
    WAW("waw", 0xFF9A5BD8),
    ALEPH("aleph", 0xFFE04B4B);

    public final String id;
    public final int color;

    RiskLevel(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public String displayKey() {
        return "risk." + LimbusExplore.MODID + "." + id;
    }
}
