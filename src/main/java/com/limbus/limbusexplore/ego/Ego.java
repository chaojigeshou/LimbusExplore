package com.limbus.limbusexplore.ego;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.combat.DamageKind;
import com.limbus.limbusexplore.sin.SinType;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

// 不可变定义目录：同一风险等级可以有多个 EGO；持有/装备与技能实现独立维护。
// 保留旧测试条目的 ID 和常量名以兼容存档。行为映射在 EgoSkills。
public final class Ego {

    public static final Ego TEST_ZAYIN = new Ego("test_zayin_ego", RiskLevel.ZAYIN, SinType.SLOTH,
            new SinCost[]{new SinCost(SinType.SLOTH, 1), new SinCost(SinType.PRIDE, 1)},
            10, DamageKind.BLUNT, 0.5f,
            0.15f, 0.55f, 0.95f);
    public static final Ego TEST_TETH = new Ego("test_teth_ego", RiskLevel.TETH, SinType.LUST,
            new SinCost[]{new SinCost(SinType.LUST, 2)},
            15, DamageKind.SLASH, 1.5f,
            0.95f, 0.30f, 0.60f);
    public static final Ego TEST_HE = new Ego("test_he_ego", RiskLevel.HE, SinType.GLUTTONY,
            new SinCost[]{new SinCost(SinType.GLUTTONY, 3), new SinCost(SinType.ENVY, 1)},
            20, DamageKind.PIERCE, 0.5f,
            0.30f, 0.75f, 0.95f);
    public static final Ego TEST_WAW = new Ego("test_waw_ego", RiskLevel.WAW, SinType.WRATH,
            new SinCost[]{new SinCost(SinType.WRATH, 2), new SinCost(SinType.SLOTH, 2)},
            25, DamageKind.BLUNT, 1.5f,
            0.95f, 0.40f, 0.30f);
    public static final Ego TEST_ALEPH = new Ego("test_aleph_ego", RiskLevel.ALEPH, SinType.GLOOM,
            new SinCost[]{new SinCost(SinType.GLOOM, 4), new SinCost(SinType.WRATH, 2)},
            30, DamageKind.SLASH, 0.5f,
            0.55f, 0.25f, 0.90f);

    public static final Ego EMBER_WATCH = new Ego("ember_watch", RiskLevel.ZAYIN, SinType.WRATH,
            new SinCost[]{new SinCost(SinType.WRATH, 2)},
            10, DamageKind.BLUNT, 0.5f, 0.95f, 0.35f, 0.12f, true);

    private static final List<Ego> CATALOG = List.of(
            TEST_ZAYIN, TEST_TETH, TEST_HE, TEST_WAW, TEST_ALEPH, EMBER_WATCH);
    private static final Map<String, Ego> BY_ID = CATALOG.stream()
            .collect(Collectors.toUnmodifiableMap(ego -> ego.id, Function.identity()));

    public final String id;
    public final RiskLevel level;
    // 主罪孽：决定图标（先复用罪孽纹理）
    public final SinType sin;
    // 释放的资源消耗组合，可以同时耗好几种
    private final List<SinCost> costs;
    private final boolean requiresUnlock;
    // 觉醒消耗的理智；侵蚀按 1.5 倍向上取整（等侵蚀机制落地）
    public final int sanityCost;
    // 使用 EGO 期间自身这一系伤害抗性变成 resistanceRate 倍（覆盖层，状态结束还原）
    public final DamageKind resistanceKind;
    public final float resistanceRate;
    // 侵蚀着色器三维柏林噪声的 RGB 权重
    public final float noiseR;
    public final float noiseG;
    public final float noiseB;

    private Ego(String id, RiskLevel level, SinType sin, SinCost[] costs, int sanityCost,
        DamageKind resistanceKind, float resistanceRate,
        float noiseR, float noiseG, float noiseB) {
        this(id, level, sin, costs, sanityCost, resistanceKind, resistanceRate, noiseR, noiseG, noiseB, false);
    }

    private Ego(String id, RiskLevel level, SinType sin, SinCost[] costs, int sanityCost,
        DamageKind resistanceKind, float resistanceRate,
        float noiseR, float noiseG, float noiseB, boolean requiresUnlock) {
        this.id = id;
        this.level = level;
        this.sin = sin;
        this.costs = List.of(costs);
        this.requiresUnlock = requiresUnlock;
        this.sanityCost = sanityCost;
        this.resistanceKind = resistanceKind;
        this.resistanceRate = resistanceRate;
        this.noiseR = noiseR;
        this.noiseG = noiseG;
        this.noiseB = noiseB;
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

    public SinCost[] costs() {
        return costs.toArray(SinCost[]::new);
    }

    public boolean requiresUnlock() {
        return requiresUnlock;
    }

    public static Ego[] values() {
        return CATALOG.toArray(Ego[]::new);
    }

    public static Ego byId(String id) {
        return id == null ? null : BY_ID.get(id);
    }
}
