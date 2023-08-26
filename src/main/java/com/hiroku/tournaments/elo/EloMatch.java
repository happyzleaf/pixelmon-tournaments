package com.hiroku.tournaments.elo;

import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.rules.general.EloType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EloMatch {
	public final List<UUID> winners = new ArrayList<>();
	public final List<UUID> losers = new ArrayList<>();
	public final boolean draw;

	public EloMatch(List<UUID> winners, List<UUID> losers, boolean draw) {
		this.winners.addAll(winners);
		this.losers.addAll(losers);
		this.draw = draw;
	}

	public void process() {
		EloStorage.registerBattle(winners, losers, Tournament.instance().getRuleSet().getRule(EloType.class).type, draw);
	}
}
