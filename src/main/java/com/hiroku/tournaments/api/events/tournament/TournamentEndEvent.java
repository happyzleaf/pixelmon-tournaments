package com.hiroku.tournaments.api.events.tournament;

import com.happyzleaf.tournaments.User;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * Event fired when a tournament ends.
 *
 * @author Hiroku
 */
public class TournamentEndEvent extends Event {
	/**
	 * The winners of the tournament. Could be empty.
	 */
	public final List<User> winners;

	public TournamentEndEvent(List<User> winners) {
		this.winners = winners;
	}
}
