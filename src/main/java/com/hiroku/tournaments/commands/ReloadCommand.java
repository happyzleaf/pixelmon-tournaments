package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.text.Text;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.Presets;
import com.hiroku.tournaments.Tournaments;
import com.hiroku.tournaments.Zones;
import com.hiroku.tournaments.api.tiers.TierLoader;
import com.hiroku.tournaments.config.TournamentConfig;
import com.hiroku.tournaments.util.TournamentUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TextFormatting;

/**
 * Command for reloading configs and data.
 *
 * @author Hiroku
 */
public class ReloadCommand implements Command<CommandSource> {
	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("reload")
//				.description(Text.of("Reloads configs and data"))
				.requires(source -> User.hasPermission(source, "tournaments.command.admin.reload"))
				.executes(this);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Beginning reload..."), false);

		TournamentUtils.createDir("config/tournaments");
		TournamentUtils.createDir("data/tournaments");
		context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Consolidated config and data directories"), false);

		Tournaments.INSTANCE.registerDefaultRules();
		Tournaments.INSTANCE.registerDefaultRewards();
		context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Reloaded default rules and rewards"), false);

		TournamentConfig.load();
		context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Reloaded tournament config"), false);

		TierLoader.load();
		context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Reloaded default and custom tiers"), false);

		Zones.load();
		context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Reloaded zones"), false);

		Presets.load();
		context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Reloaded presets"), false);

		context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Reload successful."), false);

		return 1;
	}
}
