package com.hiroku.tournaments.util;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;

public class PokemonUtils {
	private static PokemonSpecification rental = null;

	public static boolean isRental(Pokemon pokemon) {
		if (rental == null) {
			rental = PokemonSpecificationProxy.create("rental");
		}

		return rental.matches(pokemon);
	}
}
