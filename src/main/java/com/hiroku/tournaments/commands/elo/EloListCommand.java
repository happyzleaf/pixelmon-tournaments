package com.hiroku.tournaments.commands.elo;

import com.hiroku.tournaments.config.TournamentConfig;
import com.hiroku.tournaments.elo.EloStorage;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EloListCommand implements CommandExecutor {
	public static CommandSpec getSpec() {
		return CommandSpec.builder()
				.description(Text.of("Elo leaderboard command"))
				.executor(new EloListCommand())
				.permission("tournaments.command.common.elo.list")
				.arguments(
						GenericArguments.optionalWeak(GenericArguments.integer(Text.of("number"))),
						GenericArguments.optional(GenericArguments.string(Text.of("elo-type"))))
				.build();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
		PaginationService ps = Sponge.getServiceManager().provideUnchecked(PaginationService.class);

		int x = ctx.<Integer>getOne(Text.of("number")).orElse(TournamentConfig.INSTANCE.defaultEloTopNumber);
		String eloType = ctx.<String>getOne(Text.of("elo-type")).orElse(null);

		List<UUID> top = EloStorage.getTopXElo(x, eloType);

		if (top.isEmpty()) {
			src.sendMessage(Text.of(TextColors.RED, "Nobody has an Elo rating yet."));
			return CommandResult.empty();
		}

		List<Text> contents = new ArrayList<>();
		for (UUID uuid : top) {
			String name = uss.get(uuid).get().getName();
			contents.add(Text.of(TextColors.GOLD, EloStorage.getElo(uuid, eloType), ": ", TextColors.DARK_AQUA, name));
		}

		ps.builder().title(Text.of(TextColors.GOLD, "Top ", x, " ", eloType == null ? "Average" : eloType, " Elo Ratings"))
				.linesPerPage(8)
				.contents(contents)
				.padding(Text.of(TextColors.GOLD, "-"))
				.sendTo(src);

		return CommandResult.success();
	}
}
