package com.hiroku.tournaments.api.events.command;

import java.util.ArrayList;

import com.hiroku.tournaments.api.command.TournamentCommandWrapper;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Event fired before command registration, giving the opportunity for extensions to 
 * register their own subcommands beneath the primary tournament aliases.
 * 
 * @author Hiroku
 */
public class SubcommandEvent extends Event
{
	/** The subcommands to register under /tournament */
	public ArrayList<TournamentCommandWrapper> subcommands = new ArrayList<TournamentCommandWrapper>();
}
