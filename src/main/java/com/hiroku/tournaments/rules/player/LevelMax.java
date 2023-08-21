package com.hiroku.tournaments.rules.player;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;

/**
 * Rule demanding that all Pokémon be below or equal to a particular level
 *
 * @author Hiroku
 */
public class LevelMax extends PlayerRule
{
	/** The level maximum (inclusive)*/
	public int levelMax;
	
	public LevelMax(String arg) throws Exception
	{
		super(arg);
		
		levelMax = Integer.parseInt(arg);
		
		if (levelMax < 1 || levelMax > PixelmonConfig.maxLevel)
			throw new Exception("Invalid level '" + arg + "'; must be between " + 1 + " and " + PixelmonConfig.maxLevel);
	}

	@Override
	public boolean passes(Player player, PlayerPartyStorage storage)
	{
		return storage.getHighestLevel() <= levelMax;
	}

	@Override
	public Text getDisplayText()
	{
		return Text.of(TextColors.GOLD, "Level Maximum: ", TextColors.DARK_AQUA, levelMax);
	}

	@Override
	public boolean duplicateAllowed(RuleBase other)
	{
		return false;
	}

	@Override
	public boolean visibleToAll()
	{
		return true;
	}

	@Override
	public Text getBrokenRuleText(Player player)
	{
		return Text.of(TextColors.DARK_AQUA, player.getName(), TextColors.RED, " exceeded the level maximum!");
	}

	@Override
	public String getSerializationString()
	{
		return "levelmax:" + levelMax;
	}
}
