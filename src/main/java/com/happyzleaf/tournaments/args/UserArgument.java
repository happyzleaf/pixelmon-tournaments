package com.happyzleaf.tournaments.args;

import com.happyzleaf.tournaments.User;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.EntitySelector;
import net.minecraft.command.arguments.EntitySelectorParser;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.hiroku.tournaments.util.CommandUtils.getOptArgument;

public class UserArgument implements ArgumentType<UserArgument.IUserProvider> {
	private static final Collection<String> EXAMPLES = Arrays.asList("nothappyz", "d361de31-8f99-4662-a726-88493d0efc78");

	public static User getUser(CommandContext<CommandSource> context, String key) throws CommandSyntaxException {
		return context.getArgument(key, IUserProvider.class).getUser(context.getSource());
	}

	public static Optional<User> getOptUser(CommandContext<CommandSource> context, String key) throws CommandSyntaxException {
		IUserProvider provider = getOptArgument(context, key, IUserProvider.class).orElse(null);
		if (provider == null) {
			return Optional.empty();
		}

		return Optional.of(provider.getUser(context.getSource()));
	}

	public static UserArgument user() {
		return new UserArgument();
	}

	@Override
	public IUserProvider parse(StringReader reader) throws CommandSyntaxException {
		if (reader.canRead() && reader.peek() == '@') {
			EntitySelector entitySelector = new EntitySelectorParser(reader).parse();
			if (entitySelector.includesEntities()) {
				throw EntityArgument.ONLY_PLAYERS_ALLOWED.create();
			} else {
				return new UserProvider(entitySelector);
			}
		} else {
			int cursor = reader.getCursor();
			while (reader.canRead() && reader.peek() != ' ') {
				reader.skip();
			}

			String s = reader.getString().substring(cursor, reader.getCursor());
			return (source) -> {
				GameProfile profile = source.getServer().getPlayerProfileCache().getGameProfileForUsername(s);
				if (profile == null) {
					throw GameProfileArgument.PLAYER_UNKNOWN.create();
				}

				return new User(profile);
			};
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		if (context.getSource() instanceof ISuggestionProvider) {
			return ISuggestionProvider.suggest(
					ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()
							.stream()
							.map((player) -> player.getGameProfile().getName()),
					builder
			);
		}

		return Suggestions.empty();
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	@FunctionalInterface
	public interface IUserProvider {
		User getUser(CommandSource source) throws CommandSyntaxException;
	}

	public static class UserProvider implements IUserProvider {
		private final EntitySelector selector;

		public UserProvider(EntitySelector selector) {
			this.selector = selector;
		}

		public User getUser(CommandSource source) throws CommandSyntaxException {
			List<ServerPlayerEntity> list = this.selector.selectPlayers(source);
			if (list.isEmpty()) {
				throw EntityArgument.PLAYER_NOT_FOUND.create();
			} else if (list.size() != 1) {
				throw EntityArgument.TOO_MANY_PLAYERS.create();
			}
			return new User(list.get(0));
		}
	}
}
