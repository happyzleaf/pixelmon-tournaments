package com.hiroku.tournaments.api.events.tournament;

import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.api.entity.living.player.User;

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
