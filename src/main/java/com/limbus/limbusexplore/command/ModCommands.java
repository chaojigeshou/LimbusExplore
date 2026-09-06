package com.limbus.limbusexplore.command;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.sin.SinApi;
import com.limbus.limbusexplore.sin.SinType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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

// 调试命令，权限 2。正式玩法给资源后删掉。
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class ModCommands {

    private ModCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("sins")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("list").executes(ModCommands::list))
                .then(Commands.literal("add")
                        .then(Commands.argument("sin", StringArgumentType.word())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(context -> change(context, "add")))))
                .then(Commands.literal("set")
                        .then(Commands.argument("sin", StringArgumentType.word())
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(context -> change(context, "set")))))
                .then(Commands.literal("clear").executes(ModCommands::clear)));
    }

    private static int list(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        for (SinType type : SinType.values()) {
            player.sendSystemMessage(Component.literal(type.id + ": " + SinApi.get(player, type)));
        }
        return 1;
    }

    private static int change(CommandContext<CommandSourceStack> context, String action) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SinType type = SinType.byId(StringArgumentType.getString(context, "sin"));
        if (type == null) {
            player.sendSystemMessage(Component.translatable("command.limbusexplore.sin_unknown",
                    StringArgumentType.getString(context, "sin")));
            return 0;
        }
        int amount = IntegerArgumentType.getInteger(context, "amount");
        if ("add".equals(action)) {
            SinApi.add(player, type, amount);
        } else {
            SinApi.set(player, type, amount);
        }
        player.sendSystemMessage(Component.translatable("command.limbusexplore.sin_changed", type.id, SinApi.get(player, type)));
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SinApi.clear(player);
        player.sendSystemMessage(Component.translatable("command.limbusexplore.sin_cleared"));
        return 1;
    }
}
