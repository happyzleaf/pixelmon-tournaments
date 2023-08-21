package com.hiroku.tournaments.api.rule.types;

import com.hiroku.tournaments.obj.Team;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * Abstract representation of all {@link RuleBase}s that apply specifically to {@link Player}s. These will be
 * checked once per {@link Player}, on an attempted match start.
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
	 * @param player  - The {@link Player} being inspected.
	 * @param storage - The {@link PlayerPartyStorage} for the player.
	 * @param team    - The {@link Team} the player belongs to.
	 * @return - false if the player is breaking the rule, otherwise true.
	 */
	public abstract boolean passes(Player player, PlayerPartyStorage storage);

	/**
	 * The {@link Text} displayed when the rule is broken.
	 *
	 * @param player - The {@link Player} who broke the rule.
	 * @return - The {@link Text} that will appear when it has been found that the given {@link Player} broke this rule.
	 */
	public abstract Text getBrokenRuleText(Player player);
}
