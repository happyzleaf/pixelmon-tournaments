package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.text.Text;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.enums.TournamentStates;
import com.hiroku.tournaments.obj.Team;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

/**
 * Command for leaving a tournament.
 *
 * @author Hiroku
 */
public class LeaveCommand implements Command<CommandSource> {
	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("leave")
//				.description(Text.of("Leaves the tournament"))
				.requires(source -> User.hasPermission(source, "tournaments.command.common.leave"))
				.executes(this);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		PlayerEntity player = context.getSource().asPlayer();
		if (Tournament.instance() == null || Tournament.instance().state == TournamentStates.CLOSED) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "There is no tournament to leave"), true);
			return 0;
		}
		Team team = Tournament.instance().getTeam(player.getUniqueID());
		if (team == null) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "You aren't even in the tournament!"), true);
			return 0;
		}
		if (!team.alive) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Why do you want to leave? You're already knocked out!"), true);
			return 0;
		}

		Match match = Tournament.instance().getMatch(team);
		if (match != null) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "You are assigned to a match. You should use ", TextFormatting.DARK_AQUA, "/tournament forfeit", TextFormatting.RED, " instead"), true);
			return 0;
		}

		Tournament.instance().removeTeams(false, team);
		return 1;
	}
}
