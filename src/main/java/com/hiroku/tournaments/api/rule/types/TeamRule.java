package com.hiroku.tournaments.api.rule.types;

import org.spongepowered.api.text.Text;

import com.hiroku.tournaments.obj.Team;

/**
 * Abstract representation of all {@link RuleBase}s that apply specifically to {@link Team}s. These will be
 * checked once per {@link Team}, on an attempted match start.
 * 
 * @author Hiroku
 */
public abstract class TeamRule extends RuleBase
{
	public TeamRule(String arg) throws Exception
	{
		super(arg);
	}

	/**
	 * Checks whether the given {@link Team} passes the rule.
	 * 
	 * @param team - The {@link Team} being checked
	 * 
	 * @return - false if the {@link Team} is breaking this rule, otherwise true
	 */
	public abstract boolean passes(Team team);
	
	/**
	 * The {@link Text} displayed when the rule is broken.
	 * 
	 * @return - The {@link Text} that will appear when it has been found that the given {@link Team} broke this rule.
	 */
	public abstract Text getBrokenRuleText(Team team);
}
