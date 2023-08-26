package com.hiroku.tournaments.commands;

import com.hiroku.tournaments.Tournaments;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.command.TournamentCommandWrapper;
import com.hiroku.tournaments.api.events.command.SubcommandEvent;
import com.hiroku.tournaments.enums.EnumTournamentState;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.rules.player.RandomPokemon;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;

/**
 * Base command executor for all tournament commands
 *
 * @author Hiroku
 */
public class TournamentsExecutor implements CommandExecutor {
	public static CommandSpec getSpec() {
		HashMap<List<String>, CommandSpec> children = new HashMap<List<String>, CommandSpec>();

		children.put(Collections.singletonList("reload"), ReloadCommand.getSpec());
		children.put(Arrays.asList("reroll", "roll"), RerollCommand.getSpec());
		children.put(Arrays.asList("flag", "bugged"), FlagCommand.getSpec());
		children.put(Arrays.asList("preset", "presets"), PresetsCommand.getSpec());
		children.put(Arrays.asList("reward", "rewards", "prize", "prizes"), RewardsCommand.getSpec());
		children.put(Collections.singletonList("leave"), LeaveCommand.getSpec());
		children.put(Arrays.asList("forfeit", "forfeitother"), ForfeitCommand.getSpec());
		children.put(Arrays.asList("zone", "zones"), ZoneCommand.getSpec());
		children.put(Arrays.asList("create", "make"), CreateCommand.getSpec());
		children.put(Arrays.asList("join", "enter"), JoinCommand.getSpec());
		children.put(Arrays.asList("rules", "rule"), RulesCommand.getSpec());
		children.put(Collections.singletonList("ignore"), IgnoreCommand.getSpec());
		children.put(Collections.singletonList("battlerules"), BattleRulesCommand.getSpec());

		SubcommandEvent event = new SubcommandEvent();
		Tournaments.EVENT_BUS.post(event);
		for (TournamentCommandWrapper tcw : event.subcommands)
			children.put(tcw.aliases, tcw.spec);

		HashMap<String, String> choices = new HashMap<>();
		choices.put("open", "open");
		choices.put("close", "close");
		choices.put("start", "start");

		return CommandSpec.builder()
				.description(Text.of("Base tournament command"))
				.executor(new TournamentsExecutor())
				.children(children)
				.permission("tournaments.command.common.tournament")
				.arguments(GenericArguments.optional(GenericArguments.choices(Text.of("open|close|start"), choices)))
				.build();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<String> optChoice = args.<String>getOne(Text.of("open|close|start"));
		if (optChoice.isPresent()) {
			if (!src.hasPermission("tournaments.command.admin.tournament")) {
				src.sendMessage(Text.of(TextColors.RED, "You don't have permission to do any of the tournament options!"));
				return CommandResult.empty();
			}

			String choice = optChoice.get();
			if (choice.equals("open")) {
				if (Tournament.instance() == null)
					src.sendMessage(Text.of(TextColors.RED, "There is no tournament to open. Try /tournament create"));
				else if (Tournament.instance().state == EnumTournamentState.OPEN)
					src.sendMessage(Text.of(TextColors.RED, "The tournament is already open. Pay attention."));
				else if (Tournament.instance().state == EnumTournamentState.ACTIVE)
					src.sendMessage(Text.of(TextColors.RED, "I mean, the tournament is up and running, so dunno what you're trying to do"));
				else {
					Tournament.instance().open();
					return CommandResult.success();
				}
			} else if (choice.equals("close")) {
				if (Tournament.instance() == null)
					src.sendMessage(Text.of(TextColors.RED, "It's a tad difficult to close a tournament that doesn't exist"));
				else {
					if (Tournament.instance().teams != null)
						for (Team team : Tournament.instance().teams)
							for (User user : team.users)
								// TODO logic changed and now removeRentalPokemon only works with online players and does nothing.
								//      make sure this logic is solid when player is offline
								RandomPokemon.removeRentalPokemon(user, true);
					Tournament.instance().close();
					src.sendMessage(Text.of(TextColors.GRAY, "Tournament closed."));
					return CommandResult.success();
				}
			} else if (choice.equals("start")) {
				if (Tournament.instance() == null || Tournament.instance().state == EnumTournamentState.CLOSED) {
					src.sendMessage(Text.of(TextColors.RED, "There is no open tournament to start. Have you tried /tournament open?"));
					return CommandResult.empty();
				} else if (Tournament.instance().state == EnumTournamentState.ACTIVE) {
					src.sendMessage(Text.of(TextColors.RED, "A tournament is already active. Close it if you want to make a new one."));
					return CommandResult.empty();
				}

				Tournament.instance().start();
			}
		}
		if (Tournament.instance() == null)
			src.sendMessage(Text.of(TextColors.RED, "No tournament"));
		else
			Tournament.instance().showTournament(src);
		return CommandResult.success();
	}
}