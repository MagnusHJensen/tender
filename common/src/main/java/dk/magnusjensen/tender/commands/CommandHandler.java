package dk.magnusjensen.tender.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dk.magnusjensen.tender.api.Tender;
import dk.magnusjensen.tender.api.TransactionResult;
import dk.magnusjensen.tender.config.ServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;

public class CommandHandler {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tender")
            .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
            .then(Commands.literal("info")
                .executes(commandContext -> {
                    CommandSourceStack source = commandContext.getSource();

                    // Format the API version, plus a list of providers ordered by priority, and which one is active.
                    StringBuilder sb = new StringBuilder();
                    sb.append("Tender API Version: ").append(Tender.API_VERSION).append("\n");
                    sb.append("Available Providers:\n");
                    Tender.api().availableProviders().forEach(provider -> {
                        sb.append("- ").append(provider.displayName()).append(" (Priority: ").append(provider.priority()).append(")");
                        if (Tender.api().activeProvider().map(p -> p.id().equals(provider.id())).orElse(false)) {
                            sb.append(" [ACTIVE]");
                        }
                        sb.append("\n");
                    });

                    source.sendSuccess(() -> Component.literal(sb.toString()), false);

                    return 1;
                })
            )
            .then(Commands.literal("give")
                .requires(commandSourceStack -> ServerConfig.REGISTER_COMMANDS)
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", LongArgumentType.longArg(1))
                        .executes(commandContext -> {
                            var player = EntityArgument.getPlayer(commandContext, "player");
                            var amount = LongArgumentType.getLong(commandContext, "amount");
                            return handleGiveCommand(commandContext.getSource(), player.getStringUUID(), amount);
                        })
                    )
                )
                .then(Commands.argument("playerUUID", UuidArgument.uuid())
                    .then(Commands.argument("amount", LongArgumentType.longArg(1))
                        .executes(commandContext -> {
                            var playerUUID = UuidArgument.getUuid(commandContext, "playerUUID").toString();
                            var amount = LongArgumentType.getLong(commandContext, "amount");
                            return handleGiveCommand(commandContext.getSource(), playerUUID, amount);
                        })
                    )
                )
            )
            .then(Commands.literal("take")
                .requires(commandSourceStack -> ServerConfig.REGISTER_COMMANDS)
                .then(Commands.argument("player", EntityArgument.player())
                    .then(Commands.argument("amount", LongArgumentType.longArg(1))
                        .executes(commandContext -> {
                            var player = EntityArgument.getPlayer(commandContext, "player");
                            var amount = LongArgumentType.getLong(commandContext, "amount");
                            return handleTakeCommand(commandContext.getSource(), player.getStringUUID(), amount);
                        })
                    )
                )
                .then(Commands.argument("playerUUID", UuidArgument.uuid())
                    .then(Commands.argument("amount", LongArgumentType.longArg(1))
                        .executes(commandContext -> {
                            var playerUUID = UuidArgument.getUuid(commandContext, "playerUUID").toString();
                            var amount = LongArgumentType.getLong(commandContext, "amount");
                            return handleTakeCommand(commandContext.getSource(), playerUUID, amount);
                        })
                    )
                )
            )
            .then(Commands.literal("transfer")
                .requires(commandSourceStack -> ServerConfig.REGISTER_COMMANDS)
                .then(Commands.argument("fromPlayer", EntityArgument.player())
                    .then(Commands.argument("toPlayer", EntityArgument.player())
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(commandContext -> {
                                var fromPlayer = EntityArgument.getPlayer(commandContext, "fromPlayer");
                                var toPlayer = EntityArgument.getPlayer(commandContext, "toPlayer");
                                var amount = LongArgumentType.getLong(commandContext, "amount");
                                return handleTransferCommand(commandContext.getSource(), fromPlayer.getStringUUID(), toPlayer.getStringUUID(), amount);
                            })
                        )
                    )
                )
                .then(Commands.argument("fromPlayer", EntityArgument.player())
                    .then(Commands.argument("toPlayerUUID", UuidArgument.uuid())
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(commandContext -> {
                                var fromPlayer = EntityArgument.getPlayer(commandContext, "fromPlayer");
                                var toPlayerUUID = UuidArgument.getUuid(commandContext, "toPlayerUUID").toString();
                                var amount = LongArgumentType.getLong(commandContext, "amount");
                                return handleTransferCommand(commandContext.getSource(), fromPlayer.getStringUUID(), toPlayerUUID, amount);
                            })
                        )
                    )
                )
                .then(Commands.argument("fromPlayerUUID", UuidArgument.uuid())
                    .then(Commands.argument("toPlayer", EntityArgument.player())
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(commandContext -> {
                                var fromPlayerUUID = UuidArgument.getUuid(commandContext, "fromPlayerUUID").toString();
                                var toPlayer = EntityArgument.getPlayer(commandContext, "toPlayer");
                                var amount = LongArgumentType.getLong(commandContext, "amount");
                                return handleTransferCommand(commandContext.getSource(), fromPlayerUUID, toPlayer.getStringUUID(), amount);
                            })
                        )
                    )
                )
                .then(Commands.argument("fromPlayerUUID", UuidArgument.uuid())
                    .then(Commands.argument("toPlayerUUID", UuidArgument.uuid())
                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                            .executes(commandContext -> {
                                var fromPlayerUUID = UuidArgument.getUuid(commandContext, "fromPlayerUUID").toString();
                                var toPlayerUUID = UuidArgument.getUuid(commandContext, "toPlayerUUID").toString();
                                var amount = LongArgumentType.getLong(commandContext, "amount");
                                return handleTransferCommand(commandContext.getSource(), fromPlayerUUID, toPlayerUUID, amount);
                            })
                        )
                    )
                )
            )
        );
    }

    public static int handleGiveCommand(CommandSourceStack source, String playerUUID, long amount) throws CommandSyntaxException {
        Tender.api().activeProvider().orElseThrow(() -> NO_ACTIVE_PROVIDER_EXCEPTION.create()).deposit(playerUUID, amount, "Admin command").whenComplete((result, ex) -> {
            if (ex != null) {
                source.sendFailure(Component.literal(
                    "An error occurred while trying to give " + amount + " to player with UUID " + playerUUID + ": " + ex.getMessage()
                ));
                return;
            }
            switch (result) {
                case TransactionResult.Success ignored -> source.sendSuccess(() -> Component.literal("Successfully gave " + amount + " to player with UUID " + playerUUID), false);
                case TransactionResult.Failure failure -> source.sendFailure(Component.literal("Failed to give " + amount + " to player with UUID " + playerUUID + ": " + failure.reason()));
                default -> source.sendFailure(Component.literal("Failed to give " + amount + " to player with UUID " + playerUUID + ": " + result.getClass().getSimpleName()));
            }
        });

        return 0;
    }

    public static int handleTakeCommand(CommandSourceStack source, String playerUUID, long amount) throws CommandSyntaxException {
        Tender.api().activeProvider().orElseThrow(() -> NO_ACTIVE_PROVIDER_EXCEPTION.create()).withdraw(playerUUID, amount, "Admin command").whenComplete((result, ex) -> {
            if (ex != null) {
                source.sendFailure(Component.literal(
                    "An error occurred while trying to take " + amount + " from player with UUID " + playerUUID + ": " + ex.getMessage()
                ));
                return;
            }
            switch (result) {
                case TransactionResult.Success ignored -> source.sendSuccess(() -> Component.literal("Successfully took " + amount + " from player with UUID " + playerUUID), false);
                case TransactionResult.Failure failure -> source.sendFailure(Component.literal("Failed to take " + amount + " from player with UUID " + playerUUID + ": " + failure.reason()));
                case TransactionResult.InsufficientFunds ignored -> source.sendFailure(Component.literal("Failed to take " + amount + " from player with UUID " + playerUUID + ": Insufficient funds"));
                default -> source.sendFailure(Component.literal("Failed to take " + amount + " from player with UUID " + playerUUID + ": " + result.getClass().getSimpleName()));
            }
        });

        return 0;
    }

    public static int handleTransferCommand(CommandSourceStack source, String fromPlayerUUID, String toPlayerUUID, long amount) throws CommandSyntaxException {
        Tender.api().activeProvider().orElseThrow(() -> NO_ACTIVE_PROVIDER_EXCEPTION.create()).transfer(fromPlayerUUID, toPlayerUUID, amount, "Admin command").whenComplete((result, ex) -> {
            if (ex != null) {
                source.sendFailure(Component.literal(
                    "An error occurred while trying to transfer " + amount + " from player with UUID " + fromPlayerUUID + " to player with UUID " + toPlayerUUID + ": " + ex.getMessage()
                ));
                return;
            }
            switch (result) {
                case TransactionResult.Success ignored -> source.sendSuccess(() -> Component.literal("Successfully transferred " + amount + " from player with UUID " + fromPlayerUUID + " to player with UUID " + toPlayerUUID), false);
                case TransactionResult.Failure failure -> source.sendFailure(Component.literal("Failed to transfer " + amount + " from player with UUID " + fromPlayerUUID + " to player with UUID " + toPlayerUUID + ": " + failure.reason()));
                case TransactionResult.InsufficientFunds ignored -> source.sendFailure(Component.literal("Failed to transfer " + amount + " from player with UUID " + fromPlayerUUID + " to player with UUID " + toPlayerUUID + ": Insufficient funds"));
                default -> source.sendFailure(Component.literal("Failed to transfer " + amount + " from player with UUID " + fromPlayerUUID + " to player with UUID " + toPlayerUUID + ": " + result.getClass().getSimpleName()));
            }
        });

        return 0;
    }

    public static SimpleCommandExceptionType NO_ACTIVE_PROVIDER_EXCEPTION = new SimpleCommandExceptionType(Component.literal("No active economy provider found"));
}
