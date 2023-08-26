package com.hiroku.tournaments.obj;

import com.flowpowered.math.vector.Vector3d;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.Zones;
import com.hiroku.tournaments.api.Match;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;

public class Zone {
	public int uid = -1;
	public boolean engaged = true;

	private final LocationWrapper[][] spots = new LocationWrapper[2][3];

	public void setZoneSpot(int side, int slot, Location<World> location, Vector3d rotation) {
		spots[side][slot] = new LocationWrapper(location, rotation);
	}

	public LocationWrapper getZoneSpot(int side, int slot) {
		if (spots[side][slot] != null)
			return spots[side][slot];
		return null;
	}

	public void sendPlayersToZone(Match match) {
		for (int side = 0; side < 2; side++) {
			List<User> sideUsers = new ArrayList<>();

			for (Team team : match.sides[side].teams)
				sideUsers.addAll(team.users);

			if (sideUsers.size() == 1 && sideUsers.get(0).isOnline() && getZoneSpot(side, 1) != null)
				getZoneSpot(side, 1).sendPlayer(sideUsers.get(0).getPlayer().get());
			else if (sideUsers.size() == 2) {
				// This could be done in a second loop with slot = i * 2 but it's not as clear as this
				// - Roku
				if (sideUsers.get(0).isOnline() && getZoneSpot(side, 0) != null)
					getZoneSpot(side, 0).sendPlayer(sideUsers.get(0).getPlayer().get());
				if (sideUsers.get(1).isOnline() && getZoneSpot(side, 2) != null)
					getZoneSpot(side, 2).sendPlayer(sideUsers.get(1).getPlayer().get());
			}
		}
	}

	public Text getSummaryText() {
		Text.Builder builder = Text.builder();
		for (int i = 0; i < 2; i++) {
			// I need a final display form of side and slot for the lambdas
			// - Roku
			final int side = i + 1;
			LocationWrapper[] sideSet = this.spots[i];

			builder.append(Text.of(TextColors.GOLD, "["));
			for (int j = 0; j < 3; j++) {
				final int slot = j + 1;
				if (sideSet[j] == null)
					builder.append(Text.of(TextColors.RED, slot));
				else
					builder.append(Text.of(TextActions.executeCallback(src -> {
						if (src instanceof Player) {
							sideSet[slot - 1].sendPlayer((Player) src);
							src.sendMessage(Text.of(TextColors.DARK_GREEN, "Sent you to side ", TextColors.DARK_AQUA, side,
									TextColors.DARK_GREEN, " position ", TextColors.DARK_AQUA, slot));
						}
					}), TextColors.DARK_GREEN, slot));
				if (j < 2)
					builder.append(Text.of(TextColors.GRAY, ", "));
			}
			builder.append(Text.of(TextColors.GOLD, "] "));
		}

		return builder.build();
	}

	public void editZone(Player player) {
		player.sendMessage(Text.of(TextColors.GOLD, "--------------- ", TextColors.DARK_AQUA, "Modifying Zone", TextColors.GOLD, " --------------"));
		player.sendMessage(Text.of(TextColors.GRAY, "Things to know: Position 2 is where a player will be put"));
		player.sendMessage(Text.of(TextColors.GRAY, "during a single battle tournament. Positions 1 and 3 are"));
		player.sendMessage(Text.of(TextColors.GRAY, "where the two players will be put during a double battle"));
		for (int i = 0; i < 2; i++) {
			final int side = i + 1;
			Text.Builder builder = Text.builder();
			for (int j = 0; j < 3; j++) {
				final int slot = j + 1;
				if (this.getZoneSpot(side - 1, slot - 1) != null)
					builder.append(Text.of(
							TextActions.executeCallback(dummySrc -> getZoneSpot(side - 1, slot - 1).sendPlayer(player)),
							TextActions.showText(Text.of(TextColors.GRAY, "Click to warp to this zone spot")),
							TextColors.GREEN, slot, slot != 3 ? Text.of(TextColors.GRAY, ", ") : ""));
				else
					builder.append(Text.of(
							TextActions.executeCallback(dummySrc ->
							{
								this.setZoneSpot(side - 1, slot - 1, player.getLocation(), player.getRotation());
								player.sendMessage(Text.of(TextColors.DARK_GREEN, "Successfully set location and rotation for side ",
										TextColors.DARK_AQUA, side, TextColors.DARK_GREEN, " position ", TextColors.DARK_AQUA, slot));
								Zones.INSTANCE.save();
							}),
							TextActions.showText(Text.of(TextColors.GRAY, "Click to set to current location and rotation")),
							TextColors.RED, slot, slot != 3 ? Text.of(TextColors.GRAY, ", ") : ""));
			}
			player.sendMessage(Text.of(TextColors.GOLD, "Side ", TextColors.DARK_AQUA, side, TextColors.GOLD, ": [",
					builder.build(), "]", Text.of("   ", TextActions.executeCallback(dummySrc ->
							{
								this.spots[side - 1] = new LocationWrapper[3];
								Zones.INSTANCE.save();
								player.sendMessage(Text.of(TextColors.DARK_GREEN, "Cleared side ", TextColors.DARK_AQUA, side));
							}),
							TextActions.showText(Text.of(TextColors.GRAY, "Click to clear this side's zones")),
							TextColors.RED, "[Clear]")));
			player.sendMessage(Text.of(""));
		}
		player.sendMessage(
				Text.of("                                             ", TextColors.RED,
						TextActions.executeCallback(dummySrc ->
						{
							Zones.INSTANCE.removeZone(this);
							player.sendMessage(Text.of(TextColors.DARK_GREEN, "Successfully removed zone."));
							Zones.INSTANCE.save();
						}), "[Delete Zone]"));
	}
}
