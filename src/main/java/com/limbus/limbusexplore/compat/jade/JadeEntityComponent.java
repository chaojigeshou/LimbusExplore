package com.limbus.limbusexplore.compat.jade;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.combat.DamageKind;
import com.limbus.limbusexplore.combat.Resistance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

/**
 * 客户端侧显示：
 *   第一行常驻——混乱值（越低越红）
 *   详细模式（Shift）——三系抗性，每系一行：[图标] ×倍率，倍率按档位配色
 */
public class JadeEntityComponent implements IEntityComponentProvider {

    /** 三系图标（美工资源里那三张 16x16 的） */
    private static final float ICON_SIZE = 10f;

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        // 官方绕过入口：Jade 自己的设置界面里能关掉这一项
        if (!config.get(JadeEntityData.UID)) {
            return;
        }
        CompoundTag data = accessor.getServerData();
        if (!data.contains("chaos")) {
            return;
        }

        int chaos = data.getInt("chaos");
        int max = Math.max(1, data.getInt("chaosMax"));
        int ticks = data.getInt("chaosTicks");

        if (ticks > 0) {
            int seconds = (ticks + 19) / 20;
            tooltip.add(Component.translatable("jade.limbusexplore.chaos_active", seconds)
                    .withStyle(ChatFormatting.RED));
        } else {
            ChatFormatting color = chaos <= max / 4 ? ChatFormatting.RED
                    : chaos <= max / 2 ? ChatFormatting.YELLOW
                    : ChatFormatting.GREEN;
            tooltip.add(Component.translatable("jade.limbusexplore.chaos", chaos, max).withStyle(color));
        }

        if (!accessor.showDetails()) {
            return;
        }

        IElementHelper helper = IElementHelper.get();
        for (DamageKind kind : DamageKind.values()) {
            float value = data.getFloat("resist_" + kind.id);
            int line = tooltip.size();   // 每系占一行
            tooltip.add(line, new TextureIcon(iconOf(kind), ICON_SIZE));
            tooltip.add(line, helper.spacer(2, 0));
            tooltip.add(line, helper.text(Component.literal("×" + trim(value)).withStyle(tierColor(value))));
        }
    }

    private static ResourceLocation iconOf(DamageKind kind) {
        return new ResourceLocation(LimbusExplore.MODID, "textures/hud/damage_" + kind.id + ".png");
    }

    /** 抗性数值按档位配色：致命红、弱点金、普通白、耐性蓝、免疫灰 */
    private static ChatFormatting tierColor(float value) {
        if (value >= Resistance.FATAL) {
            return ChatFormatting.RED;
        }
        if (value >= Resistance.WEAK) {
            return ChatFormatting.GOLD;
        }
        if (value <= Resistance.IMMUNE) {
            return ChatFormatting.DARK_GRAY;
        }
        if (value < Resistance.NORMAL) {
            return ChatFormatting.AQUA;
        }
        return ChatFormatting.WHITE;
    }

    private static String trim(float value) {
        return value == Math.floor(value) ? String.valueOf((int) value) : String.format("%.1f", value);
    }

    @Override
    public ResourceLocation getUid() {
        return JadeEntityData.UID;
    }

    /** 直接画一张贴图的小图标元素（Jade 的 Element 基类只要实现 render + getSize） */
    private static final class TextureIcon extends Element {

        private final ResourceLocation texture;
        private final float size;

        TextureIcon(ResourceLocation texture, float size) {
            this.texture = texture;
            this.size = size;
        }

        @Override
        public Vec2 getSize() {
            return new Vec2(size, size);
        }

        @Override
        public void render(GuiGraphics gui, float x, float y, float maxX, float maxY) {
            int s = (int) size;
            gui.blit(texture, (int) x, (int) y, 0, 0, s, s, s, s);
        }
    }
}
