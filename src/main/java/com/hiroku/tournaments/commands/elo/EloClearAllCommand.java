package com.hiroku.tournaments.commands.elo;

import com.happyzleaf.tournaments.text.Text;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.elo.EloStorage;
import com.hiroku.tournaments.elo.EloTypes;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.server.command.EnumArgument;

public class EloClearAllCommand implements Command<CommandSource> {
	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("clearall")
//				.description(Text.of("Resets the Elo of every player"))
				.requires(source -> User.hasPermission(source, "tournaments.command.admin.clearall"))
				.executes(this)
				.then(
						Commands.argument("type", EnumArgument.enumArgument(EloTypes.class))
								.executes(this)
				);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		EloTypes type = context.getArgument("type", EloTypes.class);
		EloStorage.clearAllElos(type);
		context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Wiped all ", type == null ? "" : (type + " "), "Elo ratings."), true);
		return 1;
	}
}
