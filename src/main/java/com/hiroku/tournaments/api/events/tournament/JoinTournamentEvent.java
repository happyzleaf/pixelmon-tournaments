package com.hiroku.tournaments.api.events.tournament;

import com.hiroku.tournaments.obj.Team;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Fired when a {@link Team} joins the tournament. Canceling this event silently prevents them from joining.
 * 
 * @author Hiroku
 */
@Cancelable
public class JoinTournamentEvent extends Event
{
	/** The {@link Team} joining the tournament. */
	public final Team team;
	/** Whether the team was added in programmatically (true) or they joined through the command (false). */
	public final boolean forced;
	
	public JoinTournamentEvent(Team team, boolean forcedIn)
	{
		this.team = team;
		this.forced = forcedIn;
	}
}