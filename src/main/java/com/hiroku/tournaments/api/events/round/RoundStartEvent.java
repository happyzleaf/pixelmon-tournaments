package com.hiroku.tournaments.api.events.round;

import com.google.common.collect.ImmutableList;
import com.hiroku.tournaments.api.Match;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;

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

	public RoundStartEvent(ArrayList<Match> round) {
		this.round = ImmutableList.copyOf(round);
	}
}
