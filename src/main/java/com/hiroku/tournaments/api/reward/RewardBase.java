package com.hiroku.tournaments.api.reward;

import org.spongepowered.api.entity.living.player.Player;

import com.hiroku.tournaments.api.Mode;

/**
 * Abstract representation of a reward for winning a match in a tournament
 * 
 * @author Hiroku
 */
public abstract class RewardBase extends Mode
{
	/**
	 * Constructs with an argument. This is the value to the right of the colon in the reward add command.
	 * This argument might be a blank string.
	 * 
	 * @param arg - The argument (potentially a blank string) for the reward. eg. "cash:500", "500" is the argument.
	 */
	public RewardBase(String arg) throws Exception
	{
		;
	}
	
	/**e
	 * Describes how to give a {@link Player} this reward.
	 * @param player - The {@link Player} to give this reward to.
	 */
	public abstract void give(Player player);
	
	/** Gets the string form of this rule for serializing. This is effectively how the mode would be added. eg. 'levelmax:50'. */
	public abstract String getSerializationString();
}
