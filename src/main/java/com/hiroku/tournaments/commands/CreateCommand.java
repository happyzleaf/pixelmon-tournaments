package com.hiroku.tournaments.commands;

import com.hiroku.tournaments.Presets;
import com.hiroku.tournaments.Zones;
import com.hiroku.tournaments.api.Preset;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.enums.EnumTournamentState;
import com.hiroku.tournaments.obj.Zone;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class CreateCommand implements CommandExecutor {
	public static CommandSpec getSpec() {
		return CommandSpec.builder()
				.permission("tournaments.command.admin.create")
				.description(Text.of("Creates a new tournament"))
				.executor(new CreateCommand())
				.arguments(
						GenericArguments.optional(GenericArguments.string(Text.of("preset"))))
				.build();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (Tournament.instance() != null && Tournament.instance().state != EnumTournamentState.CLOSED) {
			if (Tournament.instance().state == EnumTournamentState.ACTIVE) {
				src.sendMessage(Text.of(TextColors.RED, "There is a tournament that's ACTIVE man"));
				return CommandResult.empty();
			}
			if (Tournament.instance().state == EnumTournamentState.OPEN) {
				src.sendMessage(Text.of(TextColors.RED, "There is a tournament that's open"));
				return CommandResult.empty();
			}
		}

		String arg = null;

		if (args.hasAny(Text.of("preset")))
			arg = args.<String>getOne(Text.of("preset")).get();

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
			src.sendMessage(Text.of(TextColors.DARK_GREEN, "Started a new tournament with preset: ", TextColors.DARK_AQUA, presetName));
		} else if (arg == null)
			src.sendMessage(Text.of(TextColors.DARK_GREEN, "Started a new tournament."));
		else
			src.sendMessage(Text.of(TextColors.DARK_GREEN, "Started a new tournament, but the preset given was unknown"));

		return CommandResult.success();
	}
}
