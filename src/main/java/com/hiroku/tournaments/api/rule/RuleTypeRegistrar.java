package com.hiroku.tournaments.api.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hiroku.tournaments.api.rule.types.RuleBase;

/**
 * Registrar for bindings from rule key to rule implementation. Plugins wishing to add their own rules need only register
 * the relevant RuleBase here
 * 
 * @author Hiroku
 */
public final class RuleTypeRegistrar
{
	private static HashMap<List<String>, Class<? extends RuleBase>> ruleTypes = new HashMap<List<String>, Class<? extends RuleBase>>();
	
	/**
	 * Registers a particular rule for the given set of keys. If there exists a rule that has a key that also exists
	 * in the given keys, the existing rule is removed. Through this functionality you may bypass or overwrite the default rules.
	 * 
	 * @param keys - A case insensitive list of all string representations of the keys that represent the 
	 * @param rule - The class of rule to register.
	 */
	public static void registerRuleType(List<String> keys, Class<? extends RuleBase> rule)
	{
		List<String> duplicate = null;
		
		for (String key : keys)
			for (List<String> existingKeys : ruleTypes.keySet())
				if (existingKeys.contains(key.toLowerCase()))
					duplicate = existingKeys;
		
		if (duplicate != null)
			ruleTypes.remove(duplicate);
		
		List<String> sanitizedKeys = new ArrayList<String>();
		
		for (int i = 0 ; i < keys.size() ; i++)
		{
			String key = keys.get(i);
			sanitizedKeys.add(key.toLowerCase());
		}
		
		ruleTypes.put(sanitizedKeys, rule);
	}
	
	/**
	 * Gets the class extending {@link RuleBase} that matches the given key (case insensitive) if one exists.
	 * 
	 * @param key - The key that should map to a particular rule type.
	 * 
	 * @return - The class extending {@link RuleBase} matching the key, if one exists.
	 */
	public static Class<? extends RuleBase> getRuleTypeMatchingKey(String key)
	{
		for (List<String> keys : ruleTypes.keySet())
			if (keys.contains(key.toLowerCase()))
				return ruleTypes.get(keys);
		return null;
	}
	
	/**
	 * Parses a {@link RuleBase} out of a key and nullable argument. Throws an exception if there was any fault.
	 * 	
	 * @param key - The key for the rule (e.g. "levelmax")
	 * @param arg - The argument for the rule (e.g. 50) or null if an argument isn't necessary
	 * 
	 * @return - The {@link RuleBase} constructed
	 * 
	 * @throws Exception when there was any kind of problem with the parsing. The cause message will have information about the fault.
	 */
	public static RuleBase parse(String key, String arg) throws Exception
	{
		if (arg == null)
			arg = "";
		
		Class<? extends RuleBase> ruleType = getRuleTypeMatchingKey(key);
		
		if (ruleType == null)
			throw new Exception("No rule found for key: " + key);
		
		try
		{
			return ruleType.getDeclaredConstructor(String.class).newInstance(arg);
		}
		catch (Exception e)
		{
			throw new Exception(e.getCause().getMessage());
		}
	}
}
