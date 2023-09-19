package com.happyzleaf.tournaments.args;

import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.util.CommandUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Optional;
import java.util.UUID;

public class UserArgument {
    public static final SimpleCommandExceptionType USER_NOT_FOUND = new SimpleCommandExceptionType(new LiteralMessage("User not found."));

    public static ArgumentType<String> user() {
        return StringArgumentType.word();
    }

    private static User parse(String user) throws CommandSyntaxException {
        GameProfile profile = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getGameProfileForUsername(user);
        if (profile != null) {
            return new User(profile);
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(user);
        } catch (IllegalArgumentException e) {
            throw USER_NOT_FOUND.create();
        }

        profile = ServerLifecycleHooks.getCurrentServer().getPlayerProfileCache().getProfileByUUID(uuid);
        if (profile == null) {
            throw USER_NOT_FOUND.create();
        }

        return new User(profile);
    }

    public static User getUser(CommandContext<CommandSource> context, String name) throws CommandSyntaxException {
        String user = context.getArgument(name, String.class);
        return parse(user);
    }

    public static Optional<User> getOptUser(CommandContext<CommandSource> context, String name) throws CommandSyntaxException {
        String user = CommandUtils.getOptArgument(context, name, String.class).orElse(null);
        if (user == null) {
            return Optional.empty();
        }

        return Optional.of(parse(user));
    }

    public static SuggestionProvider<CommandSource> suggest() {
        return (context, builder) -> ISuggestionProvider.suggest(context.getSource().getServer().getOnlinePlayerNames(), builder);
    }
}
