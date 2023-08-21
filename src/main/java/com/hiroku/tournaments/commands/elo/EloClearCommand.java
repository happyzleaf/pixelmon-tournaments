package com.hiroku.tournaments.commands.elo;

import com.hiroku.tournaments.elo.EloStorage;
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

public class EloClearCommand implements CommandExecutor {
	public static CommandSpec getSpec() {
		return CommandSpec.builder()
				.description(Text.of("Elo clear command"))
				.executor(new EloClearCommand())
				.permission("tournaments.command.admin.elo.clear.base")
				.arguments(GenericArguments.optionalWeak(GenericArguments.user(Text.of("user"))),
						GenericArguments.optional(GenericArguments.string(Text.of("elo-type"))))
				.build();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext ctx) throws CommandException {
		User user = ctx.<User>getOne(Text.of("user")).orElse(null);
		String eloType = ctx.<String>getOne(Text.of("elo-type")).orElse(null);

		if (user == null && !(src instanceof Player)) {
			src.sendMessage(Text.of(TextColors.RED, "You must specify a player!"));
			return CommandResult.empty();
		} else if (user == null)
			user = (User) src;

		if (user != src && !src.hasPermission("tournaments.command.admin.elo.clear.other")) {
			src.sendMessage(Text.of(TextColors.RED, "You don't have permission to clear the Elo of others!"));
			return CommandResult.empty();
		}

		if (eloType == null)
			EloStorage.clearElo(user.getUniqueId());
		else
			EloStorage.clearElo(user.getUniqueId(), eloType);

		src.sendMessage(Text.of(TextColors.DARK_GREEN, "Successfully cleared " + (eloType == null ? "" : (eloType + " "))
				+ "Elo" + (user == src ? "" : (" from " + user.getName()))));


		return CommandResult.success();
	}
}
