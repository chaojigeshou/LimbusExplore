package com.limbus.limbusexplore.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * 通用配置（config/limbusexplore-common.toml）。
 * 这里的开关就是"官方绕过入口"：服主不想用某块机制时可以直接关，
 * 关掉之后本 mod 对应部分不生效，也不影响别的 mod。
 */
public final class ModConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue LOG_STARTUP = BUILDER
            .comment("Whether to log a startup message during mod loading")
            .define("logStartup", true);

    public static final ForgeConfigSpec.BooleanValue KILL_REWARDS_ENABLED = BUILDER
            .comment("Player kills of hostile enemies grant 1 Wrath and 3 Sanity")
            .define("killRewardsEnabled", true);

    public static final ForgeConfigSpec.BooleanValue DAMAGE_KIND_ENABLED = BUILDER
            .comment("Enable slash/pierce/blunt damage kinds and their resistances",
                    "关掉只禁用三系抗性/等级修正，混乱累计和混乱挨打倍率由 chaosEnabled 控制")
            .define("damageKindEnabled", true);

    public static final ForgeConfigSpec.BooleanValue CHAOS_ENABLED = BUILDER
            .comment("Enable the chaos (stagger) system: damage builds chaos, zero triggers a 15s lock",
                    "关掉之后停止累计和混乱增伤，已在混乱中的实体在下次 tick 解除")
            .define("chaosEnabled", true);

    public static final ForgeConfigSpec.BooleanValue CHAOS_MARK_ENABLED = BUILDER
            .comment("Render the 'staggered' mark above entities that are in chaos",
                    "纯客户端显示开关，关掉只是不画那张图")
            .define("chaosMarkEnabled", true);

    public static final ForgeConfigSpec.BooleanValue JADE_COMPAT_ENABLED = BUILDER
            .comment("Send chaos/resistance data to Jade (Jade 侧还有它自己的开关界面)",
                    "关掉之后 Jade 上看不到本 mod 的信息")
            .define("jadeCompatEnabled", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private ModConfig() {
    }
}
