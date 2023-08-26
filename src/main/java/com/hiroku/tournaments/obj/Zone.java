package com.hiroku.tournaments.obj;

import com.happyzleaf.tournaments.Text;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.Zones;
import com.hiroku.tournaments.api.Match;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class Zone {
	public int uid = -1;
	public boolean engaged = true;

	private final LocationWrapper[][] spots = new LocationWrapper[2][3];

	public void setZoneSpot(int side, int slot, LocationWrapper location) {
		spots[side][slot] = location;
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
				getZoneSpot(side, 1).sendPlayer(sideUsers.get(0).getPlayer());
			else if (sideUsers.size() == 2) {
				// This could be done in a second loop with slot = i * 2 but it's not as clear as this
				// - Roku
				if (sideUsers.get(0).isOnline() && getZoneSpot(side, 0) != null)
					getZoneSpot(side, 0).sendPlayer(sideUsers.get(0).getPlayer());
				if (sideUsers.get(1).isOnline() && getZoneSpot(side, 2) != null)
					getZoneSpot(side, 2).sendPlayer(sideUsers.get(1).getPlayer());
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

			builder.append(Text.of(TextFormatting.GOLD, "["));
			for (int j = 0; j < 3; j++) {
				final int slot = j + 1;
				if (sideSet[j] == null)
					builder.append(Text.of(TextFormatting.RED, slot));
				else
					// TODO: when textaction onclick works
//					builder.append(Text.of(TextActions.executeCallback(src -> {
//						if (src.getEntity() instanceof PlayerEntity) {
//							sideSet[slot - 1].sendPlayer((PlayerEntity) src.getEntity());
//							src.sendMessage(Text.of(TextFormatting.DARK_GREEN, "Sent you to side ", TextFormatting.DARK_AQUA, side,
//									TextFormatting.DARK_GREEN, " position ", TextFormatting.DARK_AQUA, slot), Util.DUMMY_UUID);
//						}
//					}), TextFormatting.DARK_GREEN, slot));
				if (j < 2)
					builder.append(Text.of(TextFormatting.GRAY, ", "));
			}
			builder.append(Text.of(TextFormatting.GOLD, "] "));
		}

		return builder.build();
	}

	public void editZone(PlayerEntity player) {
		player.sendMessage(Text.of(TextFormatting.GOLD, "--------------- ", TextFormatting.DARK_AQUA, "Modifying Zone", TextFormatting.GOLD, " --------------"), Util.DUMMY_UUID);
		player.sendMessage(Text.of(TextFormatting.GRAY, "Things to know: Position 2 is where a player will be put"), Util.DUMMY_UUID);
		player.sendMessage(Text.of(TextFormatting.GRAY, "during a single battle tournament. Positions 1 and 3 are"), Util.DUMMY_UUID);
		player.sendMessage(Text.of(TextFormatting.GRAY, "where the two players will be put during a double battle"), Util.DUMMY_UUID);
		for (int i = 0; i < 2; i++) {
			final int side = i + 1;
			Text.Builder builder = Text.builder();
			for (int j = 0; j < 3; j++) {
				final int slot = j + 1;
				if (this.getZoneSpot(side - 1, slot - 1) != null)
					builder.append(Text.of(
							// TODO: textactions onclick
//							TextActions.executeCallback(dummySrc -> getZoneSpot(side - 1, slot - 1).sendPlayer(player)),
							// TODO: text.onhover?
//							TextActions.showText(Text.of(TextFormatting.GRAY, "Click to warp to this zone spot")),
							TextFormatting.GREEN, slot, slot != 3 ? Text.of(TextFormatting.GRAY, ", ") : ""));
				else
					builder.append(Text.of(
							// TODO: textaction onclick
//							TextActions.executeCallback(dummySrc -> {
//								this.setZoneSpot(side - 1, slot - 1, new LocationWrapper(player));
//								player.sendMessage(Text.of(TextFormatting.DARK_GREEN, "Successfully set location and rotation for side ",
//										TextFormatting.DARK_AQUA, side, TextFormatting.DARK_GREEN, " position ", TextFormatting.DARK_AQUA, slot));
//								Zones.INSTANCE.save();
//							}),
							// TODO: text.onhover?
//							TextActions.showText(Text.of(TextFormatting.GRAY, "Click to set to current location and rotation")),
							TextFormatting.RED, slot, slot != 3 ? Text.of(TextFormatting.GRAY, ", ") : ""));
			}
			player.sendMessage(Text.of(TextFormatting.GOLD, "Side ", TextFormatting.DARK_AQUA, side, TextFormatting.GOLD, ": [",
					builder.build(), "]", Text.of("   ",
							// TODO: textaction onclick
//							TextActions.executeCallback(dummySrc -> {
//								this.spots[side - 1] = new LocationWrapper[3];
//								Zones.INSTANCE.save();
//								player.sendMessage(Text.of(TextFormatting.DARK_GREEN, "Cleared side ", TextFormatting.DARK_AQUA, side), Util.DUMMY_UUID);
//							}),
							// TODO: text.onhover?
//							TextActions.showText(Text.of(TextFormatting.GRAY, "Click to clear this side's zones")),
							TextFormatting.RED, "[Clear]")), Util.DUMMY_UUID);
			player.sendMessage(Text.of(""), Util.DUMMY_UUID);
		}
		player.sendMessage(
				Text.of("                                             ", TextFormatting.RED,
						// TODO: textaction onclick
//						TextActions.executeCallback(dummySrc -> {
//							Zones.INSTANCE.removeZone(this);
//							player.sendMessage(Text.of(TextFormatting.DARK_GREEN, "Successfully removed zone."));
//							Zones.INSTANCE.save();
//						}),
						"[Delete Zone]"), Util.DUMMY_UUID);
	}
}
