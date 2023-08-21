package com.hiroku.tournaments.api.rule.types;

import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.obj.Side;

/**
 * Abstract representation of a rule which decides the outcome of a draw and/or crashed battle.
 * 
 * @author Hiroku
 */
public abstract class DeciderRule extends RuleBase
{
	public DeciderRule(String arg) throws Exception
	{
		super(arg);
	}
	
	/**
	 * Decides the winner of a match, if possible. If no winner is returned for any deciding rules, the match 
	 * is restarted.
	 * 
	 * @param match - The match that needs judgement.
	 * 
	 * @return - The {@link Side} that is deemed the winner, or null if this rule could not decide which should win.
	 */
	public abstract Side decideWinner(Match match);
	
	/**
	 * @return - The weight of this rule. The higher this is, the more important its decision. If
	 * 			 two deciding rules have different non-null winners, the one chosen by the larger weight
	 * 			 is used. Lower weight decider rules are for when the higher weight decider rules could not decide.
	 * 			 For example: A high-weight decider might count party sizes, and find that they are the same. Then, 
	 * 			 perhaps the total HP may be the lower-weight decider rule which can finally pick a clear winner.
	 */
	public abstract int getWeight();
	
	/** Whether or not this deciding rule should be used for draws */
	public abstract boolean applyToDraws();
	/** Whether or not this deciding rule should be used for crashes/errors in battle */
	public abstract boolean applyToCrashes();
}
