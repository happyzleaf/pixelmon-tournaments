package com.hiroku.tournaments.api.rule.types;

import com.happyzleaf.tournaments.Text;
import com.hiroku.tournaments.obj.Side;

/**
 * Abstract representation of all {@link RuleBase}s that apply specifically to {@link Side}s. These will be
 * checked once per {@link Side}, on an attempted match start.
 *
 * @author Hiroku
 */
public abstract class SideRule extends RuleBase {
	public SideRule(String arg) throws Exception {
		super(arg);
	}

	/**
	 * Checks whether the given {@link Side} passes the rule.
	 *
	 * @param side - The {@link Side} being checked
	 * @return - false if the {@link Side} is breaking this rule, otherwise true
	 */
	public abstract boolean passes(Side side);

	/**
	 * The {@link Text} displayed when the rule is broken.
	 *
	 * @return - The {@link Text} that will appear when it has been found that the given {@link Side} broke this rule.
	 */
	public abstract Text getBrokenRuleText(Side side);
}
