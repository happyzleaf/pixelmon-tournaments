package com.hiroku.tournaments.api.command;

import java.util.List;

import org.spongepowered.api.command.spec.CommandSpec;

/**
 * Simple wrapper for tournament subcommand creation from the {@link com.hiroku.tournaments.api.events.command.SubcommandEvent}.
 * 
 * @author Hiroku
 */
public class TournamentCommandWrapper
{
	/** The list of aliases for this subcommand. */
	public final List<String> aliases;
	/** The {@link CommandSpec} of the subcommand. */
	public final CommandSpec spec;
	
	public TournamentCommandWrapper(List<String> aliases, CommandSpec spec)
	{
		this.aliases = aliases;
		this.spec = spec;
	}
}
