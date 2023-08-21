package com.hiroku.tournaments.rules.general;

import com.hiroku.tournaments.api.rule.types.GeneralRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.elo.EloMatch;

import java.util.ArrayList;

public class EloType extends GeneralRule {
	/**
	 * The type of Elo this tournament will use.
	 */
	public String eloType;

	/**
	 * The list of recorded Elo-affecting matches that will process once the tournament finishes successfully.
	 */
	public ArrayList<EloMatch> eloMatches = new ArrayList<>();

	public EloType(String arg) throws Exception {
		super(arg);

		this.eloType = arg;
	}

	@Override
	public boolean duplicateAllowed(RuleBase other) {
		return false;
	}

	@Override
	public String getSerializationString() {
		return "elotype:" + eloType;
	}
}
