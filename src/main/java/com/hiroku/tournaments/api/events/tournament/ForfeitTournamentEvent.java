package com.hiroku.tournaments.api.events.tournament;

import com.hiroku.tournaments.obj.Team;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Event fired when a {@link Team} forfeits (or is forced to forfeit). Canceling this event will silently prevent
 * them from forfeiting.
 *
 * @author Hiroku
 */
@Cancelable
public class ForfeitTournamentEvent extends Event {
	/**
	 * The {@link Team} forfeiting.
	 */
	public final Team team;
	/**
	 * Whether they were forced to forfeit programmatically or by someone else using the command (true), or
	 * of their own volition (false).
	 */
	public final boolean forced;

	public ForfeitTournamentEvent(Team team, boolean forced) {
		this.team = team;
		this.forced = forced;
	}
}
