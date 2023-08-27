package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.text.Text;
import com.happyzleaf.tournaments.User;
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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

/**
 * Command used specifically when there is a {@link RandomPokemon} rule in effect, and a player
 * wishes to get a new set of random Pokémon.
 *
 * @author Hiroku
 */
public class RerollCommand implements Command<CommandSource> {
	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("reroll")
//				.description(Text.of("Attempts to get a new set of random Pokémon"))
				.requires(source -> User.hasPermission(source, "tournaments.command.common.reroll"))
				.executes(this);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		PlayerEntity player = context.getSource().asPlayer();

		if (Tournament.instance() == null || Tournament.instance().state != TournamentStates.OPEN) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "There must be a tournament in the open state for you to use this command!"), true);
			return 0;
		}

		Team team = Tournament.instance().getTeam(player.getUniqueID());
		User user = team == null ? null : team.getUser(player.getUniqueID());
		if (team == null) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "You aren't even in the tournament!"), true);
			return 0;
		}

		RandomPokemon rule = Tournament.instance().getRuleSet().getRule(RandomPokemon.class);
		if (rule == null) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "This tournament isn't a random Pokémon tournament!"), true);
			return 0;
		}

		return rule.attemptReroll(user) ? 1 : 0;
	}
}
