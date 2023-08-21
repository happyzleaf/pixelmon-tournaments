package com.hiroku.tournaments.commands.elo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

import com.hiroku.tournaments.elo.EloStorage;

public class EloExecutor implements CommandExecutor
{
	public static CommandSpec getSpec()
	{
		HashMap<List<String>, CommandSpec> children = new HashMap<List<String>, CommandSpec>();

		children.put(Arrays.asList("clear", "reset", "wipe"), EloClearCommand.getSpec());
		children.put(Arrays.asList("clearall", "resetall", "wipeall"), EloClearAllCommand.getSpec());
		children.put(Arrays.asList("list", "top"), EloListCommand.getSpec());
		
		return CommandSpec.builder()
				.description(Text.of("Base Elo command"))
				.executor(new EloExecutor())
				.children(children)
				.permission("tournaments.command.common.elo.base")
				.arguments(
						GenericArguments.optionalWeak(GenericArguments.user(Text.of("user"))),
						GenericArguments.optional(GenericArguments.string(Text.of("elo-type"))))
				.build();
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException
	{
		User user = ctx.<User>getOne(Text.of("user")).orElse(null);
		
		if (user == null && !(src instanceof Player))
		{
			src.sendMessage(Text.of(TextColors.RED, "You must specify a player!"));
			return CommandResult.empty();
		}
		else if (user == null)
			user = (User)src;
		
		String eloType = ctx.<String>getOne(Text.of("elo-type")).orElse(null);
		
		if (user != src && !src.hasPermission("landlord.command.common.elo.other"))
		{
			src.sendMessage(Text.of(TextColors.RED, "You don't have permission to check the Elo of other players!"));
			return CommandResult.empty();
		}
		
		int elo;
		if (eloType == null)
			elo = EloStorage.getAverageElo(user.getUniqueId());
		else
			elo = EloStorage.getElo(user.getUniqueId(), eloType);
		
		if (src == user)
			src.sendMessage(Text.of(TextColors.GOLD, eloType == null ? "Average" : eloType, " Elo: " + elo));
		else
			src.sendMessage(Text.of(TextColors.DARK_AQUA, user.getName() + "'s ", TextColors.GOLD, eloType == null ? "Average" : eloType, " Elo: " + elo));
		
		return CommandResult.success();
	}
}
