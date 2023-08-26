package com.hiroku.tournaments.commands.elo;

import com.happyzleaf.tournaments.Text;
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

public class EloClearCommand implements Command<CommandSource> {
	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("clear")
//				.description(Text.of("Elo clear command"))
				.requires(source -> User.hasPermission(source, "tournaments.command.admin.elo.clear.base"))
				.executes(this)
				.then(
						Commands.argument("user", UserArgument.user())
								.executes(this)
								.then(
										Commands.argument("type", EnumArgument.enumArgument(EloTypes.class))
												.executes(this)
								)
				);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		User user = UserArgument.getOptUser(context, "user").orElse(null);
		EloTypes type = getOptArgument(context, "type", EloTypes.class).orElse(null);

		if (user == null) {
			if (!(context.getSource().getEntity() instanceof PlayerEntity)) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "You must specify a player!"), true);
				return 0;
			}

			user = new User(context.getSource().asPlayer());
		}

		if (!user.is(context.getSource()) && !User.hasPermission(context.getSource(), "tournaments.command.admin.elo.clear.other")) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "You don't have permission to clear the Elo of others!"), true);
			return 0;
		}

		if (type == null) {
			EloStorage.clearElo(user.id);
		} else {
			EloStorage.clearElo(user.id, type);
		}

		context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Successfully cleared " + (type == null ? "" : (type + " ")) + "Elo" + (user.is(context.getSource()) ? "" : (" from " + user.getName()))), true);

		return 1;
	}
}
