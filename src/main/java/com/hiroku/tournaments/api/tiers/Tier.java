package com.hiroku.tournaments.api.tiers;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

/**
 * A tier of Pokémon. Instances of these should be added to Tier.tiers to be usable in the rules
 * relating to tiers/classes of Pokémon
 *
 * @author Hiroku
 */
public class Tier {
	/**
	 * Absolutely no legendary Pokémon
	 */
	public static final Tier NO_LEGENDARIES = new Tier("NOLEGENDARIES", "No Legendaries", p -> !PixelmonSpecies.isLegendary(p.getSpecies()));
	/**
	 * Only legendary Pokémon
	 */
	public static final Tier LEGENDARIES = new Tier("LEGENDARIES", "Legendaries", p -> PixelmonSpecies.isLegendary(p.getSpecies()));
	/**
	 * Generation 1 Pokémon
	 */
	public static final Tier GEN1 = new Tier("GEN1", "Generation 1", p -> p.getSpecies().getGeneration() == 1);
	/**
	 * Generation 2 Pokémon
	 */
	public static final Tier GEN2 = new Tier("GEN2", "Generation 2", p -> p.getSpecies().getGeneration() == 2);
	/**
	 * Generation 3 Pokémon
	 */
	public static final Tier GEN3 = new Tier("GEN3", "Generation 3", p -> p.getSpecies().getGeneration() == 3);
	/**
	 * Generation 4 Pokémon
	 */
	public static final Tier GEN4 = new Tier("GEN4", "Generation 4", p -> p.getSpecies().getGeneration() == 4);
	/**
	 * Generation 5 Pokémon
	 */
	public static final Tier GEN5 = new Tier("GEN5", "Generation 5", p -> p.getSpecies().getGeneration() == 5);
	/**
	 * Generation 6 Pokémon
	 */
	public static final Tier GEN6 = new Tier("GEN6", "Generation 6", p -> p.getSpecies().getGeneration() == 6);
	/**
	 * Generation 7 Pokémon
	 */
	public static final Tier GEN7 = new Tier("GEN7", "Generation 7", p -> p.getSpecies().getGeneration() == 7);
	/**
	 * Generation 8 Pokémon
	 */
	public static final Tier GEN8 = new Tier("GEN8", "Generation 8", p -> p.getSpecies().getGeneration() == 8);
	/**
	 * Fully Evolved, including non-evolving Pokémon like Luvdisc
	 */
	public static final Tier FE = new Tier("FE", "Fully-Evolved", p -> p.getForm().getEvolutions().isEmpty());
	/**
	 * Not-Fully Evolved, but including middle-evolutions like Ivysaur
	 */
	public static final Tier NFE = new Tier("NFE", "Not-Fully-Evolved", p -> !p.getForm().getEvolutions().isEmpty());
	/**
	 * Little cup - not-evolved, but able to evolve. e.g. Bulbasaur is acceptable, Pachirisu is not
	 */
	public static final Tier LC = new Tier("LC", "Little Cup", p -> !p.getForm().getEvolutions().isEmpty() && p.getForm().getPreEvolutions().size() == 0);
	/**
	 * Has strictly 1 type
	 */
	public static final Tier MONOTYPE = new Tier("MONOTYPE", "Monotype", p -> p.getForm().getTypes().size() == 1);
	/**
	 * Has strictly 2 types
	 */
	public static final Tier DUALTYPE = new Tier("DUALTYPE", "Dualtype", p -> p.getForm().getTypes().size() == 2);
	//NU, RU, UU, OU, UBER - Waiting on lists of each

	public static final List<Tier> tiers = new ArrayList<>();

	public static void loadDefaultTiers() {
		tiers.add(NO_LEGENDARIES);
		tiers.add(LEGENDARIES);
		tiers.add(GEN1);
		tiers.add(GEN2);
		tiers.add(GEN3);
		tiers.add(GEN4);
		tiers.add(GEN5);
		tiers.add(GEN6);
		tiers.add(GEN7);
		tiers.add(GEN8);
		tiers.add(FE);
		tiers.add(NFE);
		tiers.add(LC);
		tiers.add(MONOTYPE);
		tiers.add(DUALTYPE);
	}

	@Nullable
	public static Tier parse(String name) {
		for (Tier tier : tiers)
			if (tier.key.equalsIgnoreCase(name) || tier.displayName.equalsIgnoreCase(name))
				return tier;
		return null;
	}

	/**
	 * The raw key used to refer to this tier. This is used for serialization and parsing.
	 */
	public final String key;
	/**
	 * The display name of the tier. e.g. Fully-evolved.
	 */
	public final String displayName;
	/**
	 * The {@link Predicate} condition, given {@link Pokemon}, for what qualifies a Pokémon for this tier.
	 */
	public final Predicate<Pokemon> condition;

	public Tier(String key, String displayName, Predicate<Pokemon> condition) {
		this.key = key;
		this.displayName = displayName;
		this.condition = condition;
	}

	/**
	 * Filters out the Pokémon from the given pool based off this tier specification.
	 */
	public void filter(List<Species> pool) {
		pool.removeIf(p ->
		{
			try {
				return !condition.test(PokemonFactory.create(p));
			} catch (NoSuchElementException nsee) {
				// This is for base stats returning empties. Not sure why.
				return true;
			}
		});
	}
}
