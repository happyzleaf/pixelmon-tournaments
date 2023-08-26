package com.hiroku.tournaments.elo;

import com.hiroku.tournaments.config.TournamentConfig;

public class EloData {
	public int totalBattles;
	public int wins;
	public int losses;
	public int totalOpponentElo;

	public int getElo() {
		int elo = Math.round((totalOpponentElo + 1f * TournamentConfig.INSTANCE.eloFactor * (wins - losses)) / totalBattles);

		return Math.max(0, elo);
	}
}
