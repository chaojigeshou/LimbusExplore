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

    public static final ForgeConfigSpec.BooleanValue DAMAGE_KIND_ENABLED = BUILDER
            .comment("Enable slash/pierce/blunt damage kinds and their resistances",
                    "关掉之后本 mod 不再改伤害数值（混乱值仍然累计）")
            .define("damageKindEnabled", true);

    public static final ForgeConfigSpec.BooleanValue CHAOS_ENABLED = BUILDER
            .comment("Enable the chaos (stagger) system: damage builds chaos, zero triggers a 15s lock",
                    "关掉之后挨打不再扣混乱值、也不会触发混乱状态（已在混乱中的会正常结束）")
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
