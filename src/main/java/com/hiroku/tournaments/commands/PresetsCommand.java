package com.hiroku.tournaments.commands;

import com.google.common.collect.ImmutableMap;
import com.happyzleaf.tournaments.Text;
import com.happyzleaf.tournaments.User;
import com.happyzleaf.tournaments.args.ChoiceSetArgument;
import com.hiroku.tournaments.Presets;
import com.hiroku.tournaments.Zones;
import com.hiroku.tournaments.api.Preset;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.obj.Zone;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.hiroku.tournaments.util.CommandUtils.getOptArgument;

/**
 * Command for checking, saving, loading, and deleting presets.
 *
 * @author Hiroku
 */
public class PresetsCommand implements Command<CommandSource> {
	Set<String> CHOICES = new HashSet<>(Arrays.asList("save", "load", "delete", "rename", ""));

	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("presets")
//				.description(Text.of("Adds, removes, or checks rule preset details"))
				.requires(source -> User.hasPermission(source, "tournaments.command.admin.presets"))
				.executes(this)
				.then(
						Commands.argument("choice", ChoiceSetArgument.choiceSet(CHOICES))
								.executes(this)
								.then(
										Commands.argument("preset", StringArgumentType.greedyString())
												.executes(this)
								)
				);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		String choice = getOptArgument(context, "choice", String.class).orElse("check");
		String presetName = getOptArgument(context, "preset", String.class).orElse(null);
		if (!choice.equals("check") && presetName == null) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Missing argument: preset-name"), true);
			return 0;
		}

		if (choice.equals("check")) {
			boolean isPlayer = context.getSource().getEntity() instanceof PlayerEntity;
			ImmutableMap<String, Preset> presets = Presets.getPresets();
			context.getSource().sendFeedback(Text.of(TextFormatting.GOLD, "------- Rule Presets --------"), true);
			context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, TextFormatting.ITALIC, "Hover to see preset details"), true);
			for (String name : presets.keySet()) {
				Text.Builder builder = Text.builder();
				// TODO textactions
//				if (isPlayer) {
//					builder.append(Text.of(TextFormatting.RED, TextActions.executeCallback(dummySrc ->
//					{
//						if (Presets.getPreset(name) != null) {
//							Presets.deletePreset(name);
//							context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Successfully deleted preset: ", TextFormatting.DARK_AQUA, name), true);
//						}
//					}), TextActions.showText(Text.of(TextFormatting.GRAY, TextFormatting.ITALIC, "Click to delete this preset")), "[Delete]"));
//				}
//				builder.append(Text.of(TextFormatting.GOLD, "  ", TextFormatting.DARK_AQUA, TextActions.showText(presets.get(name).getDisplayText()), name));
				context.getSource().sendFeedback(builder.build(), true);
			}
			if (presets.isEmpty())
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "None."), true);
			return 1;
		} else if (choice.equals("rename")) {
			String[] splits = presetName.split(" ");
			if (splits.length < 2) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Not enough arguments: /tournaments presets rename <oldName> <newName>"), true);
				return 0;
			}
			String oldName = Presets.getMatchingKey(splits[0]);
			String newName = splits[1];
			if (oldName == null) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Unknown preset: ", splits[0]), true);
				return 0;
			}
			Presets.renamePreset(oldName, newName);
			context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Renamed ", TextFormatting.DARK_AQUA, oldName, TextFormatting.DARK_GREEN, " to ", TextFormatting.DARK_AQUA, newName), true);
			return 1;
		} else {
			if (Tournament.instance() == null) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No tournament. Try /tournament create"), true);
				return 0;
			}

			if (choice.equals("save")) {
				Preset preset = new Preset(Tournament.instance().getRuleSet(),
						Tournament.instance().rewards,
						Zones.INSTANCE.getEngagedZones());
				Presets.setPreset(presetName, preset);
				// TODO textactions
//				context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Successfully set preset ", Text.of(TextFormatting.DARK_AQUA, TextActions.showText(preset.getDisplayText()), presetName)));
				return 1;
			}

			String name = Presets.getMatchingKey(presetName);
			if (name == null) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Unknown preset: ", TextFormatting.DARK_AQUA, presetName), true);
				return 0;
			}

			if (choice.equals("load")) {
				Preset preset = Presets.getPreset(name);
				Tournament.instance().setRuleSet(preset.ruleSet);
				Tournament.instance().rewards.addAll(preset.rewards);
				if (!preset.zones.isEmpty())
					for (Zone zone : Zones.INSTANCE.getZones())
						if (!preset.zones.contains(zone))
							zone.engaged = false;
				context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Successfully loaded preset: ", TextFormatting.DARK_AQUA, name), true);
				return 1;
			} else if (choice.equals("delete")) {
				Presets.deletePreset(name);
				context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Successfully deleted preset: ", TextFormatting.DARK_AQUA, name), true);
				return 1;
			}
		}

		return 0;
	}
}
