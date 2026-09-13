package com.limbus.limbusexplore.compat.jade;

import com.limbus.limbusexplore.combat.DamageKind;
import com.limbus.limbusexplore.combat.Resistance;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * 客户端侧显示：
 *   第一行常驻——混乱值（满了或者混乱中会标红）
 *   详细模式（按住 Shift / 展开）——三系抗性，按档位配色
 */
public class JadeEntityComponent implements IEntityComponentProvider {

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
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
            // 混乱值越低越危险：低于一半标黄，满了标绿
            ChatFormatting color = chaos <= max / 4 ? ChatFormatting.RED
                    : chaos <= max / 2 ? ChatFormatting.YELLOW
                    : ChatFormatting.GREEN;
            tooltip.add(Component.translatable("jade.limbusexplore.chaos", chaos, max).withStyle(color));
        }

        if (accessor.showDetails()) {
            tooltip.add(Component.translatable("jade.limbusexplore.resist",
                    styled(data.getFloat("resist_" + DamageKind.SLASH.id)),
                    styled(data.getFloat("resist_" + DamageKind.PIERCE.id)),
                    styled(data.getFloat("resist_" + DamageKind.BLUNT.id))));
        }
    }

    /** 抗性数值按档位配色：致命红、弱点金、普通白、耐性蓝、免疫灰 */
    private static Component styled(float value) {
        ChatFormatting color;
        if (value >= Resistance.FATAL) {
            color = ChatFormatting.RED;
        } else if (value >= Resistance.WEAK) {
            color = ChatFormatting.GOLD;
        } else if (value <= Resistance.IMMUNE) {
            color = ChatFormatting.DARK_GRAY;
        } else if (value < Resistance.NORMAL) {
            color = ChatFormatting.AQUA;
        } else {
            color = ChatFormatting.WHITE;
        }
        return Component.literal(trim(value)).withStyle(color);
    }

    private static String trim(float value) {
        return value == Math.floor(value) ? String.valueOf((int) value) : String.format("%.1f", value);
    }

    @Override
    public ResourceLocation getUid() {
        return JadeEntityData.UID;
    }
}
