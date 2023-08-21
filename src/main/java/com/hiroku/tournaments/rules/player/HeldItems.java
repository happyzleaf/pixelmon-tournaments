package com.hiroku.tournaments.rules.player;

import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Rule barring (or explicitly allowing) held items on Pokémon.
 *
 * @author Hiroku
 */
public class HeldItems extends PlayerRule {
	/**
	 * Whether or not held items are allowed.
	 */
	public boolean allowHeldItems;

	public HeldItems(String arg) throws Exception {
		super(arg);

		this.allowHeldItems = Boolean.parseBoolean(arg);
	}

	@Override
	public boolean passes(Player player, PlayerPartyStorage storage) {
		if (allowHeldItems)
			return true;
		for (Pokemon pokemon : storage.getTeam())
			if (!pokemon.getHeldItem().isEmpty())
				return false;

		return true;
	}

	@Override
	public Text getBrokenRuleText(Player player) {
		return Text.of(TextColors.DARK_AQUA, player.getName(), TextColors.RED, " has at least one Pokémon with a held item!");
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextColors.GOLD, "Held Items Allowed: ", this.allowHeldItems ? Text.of(TextColors.GREEN, "Yes") : Text.of(TextColors.RED, "No"));
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
	public String getSerializationString() {
		return "helditems:" + allowHeldItems;
	}
}
