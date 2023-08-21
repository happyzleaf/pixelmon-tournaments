package com.hiroku.tournaments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.common.collect.ImmutableMap;
import com.hiroku.tournaments.api.Preset;
import com.hiroku.tournaments.api.reward.RewardBase;
import com.hiroku.tournaments.api.reward.RewardTypeRegistrar;
import com.hiroku.tournaments.api.rule.RuleSet;
import com.hiroku.tournaments.api.rule.RuleTypeRegistrar;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.obj.Zone;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;

/**
 * Static manager of all the rule+reward+zone presets. These are located: ./config/tournaments/presets/*.txt
 * 
 * @author Hiroku
 */
public class Presets
{
	/** Root directory for setting presets. */
	public static final String PATH = "config/tournaments/presets/";
	
	/** The mapping from preset name to the {@link Preset} */
	private static HashMap<String, Preset> presets = new HashMap<String, Preset>();
	
	/** Saves the preset with the given name. This will save into ./config/tournaments/presets/{name}.txt. */
	public static void savePreset(String name)
	{
		File file = new File(PATH + name + ".txt");
		try
		{
			if (!file.exists())
				file.createNewFile();
			
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));	
			
			Preset preset = getPreset(name);
			if (preset != null)
			{
				pw.println("rules {");
				for (RuleBase rule : preset.ruleSet.rules)
					pw.println(rule.getSerializationString());
				pw.println("}");
				pw.println("rewards {");
				for (RewardBase reward : preset.rewards)
					pw.println(reward.getSerializationString());
				pw.println("}");
				pw.println("zones {");
				for (Zone zone : preset.zones)
					pw.println(zone.uid);
				pw.println("}");
				if (preset.battleRules != null)
					pw.println("battlerules=" + preset.battleRules.exportText().replaceAll("\n", ","));
				pw.flush();
			}
			else
				Tournaments.log("Unknown error while saving preset: " + name);
			
			pw.close();
		}
		catch (IOException ioe)
		{
			Tournaments.log("Error saving preset: " + name);
			ioe.printStackTrace();
		}
	}
	
	/** Loads all of the presets saved under ./config/tournaments/presets/* into memory. */
	public static void load()
	{
		presets.clear();
		
		Tournaments.log("Loading default presets...");
		
		Preset standard = new Preset(
				new RuleSet("legendaries:false", "partycount:3", "healthtotal:2", "battletype:single", "levelmax:50"),
				new ArrayList<RewardBase>(), new ArrayList<Zone>());
		
		presets.put("Standard", standard);
		Tournaments.log("Loaded default preset: Standard");
		// Add more probably maybe ok unlikely
		
		Tournaments.log("Loading presets from config/tournaments/presets/ ...");

		File dir = new File(PATH);
		dir.mkdirs();
		for (String fileName : dir.list())
		{
			if (fileName.toLowerCase().endsWith(".txt"))
			{
				try
				{
					String name = fileName.replaceAll(".txt", "");
					
					BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dir, fileName)), StandardCharsets.UTF_8));
					
					ArrayList<String> linesList = new ArrayList<String>();
					String s = null;
					while ((s = br.readLine()) != null)
					{
						if (s.trim().startsWith("//") || s.trim().equals("") || s.trim().equals("{") || s.trim().equals("}"))
							continue;
						linesList.add(s.trim());
					}
					
					String[] args = new String[linesList.size()];
					args = linesList.toArray(args);
					
					if (getMatchingKey(name) != null)
						Tournaments.log("Duplicate presets for name: " + name);
					else
					{
						int ruleRewardZone = -1;
						RuleSet ruleset = new RuleSet();
						ArrayList<RewardBase> rewards = new ArrayList<RewardBase>();
						ArrayList<Zone> zones = new ArrayList<Zone>();
						BattleRules battleRules = new BattleRules();
						
						for (String line : linesList)
						{
							if (line.trim().toLowerCase().startsWith("battlerules"))
								battleRules.importText(line.split("=")[1].trim().replaceAll(",", "\n"));
							else if (line.trim().toLowerCase().startsWith("rules"))
								ruleRewardZone = 0;
							else if (line.trim().toLowerCase().startsWith("rewards"))
								ruleRewardZone = 1;
							else if (line.trim().toLowerCase().startsWith("zones"))
								ruleRewardZone = 2;
							else
							{
								if (ruleRewardZone == -1)
									continue;
								try
								{
									String key = line.trim();
									String arg = "";
									
									if (key.contains(":"))
									{
										arg = key.substring(key.indexOf(":") + 1);
										key = key.split(":")[0];
									}
									
									if (ruleRewardZone == 0)
										ruleset.rules.add(RuleTypeRegistrar.parse(key, arg));
									else if (ruleRewardZone == 1)
										rewards.add(RewardTypeRegistrar.parse(key, arg));
									else if (ruleRewardZone == 2)
									{
										try
										{
											int uid = Integer.parseInt(key);
											Zone zone = Zones.INSTANCE.getZone(uid);
											if (zone != null)
												zones.add(zone);
										}
										catch (NumberFormatException nfe)
										{
											;
										}
									}
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
						}
						Tournaments.log("Loaded preset: " + fileName);
						presets.put(name, new Preset(ruleset, rewards, zones));
					}
					br.close();
				}
				catch (Exception e)
				{
					Tournaments.log("Problem loading preset: " + fileName);
					e.printStackTrace();
				}
			}
		}
		if (presets.isEmpty())
			Tournaments.log("No presets to load.");
		else
			Tournaments.log("Successfully loaded " + presets.size() + " preset" + (presets.size() == 1 ? "." : "s."));
	}
	
	/** 
	 * Gets the case-precise preset name for the case insensitive argument. This is intended for other functions in this class. 
	 * The idea is that preset names should maintain their precise case, e.g. "OU-Normal" instead of the case dropped "ou-normal".
	 * This function's purpose is to simply get the precise case form of the argument, if one exists in the preset map. 
	 */
	public static String getMatchingKey(String name)
	{
		for (String existingName : presets.keySet())
			if (existingName.equalsIgnoreCase(name))
				return existingName;
		return null;
	}
	
	/** Returns the pre-defined {@link Preset} saved under the given name, case insensitive. */
	public static Preset getPreset(String name)
	{
		String key = getMatchingKey(name);
		if (key == null)
			return null;
		return presets.get(key);
	}
	
	/** 
	 * Gets all of the current presets. (Immutable. To change a preset, you must use Presets.setPreset(String, Preset) ).
	 * This is to ensure edits are saved. Nothing personal, I just don't trust you.
	 * */
	public static ImmutableMap<String, Preset> getPresets()
	{
		return ImmutableMap.copyOf(presets);
	}
	
	/** Renames a preset from the given old name to a given new name. */
	public static void renamePreset(String oldName, String newName)
	{
		String key = getMatchingKey(oldName);
		if (key != null)
		{
			Preset preset = presets.get(key);
			presets.remove(key);
			presets.put(newName, preset);
			Tournaments.log("Renamed preset " + key + " to " + newName);
			savePreset(newName);
		}
	}
	
	/** Sets the {@link Preset} for the given name, case insensitive. */
	public static void setPreset(String name, Preset preset)
	{
		String key = getMatchingKey(name);
		boolean editing = true;
		if (key == null)
		{
			editing = false;
			key = name;
		}
		
		presets.remove(key);
		presets.put(key, preset);
		savePreset(key);
		if (editing)
			Tournaments.log("Set preset: " + key);
		else
			Tournaments.log("Added new preset: " + key);
	}
	
	/** Deletes the preset of the given name. */
	public static void deletePreset(String name)
	{
		String key = getMatchingKey(name);
		if (key == null)
			return;
		presets.remove(key);
		File file = new File(PATH + key + ".json");
		if (file.exists())
			file.delete();
		Tournaments.log("Deleted preset: " + key + ".json");
	}
}
