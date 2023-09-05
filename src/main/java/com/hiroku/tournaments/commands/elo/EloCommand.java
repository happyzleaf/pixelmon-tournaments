package com.hiroku.tournaments.commands.elo;

import com.happyzleaf.tournaments.text.Text;
import com.happyzleaf.tournaments.User;
import com.happyzleaf.tournaments.args.UserArgument;
import com.hiroku.tournaments.elo.EloStorage;
import com.hiroku.tournaments.elo.EloTypes;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.command.EnumArgument;

import static com.hiroku.tournaments.util.CommandUtils.getOptArgument;

public class EloCommand implements Command<CommandSource> {
	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("elo")
//				.description(Text.of("Base Elo command"))
				.requires(source -> User.hasPermission(source, "tournaments.command.common.elo.base"))
				.executes(this)
				.then(
						Commands.argument("user", UserArgument.user())
								.executes(this)
								.then(
										Commands.argument("type", EnumArgument.enumArgument(EloTypes.class))
												.executes(this)
								)
				)
				.then(new EloClearCommand().create())
				.then(new EloClearAllCommand().create())
				.then(new EloListCommand().create());
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		User user = UserArgument.getOptUser(context, "user").orElse(null);
		EloTypes type = getOptArgument(context, "type", EloTypes.class).orElse(null);

		if (user == null) {
			if (!(context.getSource().getEntity() instanceof PlayerEntity)) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "You must specify a player!"), false);
				return 0;
			}

			user = new User(context.getSource().asPlayer());
		}

		if (!user.is(context.getSource()) && !User.hasPermission(context.getSource(), "tournaments.command.common.elo.other")) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "You don't have permission to check the Elo of other players!"), false);
			return 0;
		}

		int elo;
		if (type == null) {
			elo = EloStorage.getAverageElo(user.id);
		} else {
			elo = EloStorage.getElo(user.id, type);
		}

		if (user.is(context.getSource())) {
			context.getSource().sendFeedback(Text.of(TextFormatting.GOLD, type == null ? "Average" : type, " Elo: " + elo), false);
		} else {
			context.getSource().sendFeedback(Text.of(TextFormatting.DARK_AQUA, user.getName() + "'s ", TextFormatting.GOLD, type == null ? "Average" : type, " Elo: " + elo), false);
		}

		return 1;
	}
}
