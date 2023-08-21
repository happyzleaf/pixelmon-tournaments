package com.hiroku.tournaments.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.enums.EnumTournamentState;
import com.hiroku.tournaments.obj.Team;

/**
 * Command for leaving a tournament.
 * 
 * @author Hiroku
 */
public class LeaveCommand implements CommandExecutor
{
	public static CommandSpec getSpec()
	{
		return CommandSpec.builder()
				.permission("tournaments.command.common.leave")
				.executor(new LeaveCommand())
				.description(Text.of("Leaves the tournament"))
				.build();
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
		if (!(src instanceof Player))
		{
			src.sendMessage(Text.of(TextColors.RED, "You can't leave the tournament if you're a CONSOLE. Jeez."));
			return CommandResult.empty();
		}
		
		Player player = (Player)src;
		if (Tournament.instance() == null || Tournament.instance().state == EnumTournamentState.CLOSED)
		{
			src.sendMessage(Text.of(TextColors.RED, "There is no tournament to leave"));
			return CommandResult.empty();
		}
		Team team = Tournament.instance().getTeam(player.getUniqueId());
		if (team == null)
		{
			src.sendMessage(Text.of(TextColors.RED, "You aren't even in the tournament!"));
			return CommandResult.empty();
		}
		if (!team.alive)
		{
			src.sendMessage(Text.of(TextColors.RED, "Why do you want to leave? You're already knocked out!"));
			return CommandResult.empty();
		}
		
		Match match = Tournament.instance().getMatch(team);
		if (match != null)
		{
			src.sendMessage(Text.of(TextColors.RED, "You are assigned to a match. You should use ", TextColors.DARK_AQUA, "/tournament forfeit", TextColors.RED, " instead"));
			return CommandResult.empty();
		}
		
		Tournament.instance().removeTeams(false, team);
		return CommandResult.success();
	}
}
