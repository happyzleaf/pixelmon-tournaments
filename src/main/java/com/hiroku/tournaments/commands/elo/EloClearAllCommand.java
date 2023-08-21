package com.hiroku.tournaments.commands.elo;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.hiroku.tournaments.elo.EloStorage;

public class EloClearAllCommand implements CommandExecutor
{
	public static CommandSpec getSpec()
	{
		return CommandSpec.builder()
				.description(Text.of("Resets the Elo of every player"))
				.arguments(GenericArguments.optional(GenericArguments.string(Text.of("elo-type"))))
				.executor(new EloClearAllCommand())
				.permission("tournaments.command.admin.clearall")
				.build();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		String eloType = ctx.<String>getOne(Text.of("elo-type")).orElse(null);
		
		EloStorage.clearAllElos(eloType);
		src.sendMessage(Text.of(TextColors.DARK_GREEN, "Wiped all ", eloType == null ? "" : (eloType + " "), "Elo ratings."));
		
		return CommandResult.success();
	}
}
