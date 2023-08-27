package com.hiroku.tournaments.rules.player;

import com.happyzleaf.tournaments.text.Text;
import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

/**
 * Rule demanding that all Pokémon be below or equal to a particular level
 *
 * @author Hiroku
 */
public class LevelMax extends PlayerRule {
	/**
	 * The level maximum (inclusive)
	 */
	public int levelMax;

	public LevelMax(String arg) throws Exception {
		super(arg);

		levelMax = Integer.parseInt(arg);

		if (levelMax < 1 || levelMax > PixelmonConfigProxy.getGeneral().getMaxLevel())
			throw new Exception("Invalid level '" + arg + "'; must be between " + 1 + " and " + PixelmonConfigProxy.getGeneral().getMaxLevel());
	}

	@Override
	public boolean passes(PlayerEntity player, PlayerPartyStorage storage) {
		return storage.getHighestLevel() <= levelMax;
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextFormatting.GOLD, "Level Maximum: ", TextFormatting.DARK_AQUA, levelMax);
	}

	@Override
	public boolean duplicateAllowed(RuleBase other) {
		return false;
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}

	@Override
	public Text getBrokenRuleText(PlayerEntity player) {
		return Text.of(TextFormatting.DARK_AQUA, player.getName(), TextFormatting.RED, " exceeded the level maximum!");
	}

	@Override
	public String getSerializationString() {
		return "levelmax:" + levelMax;
	}
}
