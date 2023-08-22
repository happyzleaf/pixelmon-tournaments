package com.hiroku.tournaments.commands;

import com.google.common.collect.ImmutableMap;
import com.hiroku.tournaments.Presets;
import com.hiroku.tournaments.Zones;
import com.hiroku.tournaments.api.Preset;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.obj.Zone;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.HashMap;
import java.util.Optional;

/**
 * Command for checking, saving, loading, and deleting presets.
 *
 * @author Hiroku
 */
public class PresetsCommand implements CommandExecutor {
	public static CommandSpec getSpec() {
		HashMap<String, String> choices = new HashMap<>();
		choices.put("save", "save");
		choices.put("load", "load");
		choices.put("delete", "delete");
		choices.put("rename", "rename");
		choices.put("", "check");

		return CommandSpec.builder()
				.permission("tournaments.command.admin.presets")
				.description(Text.of("Adds, removes, or checks rule preset details"))
				.arguments(
						GenericArguments.optional(GenericArguments.choices(Text.of("option"), choices)),
						GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("preset-name"))))
				.executor(new PresetsCommand())
				.build();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		Optional<String> optChoice = args.<String>getOne(Text.of("option"));
		String choice = "check";
		if (optChoice.isPresent())
			choice = optChoice.get();
		Optional<String> optPresetName = args.<String>getOne(Text.of("preset-name"));
		if (!choice.equals("check") && !optPresetName.isPresent()) {
			src.sendMessage(Text.of(TextColors.RED, "Missing argument: preset-name"));
			return CommandResult.empty();
		}

		if (choice.equals("check")) {
			boolean isPlayer = src instanceof Player;
			ImmutableMap<String, Preset> presets = Presets.getPresets();
			src.sendMessage(Text.of(TextColors.GOLD, "------- Rule Presets --------"));
			src.sendMessage(Text.of(TextColors.GRAY, TextStyles.ITALIC, "Hover to see preset details"));
			for (String name : presets.keySet()) {
				Text.Builder builder = Text.builder();
				if (isPlayer) {
					builder.append(Text.of(TextColors.RED, TextActions.executeCallback(dummySrc ->
					{
						if (Presets.getPreset(name) != null) {
							Presets.deletePreset(name);
							src.sendMessage(Text.of(TextColors.GRAY, "Successfully deleted preset: ", TextColors.DARK_AQUA, name));
						}
					}), TextActions.showText(Text.of(TextColors.GRAY, TextStyles.ITALIC, "Click to delete this preset")), "[Delete]"));
				}
				builder.append(Text.of(TextColors.GOLD, "  ", TextColors.DARK_AQUA, TextActions.showText(presets.get(name).getDisplayText()), name));
				src.sendMessage(builder.build());
			}
			if (presets.isEmpty())
				src.sendMessage(Text.of(TextColors.RED, "None."));
			return CommandResult.success();
		}
		String arg = optPresetName.get();
		if (choice.equals("rename")) {
			String[] splits = arg.split(" ");
			if (splits.length < 2) {
				src.sendMessage(Text.of(TextColors.RED, "Not enough arguments: /tournaments presets rename <oldName> <newName>"));
				return CommandResult.empty();
			}
			String oldName = Presets.getMatchingKey(splits[0]);
			String newName = splits[1];
			if (oldName == null) {
				src.sendMessage(Text.of(TextColors.RED, "Unknown preset: ", splits[0]));
				return CommandResult.empty();
			}
			Presets.renamePreset(oldName, newName);
			src.sendMessage(Text.of(TextColors.DARK_GREEN, "Renamed ", TextColors.DARK_AQUA, oldName, TextColors.DARK_GREEN, " to ", TextColors.DARK_AQUA, newName));
			return CommandResult.success();
		} else {
			if (Tournament.instance() == null) {
				src.sendMessage(Text.of(TextColors.RED, "No tournament. Try /tournament create"));
				return CommandResult.empty();
			}
			if (choice.equals("save")) {
				Preset preset = new Preset(Tournament.instance().getRuleSet(),
						Tournament.instance().rewards,
						Zones.INSTANCE.getEngagedZones());
				Presets.setPreset(arg, preset);
				src.sendMessage(Text.of(TextColors.DARK_GREEN, "Successfully set preset ", Text.of(TextColors.DARK_AQUA, TextActions.showText(preset.getDisplayText()), arg)));
				return CommandResult.success();
			}

			String name = Presets.getMatchingKey(arg);
			if (name == null) {
				src.sendMessage(Text.of(TextColors.RED, "Unknown preset: ", TextColors.DARK_AQUA, arg));
				return CommandResult.empty();
			}

			if (choice.equals("load")) {
				Preset preset = Presets.getPreset(name);
				Tournament.instance().setRuleSet(preset.ruleSet);
				Tournament.instance().rewards.addAll(preset.rewards);
				if (!preset.zones.isEmpty())
					for (Zone zone : Zones.INSTANCE.getZones())
						if (!preset.zones.contains(zone))
							zone.engaged = false;
				src.sendMessage(Text.of(TextColors.DARK_GREEN, "Successfully loaded preset: ", TextColors.DARK_AQUA, name));
				return CommandResult.success();
			} else // "delete"
			{
				Presets.deletePreset(name);
				src.sendMessage(Text.of(TextColors.GRAY, "Successfully deleted preset: ", TextColors.DARK_AQUA, name));
				return CommandResult.success();
			}
		}
	}
}
