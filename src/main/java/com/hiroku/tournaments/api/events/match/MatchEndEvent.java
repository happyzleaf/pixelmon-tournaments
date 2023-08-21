package com.hiroku.tournaments.api.events.match;

import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.obj.Side;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Event fired when a match ends.
 *
 * @author Hiroku
 */
public class MatchEndEvent extends Event {
	/**
	 * The {@link Match} that just ended.
	 */
	public final Match match;
	/**
	 * The winning {@link Side}.
	 */
	public final Side winningSide;
	/**
	 * The losing {@link Side}.
	 */
	public final Side losingSide;

	public MatchEndEvent(Match match, Side winningSide, Side losingSide) {
		this.match = match;
		this.winningSide = winningSide;
		this.losingSide = losingSide;
	}
}
