package com.hiroku.tournaments.commands;

import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.enums.TournamentStates;
import com.hiroku.tournaments.obj.Team;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Command for forfeiting during a tournament.
 *
 * @author Hiroku
 */
public class ForfeitCommand implements CommandExecutor {
	public static CommandSpec getSpec() {
		return CommandSpec.builder()
				.permission("tournaments.command.common.forfeit")
				.description(Text.of(TextColors.RED, "Forfeits your team from the tournament."))
				.arguments(GenericArguments.optional(GenericArguments.user(Text.of("user"))))
				.executor(new ForfeitCommand())
				.build();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (Tournament.instance() == null || Tournament.instance().state == TournamentStates.CLOSED) {
			src.sendMessage(Text.of(TextColors.RED, "There is no tournament dummy"));
			return CommandResult.empty();
		}

		boolean isActive = Tournament.instance().state == TournamentStates.ACTIVE;

		User user = null;

		if (args.hasAny(Text.of("user"))) {
			if (src.hasPermission("tournaments.command.admin.forfeit"))
				user = args.<User>getOne(Text.of("user")).get();
			else {
				src.sendMessage(Text.of(TextColors.RED, "You don't have permission to forfeit others"));
				return CommandResult.empty();
			}
		} else if (!(src instanceof Player)) {
			src.sendMessage(Text.of(TextColors.RED, "What? You're forfeiting? You're the CONSOLE"));
			return CommandResult.empty();
		} else
			user = (User) src;

		boolean forced = src != user;

		Team team = Tournament.instance().getTeam(user.getUniqueId());

		if (team == null) {
			src.sendMessage(Text.of(TextColors.DARK_AQUA, user.getName(), TextColors.RED, " isn't even in the tournament"));
			return CommandResult.empty();
		} else if (!isActive) {
			Tournament.instance().removeTeams(forced, team);
			return CommandResult.success();
		}

		Tournament.instance().forfeitTeams(forced, team);

		return CommandResult.success();
	}
}
