package com.hiroku.tournaments.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.hiroku.tournaments.api.Tournament;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;

public class BattleRulesCommand implements CommandExecutor
{
	public static CommandSpec getSpec()
	{
		return CommandSpec.builder()
				.description(Text.of("Sets the Pixelmon battle rules. The battle rule argument should be the exported battle rules but instead of new lines, use commas"))
				.permission("tournaments.command.admin.battlerules")
				.arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("<clear | battle rules export text"))))
				.executor(new BattleRulesCommand())
				.build();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{	
		if (Tournament.instance() == null)
		{
			src.sendMessage(Text.of(TextColors.RED, "No tournament to set battle rules for. Try /tournament create"));
			return CommandResult.empty();
		}
		
		String arg = args.<String>getOne(Text.of("<clear | battle rules export text")).orElse("clear");
		if (arg.equalsIgnoreCase("clear"))
		{
			Tournament.instance().getRuleSet().br = new BattleRules();
			src.sendMessage(Text.of(TextColors.GREEN, "Cleared the battle rules."));
			return CommandResult.success();
		}
		else
		{
			String lineSep = arg.replaceAll(",", "\n");
			Tournament.instance().getRuleSet().br.importText(lineSep);
			src.sendMessage(Text.of(TextColors.GREEN, "Imported battle rules text. Use /tournament to check them."));
			return CommandResult.success();
		}
	}
}
