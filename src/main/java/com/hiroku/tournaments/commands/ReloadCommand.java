package com.hiroku.tournaments.commands;

import com.hiroku.tournaments.Presets;
import com.hiroku.tournaments.Tournaments;
import com.hiroku.tournaments.Zones;
import com.hiroku.tournaments.api.tiers.TierLoader;
import com.hiroku.tournaments.config.TournamentConfig;
import com.hiroku.tournaments.util.TournamentUtils;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Command for reloading configs and data.
 *
 * @author Hiroku
 */
public class ReloadCommand implements CommandExecutor {
	public static CommandSpec getSpec() {
		return CommandSpec.builder()
				.permission("tournaments.command.admin.reload")
				.executor(new ReloadCommand())
				.description(Text.of("Reloads configs and data"))
				.build();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		src.sendMessage(Text.of(TextColors.GRAY, "Beginning reload..."));

		TournamentUtils.createDir("config/tournaments");
		TournamentUtils.createDir("data/tournaments");

		src.sendMessage(Text.of(TextColors.GRAY, "Consolidated config and data directories"));

		Tournaments.INSTANCE.registerDefaultRules();
		Tournaments.INSTANCE.registerDefaultRewards();

		src.sendMessage(Text.of(TextColors.GRAY, "Reloaded default rules and rewards"));

		TournamentConfig.load();

		src.sendMessage(Text.of(TextColors.GRAY, "Reloaded tournament config"));

		TierLoader.load();

		src.sendMessage(Text.of(TextColors.GRAY, "Reloaded default and custom tiers"));

		Zones.load();

		src.sendMessage(Text.of(TextColors.GRAY, "Reloaded zones"));

		Presets.load();

		src.sendMessage(Text.of(TextColors.GRAY, "Reloaded presets"));

		src.sendMessage(Text.of(TextColors.DARK_GREEN, "Reload successful."));

		return CommandResult.success();
	}
}
