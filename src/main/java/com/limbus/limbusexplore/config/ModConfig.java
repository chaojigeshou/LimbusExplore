package com.limbus.limbusexplore.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ModConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue LOG_STARTUP = BUILDER
            .comment("Whether to log a startup message during mod loading")
            .define("logStartup", true);

    public static final ForgeConfigSpec.BooleanValue DAMAGE_KIND_ENABLED = BUILDER
            .comment("Enable slash/pierce/blunt damage kinds and their resistances",
                    "关掉之后本 mod 不再改伤害数值（混乱值仍然累计）")
            .define("damageKindEnabled", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private ModConfig() {
    }
}
