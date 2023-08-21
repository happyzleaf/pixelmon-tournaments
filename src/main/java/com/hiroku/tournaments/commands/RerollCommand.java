package com.hiroku.tournaments.commands;

import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.enums.EnumTournamentState;
import com.hiroku.tournaments.rules.player.RandomPokemon;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Command used specifically when there is a {@link RandomPokemon} rule in effect, and a player
 * wishes to get a new set of random Pokémon.
 *
 * @author Hiroku
 */
public class RerollCommand implements CommandExecutor {
	public static CommandSpec getSpec() {
		return CommandSpec.builder()
				.permission("tournaments.command.common.reroll")
				.executor(new RerollCommand())
				.description(Text.of("Attempts to get a new set of random Pokémon"))
				.build();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) {
			src.sendMessage(Text.of(TextColors.RED, "You're the console, why would you be able to reroll!?"));
			return CommandResult.empty();
		}

		Player player = (Player) src;

		if (Tournament.instance() == null || Tournament.instance().state != EnumTournamentState.OPEN) {
			src.sendMessage(Text.of(TextColors.RED, "There must be a tournament in the open state for you to use this command!"));
			return CommandResult.empty();
		}
		Tournament t = Tournament.instance();

		if (t.getTeam(player.getUniqueId()) == null) {
			player.sendMessage(Text.of(TextColors.RED, "You aren't even in the tournament!"));
			return CommandResult.empty();
		}

		RandomPokemon rule = t.getRuleSet().getRule(RandomPokemon.class);
		if (rule == null) {
			player.sendMessage(Text.of(TextColors.RED, "This tournament isn't a random Pokémon tournament!"));
			return CommandResult.empty();
		}

		return rule.attemptReroll(player) ? CommandResult.success() : CommandResult.empty();
	}
}
