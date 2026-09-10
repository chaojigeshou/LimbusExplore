package com.limbus.limbusexplore.command;

import com.limbus.limbusexplore.LimbusExplore;
import com.limbus.limbusexplore.chaos.ChaosApi;
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

/** 混乱值调试命令，权限 2。 */
@Mod.EventBusSubscriber(modid = LimbusExplore.MODID)
public final class ChaosCommands {

    private ChaosCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("chaos")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("get").executes(ChaosCommands::get))
                .then(Commands.literal("set")
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                                .executes(context -> set(context, IntegerArgumentType.getInteger(context, "value")))))
                .then(Commands.literal("damage")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                                .executes(context -> damage(context, IntegerArgumentType.getInteger(context, "amount")))))
                .then(Commands.literal("break").executes(ChaosCommands::breakNow))
                .then(Commands.literal("reset").executes(ChaosCommands::reset)));
    }

    private static void tell(ServerPlayer player) {
        player.sendSystemMessage(Component.translatable("command.limbusexplore.chaos_changed",
                ChaosApi.get(player), ChaosApi.getMax(player)));
    }

    private static int get(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        tell(player);
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context, int value) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ChaosApi.set(player, value);
        tell(player);
        return 1;
    }

    private static int damage(CommandContext<CommandSourceStack> context, int amount) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ChaosApi.damage(player, amount);
        tell(player);
        return 1;
    }

    private static int breakNow(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ChaosApi.breakNow(player);
        tell(player);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ChaosApi.reset(player);
        tell(player);
        return 1;
    }
}
