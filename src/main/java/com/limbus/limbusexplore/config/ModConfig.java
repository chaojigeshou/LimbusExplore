package com.limbus.limbusexplore.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ModConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.BooleanValue LOG_STARTUP = BUILDER
            .comment("Whether to log a startup message during mod loading")
            .define("logStartup", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

    private ModConfig() {
    }
}
