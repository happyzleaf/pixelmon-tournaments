package com.hiroku.tournaments.api.rule.types;

import com.happyzleaf.tournaments.Text;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Abstract representation of all {@link RuleBase}s that apply specifically to {@link PlayerEntity}s. These will be
 * checked once per {@link PlayerEntity}, on an attempted match start.
 *
 * @author Hiroku
 */
public abstract class PlayerRule extends RuleBase {
	public PlayerRule(String arg) throws Exception {
		super(arg);
	}

	/**
	 * Definition of passing or failing the rule.
	 *
	 * @param player  - The {@link PlayerEntity} being inspected.
	 * @param storage - The {@link PlayerPartyStorage} for the player.
	 * @return - false if the player is breaking the rule, otherwise true.
	 */
	public abstract boolean passes(PlayerEntity player, PlayerPartyStorage storage);

	/**
	 * The {@link Text} displayed when the rule is broken.
	 *
	 * @param player - The {@link PlayerEntity} who broke the rule.
	 * @return - The {@link Text} that will appear when it has been found that the given {@link PlayerEntity} broke this rule.
	 */
	public abstract Text getBrokenRuleText(PlayerEntity player);
}
