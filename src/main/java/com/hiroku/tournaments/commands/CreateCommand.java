package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.text.Text;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.Presets;
import com.hiroku.tournaments.Zones;
import com.hiroku.tournaments.api.Preset;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.enums.TournamentStates;
import com.hiroku.tournaments.obj.Zone;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TextFormatting;

import static com.hiroku.tournaments.util.CommandUtils.getOptArgument;

public class CreateCommand implements Command<CommandSource> {
	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("create")
//				.description(Text.of("Creates a new tournament"))
				.requires(source -> User.hasPermission(source, "tournaments.command.admin.create"))
				.executes(this)
				.then(
						Commands.argument("preset", StringArgumentType.greedyString())
								.executes(this)
				);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		if (Tournament.instance() != null && Tournament.instance().state != TournamentStates.CLOSED) {
			if (Tournament.instance().state == TournamentStates.ACTIVE) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "There is a tournament that's ACTIVE man"), true);
				return 0;
			}
			if (Tournament.instance().state == TournamentStates.OPEN) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "There is a tournament that's open"), true);
				return 0;
			}
		}

		String arg = getOptArgument(context, "preset", String.class).orElse(null);

		new Tournament();

		Preset preset = null;
		String presetName = null;
		if (arg != null) {
			if (Presets.getPreset(arg) != null) {
				preset = Presets.getPreset(arg);
				presetName = Presets.getMatchingKey(arg);
			}
		}
		if (preset != null) {
			Tournament.instance().setRuleSet(preset.ruleSet);
			Tournament.instance().rewards.addAll(preset.rewards);
			if (!preset.zones.isEmpty()) {
				for (Zone zone : Zones.INSTANCE.getZones()) {
					zone.engaged = preset.zones.contains(zone);
				}
			}
			context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Started a new tournament with preset: ", TextFormatting.DARK_AQUA, presetName), true);
		} else if (arg == null) {
			context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Started a new tournament."), true);
		} else {
			context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Started a new tournament, but the preset given was unknown"), true);
		}

		return 1;
	}
}
