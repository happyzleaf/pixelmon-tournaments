package com.hiroku.tournaments.api.events.tournament;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * Event fired when a tournament starts. Canceling this event will silently prevent it from starting.
 *
 * @author Hiroku
 */
@Cancelable
public class TournamentStartEvent extends Event {
}
