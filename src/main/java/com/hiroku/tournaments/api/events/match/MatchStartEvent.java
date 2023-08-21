package com.hiroku.tournaments.api.events.match;

import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.obj.Zone;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Event fired when a {@link Match} starts.
 *
 * @author Hiroku
 */
public class MatchStartEvent extends Event {
	/**
	 * The {@link Match} being started.
	 */
	public final Match match;
	/**
	 * The {@link Zone} in which it's being started.
	 */
	public final Zone zone;

	public MatchStartEvent(Match match, Zone zone) {
		this.match = match;
		this.zone = zone;
	}
}
