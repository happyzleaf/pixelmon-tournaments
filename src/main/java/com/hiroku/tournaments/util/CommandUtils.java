package com.hiroku.tournaments.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;

import java.util.Optional;

public class CommandUtils {
	public static <S, V> Optional<V> getOptArgument(CommandContext<S> context, final String name, final Class<V> clazz) {
		final ParsedArgument<S, ?> argument = context.arguments.get(name);
		if (argument == null) {
			return Optional.empty();
		}

		final Object result = argument.getResult();
		if (CommandContext.PRIMITIVE_TO_WRAPPER.getOrDefault(clazz, clazz).isAssignableFrom(result.getClass())) {
			//noinspection unchecked
			return Optional.of((V) result);
		} else {
			throw new IllegalArgumentException("Argument '" + name + "' is defined as " + result.getClass().getSimpleName() + ", not " + clazz);
		}
	}
}
