package com.hiroku.tournaments.api.events.round;

import com.google.common.collect.ImmutableList;
import com.hiroku.tournaments.api.Match;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * Event fired when a round starts
 *
 * @author Hiroku
 */
public class RoundStartEvent extends Event {
	/**
	 * The coming round's {@link Match}es.
	 */
	public final ImmutableList<Match> round;

	public RoundStartEvent(List<Match> round) {
		this.round = ImmutableList.copyOf(round);
	}
}
