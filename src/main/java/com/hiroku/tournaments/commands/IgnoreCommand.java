package com.hiroku.tournaments.commands;

import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.enums.EnumTournamentState;

public class IgnoreCommand implements CommandExecutor
{
	public static CommandSpec getSpec()
	{
		return CommandSpec.builder()
				.permission("tournaments.command.common.ignore")
				.executor(new IgnoreCommand())
				.description(Text.of("Toggles tournament messages"))
				.arguments(GenericArguments.optional(GenericArguments.bool(Text.of("yes/no"))))
				.build();
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
		if (!(src instanceof Player))
		{
			src.sendMessage(Text.of(TextColors.RED, "B-but... you're not even a player"));
			return CommandResult.empty();
		}
		
		if (Tournament.instance() == null || Tournament.instance().state == EnumTournamentState.CLOSED)
		{
			src.sendMessage(Text.of(TextColors.RED, "There's nothing to ignore; there's no tournament open"));
			return CommandResult.empty();
		}
		
		Player player = (Player)src;
		boolean ignore = !Tournament.instance().ignoreList.contains(player.getUniqueId());
		Optional<Boolean> optBool = args.<Boolean>getOne(Text.of("yes/no"));
		if (optBool.isPresent())
			ignore = optBool.get();
		
		Tournament.instance().ignoreList.remove(player.getUniqueId());
		if (ignore)
			Tournament.instance().ignoreList.add(player.getUniqueId());
		
		src.sendMessage(Tournament.instance().getMessageProvider().getIgnoreToggleMessage(ignore));
			
		return CommandResult.success();
	}
}
