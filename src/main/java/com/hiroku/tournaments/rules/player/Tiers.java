package com.hiroku.tournaments.rules.player;

import com.happyzleaf.tournaments.text.Text;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.api.tiers.Tier;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Rule restricting what kind of Pokémon can be used.
 *
 * @author Hiroku
 */
public class Tiers extends PlayerRule {
	/**
	 * The list of {@link Tier} that define the Pokémon allowed in the tournament.
	 */
	public List<Tier> tiers = new ArrayList<>();

	public Tiers(String arg) throws Exception {
		super(arg);

		String[] splits = arg.split(",");

		for (String name : splits) {
			Tier tier = Tier.parse(name);
			if (tier != null)
				tiers.add(tier);
		}

		if (tiers.isEmpty())
			throw new IllegalArgumentException("No valid tiers provided");
	}

	@Override
	public boolean passes(PlayerEntity player, PlayerPartyStorage storage) {
		for (Pokemon pokemon : storage.getTeam())
			for (Tier tier : tiers)
				if (!tier.condition.test(pokemon))
					return false;

		return true;
	}

	@Override
	public Text getBrokenRuleText(PlayerEntity player) {
		return Text.of(TextFormatting.DARK_AQUA, player.getName(), TextFormatting.RED, " brought illegal Pokémon to the tournament!");
	}

	@Override
	public boolean duplicateAllowed(RuleBase other) {
		for (Tier tier : tiers)
			if (!((Tiers) other).tiers.contains(tier))
				((Tiers) other).tiers.add(tier);
		return false;
	}

	@Override
	public String getSerializationString() {
		StringBuilder line = new StringBuilder("tiers:" + tiers.get(0).key);
		for (int i = 1; i < tiers.size(); i++)
			line.append(",").append(tiers.get(i).key);
		return line.toString();
	}

	@Override
	public Text getDisplayText() {
		Text.Builder builder = Text.builder().append(Text.of(TextFormatting.GOLD, "Allowed Pokémon Tiers: \n",
				TextFormatting.DARK_AQUA, "    ", tiers.get(0).displayName));
		for (int i = 1; i < tiers.size(); i++)
			builder.append(Text.of("\n    ", TextFormatting.DARK_AQUA, tiers.get(i).displayName));
		return builder.build();
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}

	@Override
	public boolean canRuleBeAdded(Tournament tournament, RuleBase rule) {
		if (rule instanceof RandomPokemon)
			synchronize((RandomPokemon) rule);

		return true;
	}

	public void synchronize(RandomPokemon other) {
		List<Tier> combined = new ArrayList<>();
		for (Tier tier : other.tiers)
			if (!combined.contains(tier))
				combined.add(tier);
		for (Tier tier : tiers)
			if (!combined.contains(tier))
				combined.add(tier);
		other.tiers.clear();
		other.tiers.addAll(combined);
		tiers.clear();
		tiers.addAll(combined);
	}
}
