package com.hiroku.tournaments.commands.elo;

import com.happyzleaf.tournaments.Text;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.elo.EloStorage;
import com.hiroku.tournaments.elo.EloTypes;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.command.EnumArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EloListCommand implements Command<CommandSource> {
	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("list")
//				.description(Text.of("Elo leaderboard command"))
				.requires(source -> User.hasPermission(source, "tournaments.command.common.elo.list"))
				.executes(this)
				.then(
						Commands.argument("number", IntegerArgumentType.integer())
								.executes(this)
								.then(
										Commands.argument("type", EnumArgument.enumArgument(EloTypes.class))
												.executes(this)
								)
				);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		int x = IntegerArgumentType.getInteger(context, "number");
		EloTypes type = context.getArgument("type", EloTypes.class);

		List<UUID> top = EloStorage.getTopXElo(x, type);

		if (top.isEmpty()) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Nobody has an Elo rating yet."), true);
			return 0;
		}

		List<Text> contents = new ArrayList<>();
		for (UUID id : top) {
			contents.add(Text.of(TextFormatting.GOLD, EloStorage.getElo(id, type), ": ", TextFormatting.DARK_AQUA, new User(id).getName()));
		}

		// TODO: pagination
//		ps.builder().title(Text.of(TextFormatting.GOLD, "Top ", x, " ", type == null ? "Average" : type, " Elo Ratings"))
//				.linesPerPage(8)
//				.contents(contents)
//				.padding(Text.of(TextFormatting.GOLD, "-"))
//				.sendTo(src);

		return 1;
	}
}
