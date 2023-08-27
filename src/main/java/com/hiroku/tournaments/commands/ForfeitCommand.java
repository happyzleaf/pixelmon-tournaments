package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.text.Text;
import com.happyzleaf.tournaments.User;
import com.happyzleaf.tournaments.args.UserArgument;
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
 * Command for forfeiting during a tournament.
 *
 * @author Hiroku
 */
public class ForfeitCommand implements Command<CommandSource> {
	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("forfeit")
//				.description(Text.of(TextColors.RED, "Forfeits your team from the tournament."))
				.requires(source -> User.hasPermission(source, "tournaments.command.common.forfeit"))
				.executes(this)
				.then(
						Commands.argument("user", UserArgument.user())
								.executes(this)
				);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		if (Tournament.instance() == null || Tournament.instance().state == TournamentStates.CLOSED) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "There is no tournament dummy"), true);
			return 0;
		}

		boolean isActive = Tournament.instance().state == TournamentStates.ACTIVE;

		User user = UserArgument.getOptUser(context, "user").orElse(null);
		if (user != null) {
			if (!User.hasPermission(context.getSource(), "tournaments.command.admin.forfeit")) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "You don't have permission to forfeit others"), true);
				return 0;
			}
		} else if (!(context.getSource().getEntity() instanceof PlayerEntity)) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "What? You're forfeiting? You're the CONSOLE"), true);
			return 0;
		} else {
			user = new User(context.getSource().asPlayer());
		}

		boolean forced = !user.is(context.getSource());

		Team team = Tournament.instance().getTeam(user.id);

		if (team == null) {
			context.getSource().sendFeedback(Text.of(TextFormatting.DARK_AQUA, user.getName(), TextFormatting.RED, " isn't even in the tournament"), true);
			return 0;
		} else if (!isActive) {
			Tournament.instance().removeTeams(forced, team);
			return 1;
		}

		Tournament.instance().forfeitTeams(forced, team);

		return 1;
	}
}
