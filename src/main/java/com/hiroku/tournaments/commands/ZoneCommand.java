package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.User;
import com.happyzleaf.tournaments.text.Pagination;
import com.happyzleaf.tournaments.text.Text;
import com.hiroku.tournaments.Zones;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.obj.LocationWrapper;
import com.hiroku.tournaments.obj.Zone;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

import static com.hiroku.tournaments.util.CommandUtils.getOptArgument;

public class ZoneCommand implements Command<CommandSource> {
    public LiteralArgumentBuilder<CommandSource> create() {
        return Commands.literal("zones")
//				.description(Text.of("For setting and checking zone teleports for matches"))
                .requires(source -> User.hasPermission(source, "tournaments.command.admin.zones"))
                .executes(this)
                .then(
                        Commands.argument("options", StringArgumentType.greedyString())
                                .executes(this)
                );
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String argStr = getOptArgument(context, "options", String.class).orElse(null);
        if (argStr == null) {
            showZonesSummary(context.getSource());
            return 1;
        }

        String[] argArr = argStr.split(" ");
        if (argArr[0].equalsIgnoreCase("remove")) {
            if (argArr.length < 2) {
                context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Not enough arguments. Missing: Zone number/s"), true);
                context.getSource().sendFeedback(Text.of(TextFormatting.RED, "/tournament zones [remove <zoneNumbers...>]"), true);
                return 0;
            }
            for (int i = 1; i < argArr.length; i++) {
                try {
                    int zoneIndex = Integer.parseInt(argArr[i]);
                    if (zoneIndex < 1 || zoneIndex > Zones.INSTANCE.getZones().size())
                        throw new NumberFormatException();
                    Zones.INSTANCE.removeZone(Zones.INSTANCE.getZones().get(zoneIndex - 1));
                    Zones.INSTANCE.save();
                    context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Removed zone ", argArr[i]), true);
                } catch (NumberFormatException nfe) {
                    context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Invalid zone number: ", TextFormatting.DARK_AQUA, argArr[i]), true);
                }
            }
            return 1;
        }
        if (argArr[0].equalsIgnoreCase("leavezone")) {
            if (argArr.length > 1) {
                if (context.getSource().getEntity() instanceof PlayerEntity) {
                    Zones.INSTANCE.leaveZone = new LocationWrapper(context.getSource().asPlayer());
                    Zones.INSTANCE.save();
                    context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Successfully set tournament leave zone"), true);
                    return 1;
                }

                context.getSource().sendFeedback(Text.of(TextFormatting.RED, "You're not a player! Jeez."), true);
                return 0;
            }

            if (Zones.INSTANCE.leaveZone == null) {
                context.getSource().sendFeedback(Text.of(TextFormatting.RED, "There is no leave zone set. Use /tournament zones leavezone set"), true);
                return 0;
            }
            if (context.getSource().getEntity() instanceof PlayerEntity) {
                Zones.INSTANCE.leaveZone.sendPlayer(context.getSource().asPlayer());
                context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "You've been warped to the tournament leave zone"), true);
                return 1;
            }
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "You aren't a player!"), true);
        }
        if (argArr[0].equalsIgnoreCase("leavezoneremove")) {
            Zones.INSTANCE.leaveZone = null;
            Zones.INSTANCE.save();
            context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Leavezone was removed!"), true);
            return 1;
        }
        return 0;
    }

    public static void showZonesSummary(CommandSource source) {
        List<Text> contents = new ArrayList<>();
        for (int i = 0; i < Zones.INSTANCE.getZones().size(); i++) {
            final int fi = i + 1;
            Zone zone = Zones.INSTANCE.getZones().get(i);
            Text suffix = Text.of("");
            if (Tournament.instance() != null) {
                if (zone.engaged) {
                    suffix = Text.of(" ", Text.of(TextFormatting.GOLD, "[", TextFormatting.RED, "Disengage", TextFormatting.GOLD, "]")
                            .onClick(source, (src, ctx) -> {
                                if (zone.engaged) {
                                    zone.engaged = false;
                                    src.sendMessage(Text.of(TextFormatting.GRAY, "Disengaged zone ", TextFormatting.DARK_AQUA, fi), Util.DUMMY_UUID);
                                }
                            }));
                } else {
                    suffix = Text.of(" ", Text.of(TextFormatting.GOLD, "[", TextFormatting.DARK_GREEN, "Engage", TextFormatting.GOLD, "]")
                            .onClick(source, (src, ctx) -> {
                                if (!zone.engaged) {
                                    zone.engaged = true;
                                    src.sendMessage(Text.of(TextFormatting.GRAY, "Engaged zone ", TextFormatting.DARK_AQUA, fi), Util.DUMMY_UUID);
                                }
                            }));
                }
            }
            contents.add(Text.of(TextFormatting.GOLD, "[", TextFormatting.DARK_AQUA, "Zone ", (i + 1), TextFormatting.GOLD, "]: ", zone.getSummaryText(), " ",
                    Text.of(TextFormatting.GOLD, "[", TextFormatting.YELLOW, "Edit", TextFormatting.GOLD, "]")
                            .onHover(Text.of(TextFormatting.GRAY, TextFormatting.ITALIC, "Click to edit zone"))
                            .onClick(source, (src, ctx) -> zone.editZone(src)),
                    suffix));
        }
        contents.add(
                Text.of(TextFormatting.GOLD, "[", TextFormatting.AQUA, "Add new zone", TextFormatting.GOLD, "]")
                        .onHover(Text.of(TextFormatting.GRAY, TextFormatting.ITALIC, "Click here to create a new zone"))
                        .onClick(source, (src, ctx) -> {
                            Zone zone = new Zone();
                            Zones.INSTANCE.addZone(zone);
                            zone.editZone(src);
                            Zones.INSTANCE.save();
                        })
        );
        Pagination.builder()
                .title(Text.of(TextFormatting.GOLD, "Zones"))
                .padding(Text.of(TextFormatting.GOLD, "-"))
                .linesPerPage(8)
                .contents(contents)
                .sendTo(source);
    }
}
