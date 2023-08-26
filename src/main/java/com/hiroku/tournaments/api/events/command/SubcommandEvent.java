package com.hiroku.tournaments.api.events.command;

import com.hiroku.tournaments.api.command.TournamentCommandWrapper;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Event fired before command registration, giving the opportunity for extensions to
 * register their own subcommands beneath the primary tournament aliases.
 *
 * @author Hiroku
 */
public class SubcommandEvent extends Event {
	/**
	 * The subcommands to register under /tournament
	 */
	public List<TournamentCommandWrapper> subcommands = new ArrayList<>();
}
