package com.limbus.limbusexplore.command;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.sanity.SanityApi;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// 理智值调试命令，权限 2。战斗逻辑落地后删。
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class SanityCommands {

    private SanityCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("sanity")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("get").executes(SanityCommands::get))
                .then(Commands.literal("add")
                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                .executes(context -> add(context, IntegerArgumentType.getInteger(context, "amount")))))
                .then(Commands.literal("set")
                        .then(Commands.argument("value", IntegerArgumentType.integer(-45, 45))
                                .executes(context -> set(context, IntegerArgumentType.getInteger(context, "value")))))
                .then(Commands.literal("reset").executes(SanityCommands::reset)));
    }

    private static void tell(ServerPlayer player, int value) {
        player.sendSystemMessage(
                Component.translatable("command.limbusexplore.sanity_changed", value));
    }

    private static int get(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        tell(player, SanityApi.get(player));
        return 1;
    }

    private static int add(CommandContext<CommandSourceStack> context, int amount) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SanityApi.add(player, amount);
        tell(player, SanityApi.get(player));
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context, int value) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SanityApi.set(player, value);
        tell(player, SanityApi.get(player));
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        SanityApi.reset(player);
        tell(player, SanityApi.get(player));
        return 1;
    }
}
