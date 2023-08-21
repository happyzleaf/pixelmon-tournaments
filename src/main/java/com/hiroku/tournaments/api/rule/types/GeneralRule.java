package com.hiroku.tournaments.api.rule.types;

/**
 * Abstract representation of all {@link RuleBase}s that are used only in structural or general ways, not
 * on teams, players, or for decisions.
 *
 * @author Hiroku
 */
public abstract class GeneralRule extends RuleBase {
	public GeneralRule(String arg) throws Exception {
		super(arg);
	}
}
