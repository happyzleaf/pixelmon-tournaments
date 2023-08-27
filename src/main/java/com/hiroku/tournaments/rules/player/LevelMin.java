package com.hiroku.tournaments.rules.player;

import com.happyzleaf.tournaments.text.Text;
import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

/**
 * Rule demanding that all Pokémon be above or equal to a particular level
 *
 * @author Hiroku
 */
public class LevelMin extends PlayerRule {
	/**
	 * The level minimum (inclusive)
	 */
	public int levelMin;

	public LevelMin(String arg) throws Exception {
		super(arg);

		levelMin = Integer.parseInt(arg);

		if (levelMin < 1 || levelMin > PixelmonConfigProxy.getGeneral().getMaxLevel())
			throw new Exception("Invalid level '" + arg + "'; must be between " + 1 + " and " + PixelmonConfigProxy.getGeneral().getMaxLevel());
	}

	@Override
	public boolean passes(PlayerEntity player, PlayerPartyStorage storage) {
		return storage.getLowestLevel() >= levelMin;
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextFormatting.GOLD, "Level Minimum: ", TextFormatting.DARK_AQUA, levelMin);
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
		return Text.of(TextFormatting.DARK_AQUA, player.getName(), TextFormatting.RED, " went below the level minimum!");
	}

	@Override
	public String getSerializationString() {
		return "levelmin:" + levelMin;
	}
}
