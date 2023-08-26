package com.hiroku.tournaments.rules.player;

import com.happyzleaf.tournaments.Text;
import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

/**
 * Rule barring (or explicitly allowing) held items on Pokémon.
 *
 * @author Hiroku
 */
public class HeldItems extends PlayerRule {
	/**
	 * Whether held items are allowed.
	 */
	public boolean allowHeldItems;

	public HeldItems(String arg) throws Exception {
		super(arg);

		this.allowHeldItems = Boolean.parseBoolean(arg);
	}

	@Override
	public boolean passes(PlayerEntity player, PlayerPartyStorage storage) {
		if (allowHeldItems)
			return true;
		for (Pokemon pokemon : storage.getTeam())
			if (!pokemon.getHeldItem().isEmpty())
				return false;

		return true;
	}

	@Override
	public Text getBrokenRuleText(PlayerEntity player) {
		return Text.of(TextFormatting.DARK_AQUA, player.getName(), TextFormatting.RED, " has at least one Pokémon with a held item!");
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextFormatting.GOLD, "Held Items Allowed: ", this.allowHeldItems ? Text.of(TextFormatting.GREEN, "Yes") : Text.of(TextFormatting.RED, "No"));
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
