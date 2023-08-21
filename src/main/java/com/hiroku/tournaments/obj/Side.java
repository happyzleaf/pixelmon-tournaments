package com.hiroku.tournaments.obj;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Simple representation of the list of teams that depicts a side of a match
 *
 * @author Hiroku
 */
public class Side {
	/**
	 * The array of teams (normally just 1) that are on this side of a match
	 */
	public final Team[] teams;

	public Side(Team... teams) {
		this.teams = teams;
	}

	/**
	 * Shortcut function for creating a side based off a series of teams.
	 */
	public static Side of(Team... teams) {
		return new Side(teams);
	}

	/**
	 * Gets whether the given UUID belongs to this side.
	 *
	 * @param uuid - The UUID of the player to look for.
	 * @return - true if the player is on this side, otherwise false.
	 */
	public boolean hasPlayer(UUID uuid) {
		return getTeam(uuid) != null;
	}

	/**
	 * Gets the specific team that the given UUID is a member of. This will return null if they are not on this side.
	 *
	 * @param uuid - The UUID of the player whose team must be found.
	 * @return - The {@link Team} the given player belongs to on this side, or null if they are not in any team on this side.
	 */
	public Team getTeam(UUID uuid) {
		for (Team team : teams)
			if (team.hasPlayer(uuid))
				return team;
		return null;
	}

	/**
	 * Gets the number of players on this side, excluding offline players if the parameter is false.
	 *
	 * @param countOffline - Whether offline players should be counted.
	 * @return - The number of players on this side, excluding offline players if the parameter is false.
	 */
	public int getNumPlayers(boolean countOffline) {
		int count = 0;
		for (Team team : teams)
			count += team.getNumPlayers(countOffline);
		return count;
	}

	/**
	 * Gets the UUIDs of every member of all teams on this side.
	 */
	public ArrayList<UUID> getUUIDs() {
		ArrayList<UUID> uuids = new ArrayList<>();
		for (Team team : teams)
			uuids.addAll(team.getUUIDs());
		return uuids;
	}

	/**
	 * Gets the display text for this side.
	 *
	 * @return - The {@link Text} to display to represent this side.
	 */
	public Text getDisplayText() {
		Text.Builder builder = Text.builder();
		builder.append(teams[0].getDisplayText());
		if (teams.length == 2)
			builder.append(Text.of(TextColors.GOLD, " & ", teams[1].getDisplayText()));
		return builder.build();
	}

	/**
	 * Sends a message to every team in the side.
	 */
	public void sendMessage(Text text) {
		for (Team team : teams)
			team.sendMessage(text);
	}
}
