package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.text.Text;
import com.happyzleaf.tournaments.User;
import com.happyzleaf.tournaments.args.ChoiceSetArgument;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.enums.TournamentStates;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.rules.player.RandomPokemon;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.hiroku.tournaments.util.CommandUtils.getOptArgument;

/**
 * Base command executor for all tournament commands
 *
 * @author Hiroku
 */
public class TournamentsExecutor implements Command<CommandSource> {
	private static final Set<String> CHOICES = new HashSet<>(Arrays.asList("open", "close", "start"));

	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("tournaments")
//				.description(Text.of("Base tournament command"))
				.requires(source -> User.hasPermission(source, "tournaments.command.common.tournament"))
				.executes(this)
				.then(
						Commands.argument("open|close|start", ChoiceSetArgument.choiceSet(CHOICES))
								.executes(this)
				)
				.then(new ReloadCommand().create())
				.then(new RerollCommand().create())
				.then(new FlagCommand().create())
				.then(new PresetsCommand().create())
				.then(new RewardsCommand().create())
				.then(new LeaveCommand().create())
				.then(new ForfeitCommand().create())
				.then(new ZoneCommand().create())
				.then(new CreateCommand().create())
				.then(new JoinCommand().create())
				.then(new RulesCommand().create())
				.then(new IgnoreCommand().create())
				.then(new BattleRulesCommand().create());
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		String choice = getOptArgument(context, "open|close|start", String.class).orElse(null);
		if (choice != null) {
			if (!User.hasPermission(context.getSource(), "tournaments.command.admin.tournament")) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "You don't have permission to do any of the tournament options!"), true);
				return 0;
			}

			if (choice.equals("open")) {
				if (Tournament.instance() == null)
					context.getSource().sendFeedback(Text.of(TextFormatting.RED, "There is no tournament to open. Try /tournament create"), true);
				else if (Tournament.instance().state == TournamentStates.OPEN)
					context.getSource().sendFeedback(Text.of(TextFormatting.RED, "The tournament is already open. Pay attention."), true);
				else if (Tournament.instance().state == TournamentStates.ACTIVE)
					context.getSource().sendFeedback(Text.of(TextFormatting.RED, "I mean, the tournament is up and running, so dunno what you're trying to do"), true);
				else {
					Tournament.instance().open();
					return 1;
				}
			} else if (choice.equals("close")) {
				if (Tournament.instance() == null)
					context.getSource().sendFeedback(Text.of(TextFormatting.RED, "It's a tad difficult to close a tournament that doesn't exist"), true);
				else {
					if (Tournament.instance().teams != null)
						for (Team team : Tournament.instance().teams)
							for (User user : team.users)
								// TODO logic changed and now removeRentalPokemon only works with online players and does nothing.
								//      make sure this logic is solid when player is offline
								RandomPokemon.removeRentalPokemon(user, true);
					Tournament.instance().close();
					context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Tournament closed."), true);
					return 1;
				}
			} else if (choice.equals("start")) {
				if (Tournament.instance() == null || Tournament.instance().state == TournamentStates.CLOSED) {
					context.getSource().sendFeedback(Text.of(TextFormatting.RED, "There is no open tournament to start. Have you tried /tournament open?"), true);
					return 0;
				} else if (Tournament.instance().state == TournamentStates.ACTIVE) {
					context.getSource().sendFeedback(Text.of(TextFormatting.RED, "A tournament is already active. Close it if you want to make a new one."), true);
					return 0;
				}

				Tournament.instance().start();
			}
		}

		if (Tournament.instance() == null)
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No tournament"), true);
		else
			Tournament.instance().showTournament(context.getSource());

		return 1;
	}
}