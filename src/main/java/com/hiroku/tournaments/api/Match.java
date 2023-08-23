package com.hiroku.tournaments.api;

import com.happyzleaf.tournaments.Text;
import com.hiroku.tournaments.obj.MatchStartResult;
import com.hiroku.tournaments.obj.Side;
import com.hiroku.tournaments.obj.Team;
import net.minecraft.util.text.TextFormatting;

import java.util.UUID;

/**
 * Representation of a match during a tournament.
 *
 * @author Hiroku
 */
public abstract class Match {
	/**
	 * The sides of the battle
	 */
	public final Side[] sides;
	/**
	 * Whether the battle for this match is actively running.
	 */
	public boolean matchActive;

	public Match(Side side1, Side side2) {
		this.sides = new Side[]{side1, side2};
	}

	public abstract void start(Tournament tournament, boolean rematch);

	public abstract void forceEnd();

	/**
	 * Attempts to start a match and returns the result.
	 *
	 * @return - A {@link MatchStartResult} representing the result of the battle start. This will be a subclass of MatchStartResult.
	 */
	public void start(Tournament tournament) {
		start(tournament, false);
	}

	/**
	 * Sends a message to everyone in the match.
	 */
	public void sendMessage(Text text) {
		for (Side side : sides)
			side.sendMessage(text);
	}

	/**
	 * Gets the {@link Team} for the given UUID, or null if the UUID doesn't match anyone in the match.
	 */
	public Team getTeam(UUID uuid) {
		for (Side side : sides)
			if (side.hasPlayer(uuid))
				return side.getTeam(uuid);
		return null;
	}

	/**
	 * Gets the {@link Side} for the given UUID, or null if the UUID doesn't match anyone in the match.
	 */
	public Side getSide(UUID uuid) {
		for (Side side : sides)
			if (side.hasPlayer(uuid))
				return side;
		return null;
	}

	/**
	 * Gets the {@link Side} for the given {@link Team}, or null if the UUID doesn't match anyone in the match.
	 */
	public Side getSide(Team team) {
		for (Side side : sides)
			for (Team otherTeam : side.teams)
				if (otherTeam != null && otherTeam == team)
					return side;
		return null;
	}

	/**
	 * Gets the side opposite the side for the given UUID. For example, if the given UUID is a member of the first side, the second side
	 * is returned.
	 *
	 * @param uuid - The UUID to check for.
	 * @return - The {@link Side} that the given UUID does NOT belong to.
	 */
	public Side getOtherSide(UUID uuid) {
		return sides[0].hasPlayer(uuid) ? sides[1] : sides[0];
	}

	/**
	 * Gets the side facing the given side.
	 */
	public Side getOtherSide(Side side) {
		return sides[0] == side ? sides[1] : sides[0];
	}

	/**
	 * Gets the number of players in the entire match. If the parameter is false, only online players will be counted.
	 *
	 * @param countOffline - Whether offline players should be counted.
	 * @return - The number of players in the match (excluding offline players if the parameter is false)
	 */
	public int getNumPlayers(boolean countOffline) {
		int count = 0;
		for (Side side : sides)
			count += side.getNumPlayers(countOffline);
		return count;
	}

	/**
	 * Gets the display {@link Text} for this {@link Match}. This is normally of the format "side1 vs side2".
	 *
	 * @return - The {@link Text} that will display in summary of this match.
	 */
	public Text getDisplayText() {
		return Text.of(sides[0].getDisplayText(), TextFormatting.RED, " vs ", sides[1].getDisplayText());
	}

	public Text getStateText() {
		return Text.of((matchActive ? TextFormatting.RED : TextFormatting.GRAY), "*");
	}
}
