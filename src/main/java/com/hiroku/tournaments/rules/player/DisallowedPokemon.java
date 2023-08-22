package com.hiroku.tournaments.rules.player;

import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.api.tiers.Tier;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.util.helpers.CollectionHelper;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;

public class DisallowedPokemon extends PlayerRule {
	public ArrayList<String> pokemons = new ArrayList<>();
	public ArrayList<Tier> tiers = new ArrayList<>();

	public DisallowedPokemon(String arg) throws Exception {
		super(arg);

		String[] splits = arg.split(",");
		for (String name : splits) {
			String str = name.replace("_", " ");
			if (EnumSpecies.hasPokemon(str))
				pokemons.add(str);
			else if (Tier.parse(name) != null)
				tiers.add(Tier.parse(name));
			else
				throw new Exception("Invalid Pokémon or tier. These are case sensitive, and without spaces. e.g. Pikachu. Use _ instead of space");
		}
	}

	@Override
	public boolean passes(Player player, PlayerPartyStorage storage) {
		for (Pokemon pokemon : storage.getTeam())
			if (pokemons.contains(pokemon.getSpecies().getPokemonName()) || pokemons.contains(pokemon.getSpecies().getLocalizedName()))
				return false;
			else if (CollectionHelper.find(tiers, tier -> tier.condition.test(pokemon)) != null)
				return false;
		return true;
	}

	@Override
	public Text getDisplayText() {
		Text.Builder builder = Text.builder();

		if (pokemons.size() > 0)
			builder.append(Text.of(TextColors.DARK_AQUA, pokemons.get(0)));
		if (tiers.size() > 0)
			if (pokemons.isEmpty())
				builder.append(Text.of(TextColors.DARK_AQUA, tiers.get(0).key));
			else
				builder.append(Text.of(TextColors.GOLD, ", ", TextColors.DARK_AQUA, tiers.get(0).key));
		for (int i = 1; i < pokemons.size(); i++)
			builder.append(Text.of(TextColors.GOLD, ", ", TextColors.DARK_AQUA, pokemons.get(i)));
		for (int i = 1; i < tiers.size(); i++)
			builder.append(Text.of(TextColors.GOLD, ", ", TextColors.DARK_AQUA, tiers.get(i)));
		return Text.of(TextColors.GOLD, "Disallowed Pokémon/tier(s): ", builder.build());
	}

	@Override
	public Text getBrokenRuleText(Player player) {
		return Text.of(TextColors.DARK_AQUA, player.getName(), TextColors.RED, " has a disallowed Pokémon!");
	}

	@Override
	public boolean duplicateAllowed(RuleBase other) {
		// Transfers this rule's Pokémon list into the rule that's about to replace it.
		DisallowedPokemon disallowed = (DisallowedPokemon) other;
		for (String pokemon : this.pokemons)
			if (!disallowed.pokemons.contains(pokemon))
				disallowed.pokemons.add(pokemon);
		for (Tier tier : this.tiers)
			if (!disallowed.tiers.contains(tier))
				disallowed.tiers.add(tier);

		return false;
	}

	@Override
	public String getSerializationString() {
		String serialize = pokemons.get(0);
		for (int i = 1; i < pokemons.size(); i++)
			serialize += "," + pokemons.get(i);
		return "disallowedpokemon:" + serialize;
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}
}
