package com.limbus.limbusexplore.sin;

import com.limbus.limbusexplore.LimbusExplore;
import net.minecraft.resources.ResourceLocation;

// 七种罪孽。id 同时是注册名、lang 键后缀和同步包数组下标的来源，别乱改。
// 图标在 textures/hud/sin_<id>.png；color 暂时只做粒子、调试用。
public enum SinType {

    WRATH("wrath", 0xFFD32F2F),
    LUST("lust", 0xFFE55FA0),
    SLOTH("sloth", 0xFF3BC6A8),
    GLUTTONY("gluttony", 0xFF4A86E8),
    GLOOM("gloom", 0xFF5B6C94),
    PRIDE("pride", 0xFFF2C94C),
    ENVY("envy", 0xFF3FA34D);

    public final String id;
    public final int color;

    SinType(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public String displayKey() {
        return "sin." + LimbusExplore.MODID + "." + id;
    }

    public ResourceLocation texture() {
        return new ResourceLocation(LimbusExplore.MODID, "textures/hud/sin_" + id + ".png");
    }

    public static SinType byId(String id) {
        for (SinType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return null;
    }
}
