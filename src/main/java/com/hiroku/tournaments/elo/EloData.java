package com.hiroku.tournaments.elo;

import com.hiroku.tournaments.config.TournamentConfig;

public class EloData
{
	/** The Elo type for the simple single battle format. */
	public static final String SINGLE = "single";
	/** The Elo type for double battles where there is one player on the team. */
	public static final String DOUBLE1v1 = "double1v1";
	/** */
	public static final String DOUBLE2v2 = "double2v2";
	
	public int totalBattles;
	public int wins;
	public int losses;
	public int totalOpponentElo;
	
	public int getElo()
	{
		int elo = Math.round((totalOpponentElo + 1f * TournamentConfig.INSTANCE.eloFactor * (wins - losses))/totalBattles);
		
		return Math.max(0, elo);
	}
}
