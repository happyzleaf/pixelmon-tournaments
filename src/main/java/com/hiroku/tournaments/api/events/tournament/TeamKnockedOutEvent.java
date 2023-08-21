package com.hiroku.tournaments.api.events.tournament;

import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.obj.Team;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Event fired when a team is about to be knocked out of the tournament. If this event is cancelled, they will not be knocked out
 * and will be in the queue for matchmaking in the next round.
 * 
 * @author Hiroku
 */
@Cancelable
public class TeamKnockedOutEvent extends Event
{
	/** The {@link Team} that lost */
	public final Team team;
	/** The {@link Match} they lost in */
	public final Match match;
	
	public TeamKnockedOutEvent(Team team, Match match)
	{
		this.team = team;
		this.match = match;
	}
}
