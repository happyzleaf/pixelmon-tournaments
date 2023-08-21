package com.hiroku.tournaments.api.reward;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * Registrar for the different types of rewards for tournaments. 
 * 
 * @author Hiroku
 */
public final class RewardTypeRegistrar
{
	/** The mapping from key list to class representation of reward type. */
	public static HashMap<List<String>, Class<? extends RewardBase>> rewardTypes = new HashMap<List<String>, Class<? extends RewardBase>>();
	
	/**
	 * Registers a type of reward. If any keys are already in use, the existing reward type will be overwritten.
	 * 
	 * @param keys - A list of the text representations of the reward type. e.g. "items", "money"
	 * @param rewardType - The class (extending {@link RewardBase}) that is this tournament reward type.
	 */
	public static void registerRewardType(List<String> keys, Class<? extends RewardBase> rewardType)
	{
		List<String> duplicate = null;
		
		outerloop:
		for (String key : keys)
		{
			for (List<String> existingKeys : rewardTypes.keySet())
			{
				if (existingKeys.contains(key.toLowerCase()))
				{
					duplicate = existingKeys;
					break outerloop;
				}
			}
		}
		
		if (duplicate != null)
			rewardTypes.remove(duplicate);
		
		ArrayList<String> sanitizedKeys = new ArrayList<String>();
		for (String key : keys)
			sanitizedKeys.add(key.toLowerCase());
		
		rewardTypes.put(sanitizedKeys, rewardType);				
	}
	
	/**
	 * Gets the class of {@link RewardBase} associated with the given key, if a reward for that key exists.
	 * 
	 * @param key - A string representation of the reward. eg. "money", "item", "pokemon".
	 * 
	 * @return - The class extending {@link RewardBase} for the given key, or null if none exists.
	 */
	public static Class<? extends RewardBase> getRewardTypeForKey(String key)
	{
		for (Entry<List<String>, Class<? extends RewardBase>> entry : rewardTypes.entrySet())
			if (entry.getKey().contains(key.toLowerCase()))
				return entry.getValue();
		return null;
	}
	
	/**
	 * Parses a {@link RewardBase} out of a key and nullable argument. Throws an exception if there was any fault.
	 * 
	 * @param key - The String representation of the reward type.
	 * @param arg - The argument that the rule needs for construction, or null if the reward type doesn't require it.
	 * 
	 * @return - The {@link RewardBase} constructed.
	 * 
	 * @throws Exception when there was any kind of problem with the parsing. The cause message will have information about the fault.
	 */
	public static RewardBase parse(String key, String arg) throws Exception
	{
		if (arg == null)
			arg = "";
		
		Class<? extends RewardBase> rewardType = getRewardTypeForKey(key);
		
		if (rewardType == null)
			throw new Exception("No reward type for key: " + key);
		
		try
		{
			return rewardType.getDeclaredConstructor(String.class).newInstance(arg);
		}
		catch (Exception e)
		{
			throw new Exception(e.getCause().getMessage());
		}
	}
}
