package com.hiroku.tournaments.api.events.tournament;

import com.hiroku.tournaments.obj.Team;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Event fired when a {@link Team} leaves the tournament. Canceling this event will silently prevent them from leaving.
 *
 * @author Hiroku
 */
@Cancelable
public class LeaveTournamentEvent extends Event {
	/**
	 * The {@link Team} leaving the tournament.
	 */
	public final Team team;
	/**
	 * Whether the team was removed programmatically (true) or they left through the command (false).
	 */
	public final boolean forced;

	public LeaveTournamentEvent(Team team, boolean forcedOut) {
		this.team = team;
		this.forced = forcedOut;
	}
}
