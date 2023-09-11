package com.hiroku.tournaments.api.requirements;

import com.pixelmonmod.api.pokemon.requirement.AbstractBooleanPokemonRequirement;
import com.pixelmonmod.api.requirement.Requirement;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class RentalRequirement extends AbstractBooleanPokemonRequirement {
    private static final String KEY = "rental";
    private static final Set<String> KEYS = new HashSet<>(Collections.singleton(KEY));

    public RentalRequirement() {
        super(KEYS);
    }

    public RentalRequirement(boolean value) {
        super(KEYS, value);
    }

    @Override
    public Requirement<Pokemon, PixelmonEntity, Boolean> createInstance(Boolean value) {
        return new RentalRequirement(value);
    }

    @Override
    public boolean isDataMatch(Pokemon pokemon) {
        return pokemon.hasFlag(KEY);
    }

    @Override
    public void applyData(Pokemon pokemon) {
        if (this.value) {
            pokemon.addFlag(KEY);
        } else {
            pokemon.removeFlag(KEY);
        }
    }

    public static boolean is(Pokemon pokemon) {
        return pokemon.hasFlag(KEY);
    }
}
