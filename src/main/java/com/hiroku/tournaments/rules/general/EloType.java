package com.hiroku.tournaments.rules.general;

import com.hiroku.tournaments.api.rule.types.GeneralRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.elo.EloMatch;
import com.hiroku.tournaments.elo.EloTypes;

import java.util.ArrayList;
import java.util.List;

public class EloType extends GeneralRule {
	/**
	 * The type of Elo this tournament will use.
	 */
	public EloTypes type;

	/**
	 * The list of recorded Elo-affecting matches that will process once the tournament finishes successfully.
	 */
	public List<EloMatch> eloMatches = new ArrayList<>();

	public EloType(String arg) throws Exception {
		super(arg);

		this.type = EloTypes.valueOf(arg.toUpperCase());
	}

	@Override
	public boolean duplicateAllowed(RuleBase other) {
		return false;
	}

	@Override
	public String getSerializationString() {
		return "elotype:" + type.toString();
	}
}
