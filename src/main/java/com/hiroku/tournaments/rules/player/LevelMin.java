package com.hiroku.tournaments.rules.player;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.pixelmonmod.pixelmon.config.PixelmonConfig;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;

/**
 * Rule demanding that all Pokémon be above or equal to a particular level
 *
 * @author Hiroku
 */
public class LevelMin extends PlayerRule
{
	/** The level minimum (inclusive) */
	public int levelMin;
	
	public LevelMin(String arg) throws Exception
	{
		super(arg);
		
		levelMin = Integer.parseInt(arg);
		
		if (levelMin < 1 || levelMin > PixelmonConfig.maxLevel)
			throw new Exception("Invalid level '" + arg + "'; must be between " + 1 + " and " + PixelmonConfig.maxLevel);
	}
	
	@Override
	public boolean passes(Player player, PlayerPartyStorage storage)
	{
		return storage.getLowestLevel() >= levelMin;
	}

	@Override
	public Text getDisplayText()
	{
		return Text.of(TextColors.GOLD, "Level Minimum: ", TextColors.DARK_AQUA, levelMin);
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
		return Text.of(TextColors.DARK_AQUA, player.getName(), TextColors.RED, " went below the level minimum!");
	}

	@Override
	public String getSerializationString()
	{
		return "levelmin:" + levelMin;
	}
}
