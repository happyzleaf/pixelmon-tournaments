package com.hiroku.tournaments.commands;

import com.hiroku.tournaments.Zones;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.obj.LocationWrapper;
import com.hiroku.tournaments.obj.Zone;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.ArrayList;

public class ZoneCommand implements CommandExecutor {
	public static CommandSpec getSpec() {
		return CommandSpec.builder()
				.arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("options"))))
				.permission("tournaments.command.admin.zones")
				.executor(new ZoneCommand())
				.description(Text.of("For setting and checking zone teleports for matches"))
				.build();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		String argStr = null;
		if (args.hasAny(Text.of("options")))
			argStr = args.<String>getOne(Text.of("options")).get();
		if (argStr == null) {
			showZonesSummary(src);
			return CommandResult.success();
		}
		String[] argArr = argStr.split(" ");
		if (argArr[0].equalsIgnoreCase("remove")) {
			if (argArr.length < 2) {
				src.sendMessage(Text.of(TextColors.RED, "Not enough arguments. Missing: Zone number/s"));
				src.sendMessage(Text.of(TextColors.RED, "/tournament zones [remove <zoneNumbers...>]"));
				return CommandResult.empty();
			}
			for (int i = 1; i < argArr.length; i++) {
				try {
					int zoneIndex = Integer.parseInt(argArr[i]);
					if (zoneIndex < 1 || zoneIndex > Zones.INSTANCE.getZones().size())
						throw new NumberFormatException();
					Zones.INSTANCE.removeZone(Zones.INSTANCE.getZones().get(zoneIndex - 1));
					Zones.INSTANCE.save();
					src.sendMessage(Text.of(TextColors.DARK_GREEN, "Removed zone ", argArr[i]));
				} catch (NumberFormatException nfe) {
					src.sendMessage(Text.of(TextColors.RED, "Invalid zone number: ", TextColors.DARK_AQUA, argArr[i]));
				}
			}
			return CommandResult.success();
		}
		if (argArr[0].equalsIgnoreCase("leavezone")) {
			if (argArr.length > 1) {
				if (src instanceof Player) {
					Zones.INSTANCE.leaveZone = new LocationWrapper(((Player) src).getLocation(), ((Player) src).getRotation());
					Zones.INSTANCE.save();
					src.sendMessage(Text.of(TextColors.DARK_GREEN, "Successfully set tournament leave zone"));
					return CommandResult.success();
				}

				src.sendMessage(Text.of(TextColors.RED, "You're not a player! Jeez."));
				return CommandResult.empty();
			}

			if (Zones.INSTANCE.leaveZone == null) {
				src.sendMessage(Text.of(TextColors.RED, "There is no leave zone set. Use /tournaments zones leavezone set"));
				return CommandResult.empty();
			}
			if (src instanceof Player) {
				Zones.INSTANCE.leaveZone.sendPlayer((Player) src);
				src.sendMessage(Text.of(TextColors.DARK_GREEN, "You've been warped to the tournament leave zone"));
				return CommandResult.success();
			}
			src.sendMessage(Text.of(TextColors.RED, "You aren't a player!"));
		}
		if (argArr[0].equalsIgnoreCase("leavezoneremove")) {
			Zones.INSTANCE.leaveZone = null;
			Zones.INSTANCE.save();
			src.sendMessage(Text.of(TextColors.DARK_GREEN, "Leavezone was removed!"));
			return CommandResult.success();
		}
		return CommandResult.empty();
	}

	public static void showZonesSummary(CommandSource src) {
		ArrayList<Text> contents = new ArrayList<Text>();
		for (int i = 0; i < Zones.INSTANCE.getZones().size(); i++) {
			final int fi = i + 1;
			Zone zone = Zones.INSTANCE.getZones().get(i);
			Text suffix = Text.of("");
			if (Tournament.instance() != null) {
				if (zone.engaged)
					suffix = Text.of(TextColors.GOLD,
							TextActions.executeCallback(dummySrc ->
							{
								if (zone.engaged) {
									zone.engaged = false;
									src.sendMessage(Text.of(TextColors.GRAY, "Disengaged zone ", TextColors.DARK_AQUA, fi));
								}
							}), " [", TextColors.RED, "Disengage", TextColors.GOLD, "]");
				else
					suffix = Text.of(TextColors.GOLD,
							TextActions.executeCallback(dummySrc ->
							{
								if (!zone.engaged) {
									zone.engaged = true;
									src.sendMessage(Text.of(TextColors.GRAY, "Engaged zone ", TextColors.DARK_AQUA, fi));
								}
							}), " [", TextColors.DARK_GREEN, "Engage", TextColors.GOLD, "]");
			}
			contents.add(Text.of(TextColors.GOLD, "[", TextColors.DARK_AQUA, "Zone ", (i + 1), TextColors.GOLD, "]: ", zone.getSummaryText(),
					" ", Text.of(
							TextActions.showText(Text.of(TextColors.GRAY, TextStyles.ITALIC, "Click to edit zone")),
							TextActions.executeCallback(dummySrc -> zone.editZone((Player) src)),
							TextColors.GOLD, "[", TextColors.YELLOW, "Edit", TextColors.GOLD, "]"),
					suffix)
			);
		}
		contents.add(Text.of(
				TextActions.executeCallback(dummySrc ->
				{
					Zone zone = new Zone();
					Zones.INSTANCE.addZone(zone);
					zone.editZone((Player) src);
					Zones.INSTANCE.save();
				}),
				TextActions.showText(Text.of(TextColors.GRAY, TextStyles.ITALIC, "Click here to create a new zone")),
				TextColors.GOLD, "[", TextColors.AQUA, "Add new zone", TextColors.GOLD, "]"
		));

		Sponge.getServiceManager().provide(PaginationService.class).get().builder()
				.contents(contents)
				.padding(Text.of(TextColors.GOLD, "-"))
				.title(Text.of(TextColors.GOLD, "Zones"))
				.linesPerPage(8)
				.sendTo(src);
	}
}
