package com.hiroku.tournaments.elo;

import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.rules.general.EloType;

import java.util.ArrayList;
import java.util.UUID;

public class EloMatch {
	public ArrayList<UUID> winners = new ArrayList<>();
	public ArrayList<UUID> losers = new ArrayList<>();
	public boolean draw = false;

	public EloMatch(ArrayList<UUID> winners, ArrayList<UUID> losers, boolean draw) {
		this.winners = winners;
		this.losers = losers;
		this.draw = draw;
	}

	public void process() {
		EloStorage.registerBattle(winners, losers, Tournament.instance().getRuleSet().getRule(EloType.class).eloType, draw);
	}
}
