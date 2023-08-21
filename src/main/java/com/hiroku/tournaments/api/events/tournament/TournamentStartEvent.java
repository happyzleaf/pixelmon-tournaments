package com.hiroku.tournaments.api.events.tournament;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Event fired when a tournament starts. Canceling this event will silently prevent it from starting.
 * 
 * @author Hiroku
 */
@Cancelable
public class TournamentStartEvent extends Event
{
	;
}
