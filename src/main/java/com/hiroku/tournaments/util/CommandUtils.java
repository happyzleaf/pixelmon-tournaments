package com.hiroku.tournaments.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CommandUtils {
    private static Field PRIMITIVE_TO_WRAPPER_f;
    private static Field arguments_f;

    private static <S> Map<String, ParsedArgument<S, ?>> getArguments(CommandContext<S> context) {
        try {
            if (arguments_f == null) {
                arguments_f = CommandContext.class.getDeclaredField("arguments");
                arguments_f.setAccessible(true);
            }

            //noinspection unchecked
            return (Map<String, ParsedArgument<S, ?>>) arguments_f.get(context);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<Class<?>, Class<?>> getPRIMITIVE_TO_WRAPPER() {
        try {
            if (PRIMITIVE_TO_WRAPPER_f == null) {
                PRIMITIVE_TO_WRAPPER_f = CommandContext.class.getDeclaredField("PRIMITIVE_TO_WRAPPER");
                PRIMITIVE_TO_WRAPPER_f.setAccessible(true);
            }

            //noinspection unchecked
            return (Map<Class<?>, Class<?>>) PRIMITIVE_TO_WRAPPER_f.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static <S, V> Optional<V> getOptArgument(CommandContext<S> context, final String name, final Class<V> clazz) {
        final ParsedArgument<S, ?> argument = getArguments(context).get(name);
        if (argument == null) {
            return Optional.empty();
        }

        final Object result = argument.getResult();
        if (getPRIMITIVE_TO_WRAPPER().getOrDefault(clazz, clazz).isAssignableFrom(result.getClass())) {
            //noinspection unchecked
            return Optional.of((V) result);
        } else {
            throw new IllegalArgumentException("Argument '" + name + "' is defined as " + result.getClass().getSimpleName() + ", not " + clazz);
        }
    }

    public static <S extends CommandSource> Optional<PlayerEntity> getOptPlayer(CommandContext<S> context, final String name) throws CommandSyntaxException {
        EntitySelector selector = getOptArgument(context, name, EntitySelector.class).orElse(null);
        if (selector == null) {
            return Optional.empty();
        }

        List<ServerPlayerEntity> players = selector.selectPlayers(context.getSource());
        if (players.isEmpty()) {
            return Optional.empty();
        } else if (players.size() > 1) {
            throw EntityArgument.TOO_MANY_PLAYERS.create();
        }

        return Optional.of(players.get(0));
    }
}
