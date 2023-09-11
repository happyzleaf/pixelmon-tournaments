package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.User;
import com.happyzleaf.tournaments.text.Text;
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
import net.minecraft.util.text.TextFormatting;

import java.util.Map;

/**
 * Command for checking, saving, loading, and deleting presets.
 *
 * @author Hiroku
 */
public class PresetsCommand implements Command<CommandSource> {
    public LiteralArgumentBuilder<CommandSource> create() {
        return Commands.literal("presets")
//				.description(Text.of("Adds, removes, or checks rule preset details"))
                .requires(source -> User.hasPermission(source, "tournaments.command.admin.presets"))
                .executes(this)
                .then(
                        Commands.literal("check")
                                .executes(this::check)
                )
                .then(
                        Commands.literal("rename")
                                .then(
                                        Commands.argument("preset", StringArgumentType.greedyString())
                                                .executes(this::rename)
                                )
                )
                .then(
                        Commands.literal("save")
                                .then(
                                        Commands.argument("preset", StringArgumentType.greedyString())
                                                .executes(this::save)
                                )
                )
                .then(
                        Commands.literal("load")
                                .then(
                                        Commands.argument("preset", StringArgumentType.greedyString())
                                                .executes(this::load)
                                )
                )
                .then(
                        Commands.literal("delete")
                                .then(
                                        Commands.argument("preset", StringArgumentType.greedyString())
                                                .executes(this::delete)
                                )
                );
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        return check(context);
    }

    private int check(CommandContext<CommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(Text.of(TextFormatting.GOLD, "------- Rule Presets --------"), false);
        Map<String, Preset> presets = Presets.getPresets();
        if (presets.isEmpty()) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "None."), false);
        }

        context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, TextFormatting.ITALIC, "Hover to see preset details"), false);

        for (String name : presets.keySet()) {
            context.getSource().sendFeedback(Text.of(
                    Text.of(TextFormatting.DARK_AQUA, name).onHover(presets.get(name).getDisplayText()),
                    TextFormatting.GOLD, "  ",
                    Text.of(TextFormatting.RED, "[Delete]")
                            .onHover(Text.of(TextFormatting.GRAY, TextFormatting.ITALIC, "Click to delete this preset"))
                            .onClick(context.getSource(), (src, ctx) -> {
                                if (Presets.getPreset(name) != null) {
                                    Presets.deletePreset(name);
                                    context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Successfully deleted preset: ", TextFormatting.DARK_AQUA, name), false);
                                }
                            })
            ), false);
        }

        return 1;
    }

    private int rename(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String presetName = context.getArgument("preset", String.class);
        String[] splits = presetName.split(" ");
        if (splits.length < 2) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Not enough arguments: /tournament presets rename <oldName> <newName>"), false);
            return 0;
        }

        String oldName = Presets.getMatchingKey(splits[0]);
        String newName = splits[1];
        if (oldName == null) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Unknown preset: ", splits[0]), false);
            return 0;
        }

        Presets.renamePreset(oldName, newName);
        context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Renamed ", TextFormatting.DARK_AQUA, oldName, TextFormatting.DARK_GREEN, " to ", TextFormatting.DARK_AQUA, newName), false);
        return 1;
    }

    private int save(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (Tournament.instance() == null) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No tournament. Try /tournament create"), false);
            return 0;
        }

        String presetName = context.getArgument("preset", String.class);
        Preset preset = new Preset(Tournament.instance().getRuleSet(), Tournament.instance().rewards, Zones.INSTANCE.getEngagedZones());
        Presets.setPreset(presetName, preset);

        context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Successfully set preset ", Text.of(TextFormatting.DARK_AQUA, presetName).onHover(preset.getDisplayText())), false);
        return 1;
    }

    private int load(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (Tournament.instance() == null) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No tournament. Try /tournament create"), false);
            return 0;
        }

        String presetName = context.getArgument("preset", String.class);
        String name = Presets.getMatchingKey(presetName);
        if (name == null) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Unknown preset: ", TextFormatting.DARK_AQUA, presetName), false);
            return 0;
        }

        Preset preset = Presets.getPreset(name);
        Tournament.instance().setRuleSet(preset.ruleSet);
        Tournament.instance().rewards.addAll(preset.rewards);
        if (!preset.zones.isEmpty()) {
            for (Zone zone : Zones.INSTANCE.getZones()) {
                if (!preset.zones.contains(zone)) {
                    zone.engaged = false;
                }
            }
        }

        context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Successfully loaded preset: ", TextFormatting.DARK_AQUA, name), false);
        return 1;
    }

    private int delete(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (Tournament.instance() == null) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No tournament. Try /tournament create"), false);
            return 0;
        }

        String presetName = context.getArgument("preset", String.class);
        String name = Presets.getMatchingKey(presetName);
        if (name == null) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Unknown preset: ", TextFormatting.DARK_AQUA, presetName), false);
            return 0;
        }

        Presets.deletePreset(name);

        context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Successfully deleted preset: ", TextFormatting.DARK_AQUA, name), false);
        return 1;
    }
}
