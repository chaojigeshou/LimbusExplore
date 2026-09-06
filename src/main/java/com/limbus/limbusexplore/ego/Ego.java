package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.sin.SinType;
import net.minecraft.resources.ResourceLocation;

// 测试用 EGO，Z~A 各一条。字段对齐巴士的 EGO 数据结构：
// 多资源消耗组合、觉醒/侵蚀双技能、理智消耗、罪孽抗性覆盖、被动。
// 换正式内容时只动这个枚举，下面的 key/texture 方法不用改。
public enum Ego {

    TEST_ZAYIN("test_zayin_ego", RiskLevel.ZAYIN, SinType.SLOTH,
            new SinCost[]{new SinCost(SinType.SLOTH, 1), new SinCost(SinType.PRIDE, 1)},
            10, SinType.SLOTH, 0.5f),
    TEST_TETH("test_teth_ego", RiskLevel.TETH, SinType.LUST,
            new SinCost[]{new SinCost(SinType.LUST, 2)},
            15, SinType.ENVY, 1.5f),
    TEST_HE("test_he_ego", RiskLevel.HE, SinType.GLUTTONY,
            new SinCost[]{new SinCost(SinType.GLUTTONY, 3), new SinCost(SinType.ENVY, 1)},
            20, SinType.GLUTTONY, 0.5f),
    TEST_WAW("test_waw_ego", RiskLevel.WAW, SinType.WRATH,
            new SinCost[]{new SinCost(SinType.WRATH, 2), new SinCost(SinType.SLOTH, 2)},
            25, SinType.PRIDE, 1.5f),
    TEST_ALEPH("test_aleph_ego", RiskLevel.ALEPH, SinType.GLOOM,
            new SinCost[]{new SinCost(SinType.GLOOM, 4), new SinCost(SinType.WRATH, 2)},
            30, SinType.GLOOM, 0.5f);

    public final String id;
    public final RiskLevel level;
    // 主罪孽：决定图标（先复用罪孽纹理）和抗性覆盖的默认指向
    public final SinType sin;
    // 释放的资源消耗组合，可以同时耗好几种
    public final SinCost[] costs;
    // 觉醒消耗的理智；侵蚀按 1.5 倍向上取整（等侵蚀机制落地）
    public final int sanityCost;
    // 使用 EGO 后覆盖自身这份罪孽抗性为 resistanceRate 倍
    public final SinType resistanceSin;
    public final float resistanceRate;

    Ego(String id, RiskLevel level, SinType sin, SinCost[] costs, int sanityCost,
        SinType resistanceSin, float resistanceRate) {
        this.id = id;
        this.level = level;
        this.sin = sin;
        this.costs = costs;
        this.sanityCost = sanityCost;
        this.resistanceSin = resistanceSin;
        this.resistanceRate = resistanceRate;
    }

    public String displayKey() {
        return "ego." + LimbusExplore.MODID + "." + id;
    }

    public String descKey() {
        return displayKey() + ".desc";
    }

    public String awakeningKey() {
        return displayKey() + ".awakening";
    }

    public String corrosionKey() {
        return displayKey() + ".corrosion";
    }

    public String passiveKey() {
        return displayKey() + ".passive";
    }

    // 正式图放 textures/ego/<id>.png，没放之前渲染那边回退罪孽图标
    public ResourceLocation texture() {
        return new ResourceLocation(LimbusExplore.MODID, "textures/ego/" + id + ".png");
    }

    public static Ego byId(String id) {
        for (Ego ego : values()) {
            if (ego.id.equals(id)) {
                return ego;
            }
        }
        return null;
    }
}
