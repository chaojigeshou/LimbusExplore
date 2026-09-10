package com.limbus.limbusexplore.command;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.combat.DamageKind;
import com.limbus.limbusexplore.combat.Resistance;
import com.limbus.limbusexplore.combat.ResistanceApi;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 三系抗性调试命令，权限 2：
 *   /resistance get
 *   /resistance set <slash|pierce|blunt> <倍率>     （0 免疫 / 0.5 耐性 / 1 普通 / 1.5 弱点 / 2 致命）
 *   /resistance preset <immune|endured|normal|weak|fatal>   一键三系
 */
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class ResistanceCommands {

    private ResistanceCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("resistance")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("get").executes(ResistanceCommands::get))
                .then(Commands.literal("set")
                        .then(Commands.argument("kind", StringArgumentType.word())
                                .then(Commands.argument("value", FloatArgumentType.floatArg(0f, 10f))
                                        .executes(ResistanceCommands::set))))
                .then(Commands.literal("preset")
                        .then(Commands.argument("tier", StringArgumentType.word())
                                .executes(ResistanceCommands::preset))));
    }

    private static void tell(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("command.limbusexplore.resistance_changed",
                String.format("%.2f", ResistanceApi.get(player, DamageKind.SLASH)),
                String.format("%.2f", ResistanceApi.get(player, DamageKind.PIERCE)),
                String.format("%.2f", ResistanceApi.get(player, DamageKind.BLUNT))));
    }

    private static int get(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        tell(player);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        DamageKind kind = DamageKind.byId(StringArgumentType.getString(context, "kind"));
        if (kind == null) {
            player.sendSystemMessage(Component.translatable("command.limbusexplore.resistance_unknown_kind",
                    StringArgumentType.getString(context, "kind")));
            return 0;
        }
        ResistanceApi.set(player, kind, FloatArgumentType.getFloat(context, "value"));
        tell(player);
        return 1;
    }

    private static int preset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        float value = switch (StringArgumentType.getString(context, "tier").toLowerCase()) {
            case "immune" -> Resistance.IMMUNE;
            case "endured" -> Resistance.ENDURED;
            case "weak" -> Resistance.WEAK;
            case "fatal" -> Resistance.FATAL;
            default -> Resistance.NORMAL;
        };
        ResistanceApi.apply(player, value, value, value);
        tell(player);
        return 1;
    }
}
