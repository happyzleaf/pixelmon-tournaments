package com.hiroku.tournaments.rules.player;

import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.abilities.AbilityBase;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Rule barring specific abilities. Adding new instances of this rule stacks the forbidden rules into a larger list.
 *
 * @author Hiroku
 */
public class DisallowedAbility extends PlayerRule {
	/**
	 * The list of all abilities (represented by their classes) that are not allowed.
	 */
	public ArrayList<String> abilities = new ArrayList<>();

	public DisallowedAbility(String arg) throws Exception {
		super(arg);

		String[] splits = arg.split(",");
		for (String name : splits) {
			Optional<AbilityBase> optAbility = AbilityBase.getAbility(name);
			if (optAbility.isPresent())
				abilities.add(name);
			else
				throw new Exception("Invalid ability. These are case sensitive, and without spaces. e.g. SwiftSwim");
		}
	}

	@Override
	public boolean passes(Player player, PlayerPartyStorage storage) {
		for (Pokemon pokemon : storage.getTeam())
			if (abilities.contains(pokemon.getAbility().getName()))
				return false;

		return true;
	}

	@Override
	public Text getBrokenRuleText(Player player) {
		return Text.of(TextColors.DARK_AQUA, player.getName(), TextColors.RED, " has a Pokémon with a disallowed ability!");
	}

	@Override
	public Text getDisplayText() {
		Text.Builder builder = Text.builder();
		builder.append(Text.of(TextColors.DARK_AQUA, abilities.get(0)));
		for (int i = 1; i < abilities.size(); i++)
			builder.append(Text.of(TextColors.GOLD, ", ", TextColors.DARK_AQUA, abilities.get(i)));
		return Text.of(TextColors.GOLD, "Disallowed ability(s): ", builder.build());
	}

	@Override
	public boolean duplicateAllowed(RuleBase other) {
		// Transfers this rule's ability list into the rule that's about to replace it.
		DisallowedAbility disallowed = (DisallowedAbility) other;
		for (String ability : this.abilities)
			if (!disallowed.abilities.contains(ability))
				disallowed.abilities.add(ability);

		return false;
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}

	@Override
	public String getSerializationString() {
		String serialize = abilities.get(0);
		for (int i = 1; i < abilities.size(); i++)
			serialize += "," + abilities.get(i);
		return "disallowedabilities:" + serialize;
	}
}
