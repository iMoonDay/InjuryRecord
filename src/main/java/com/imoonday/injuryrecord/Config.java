package com.imoonday.injuryrecord;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {

    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.IntValue maxDataCount;
    public static ForgeConfigSpec.BooleanValue removePermission;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupConfig(configBuilder);
        SPEC = configBuilder.build();
    }

    private static void setupConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("The maximum number of records that can be stored");
        builder.comment("最大存储记录量");
        maxDataCount = builder.defineInRange("maxDataCount", 10000, 1, Integer.MAX_VALUE);

        builder.comment("Whether to require permission to delete records");
        builder.comment("删除是否需要权限");
        removePermission = builder.define("removePermission", false);
    }
}
