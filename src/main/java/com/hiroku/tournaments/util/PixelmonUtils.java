package com.hiroku.tournaments.util;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleProperty;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.api.rules.PropertyValue;

import java.lang.reflect.Field;
import java.util.Map;

public class PixelmonUtils {
    private static Field properties_f = null;
    private static PokemonSpecification rental = null;

    public static boolean isRental(Pokemon pokemon) {
        if (rental == null) {
            rental = PokemonSpecificationProxy.create("rental");
        }

        return rental.matches(pokemon);
    }

    public static Map<BattleProperty<?>, PropertyValue<?>> getBRProperties(BattleRules br) {
        try {
            if (properties_f == null) {
                properties_f = BattleRules.class.getDeclaredField("properties");
                properties_f.setAccessible(true);
            }

            //noinspection unchecked
            return (Map<BattleProperty<?>, PropertyValue<?>>) properties_f.get(br);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Bug fix for weird pixelmon regression in {@link BattleRules#exportText()}
     * @param br The {@link BattleRules} to export.
     * @return The exported {@link BattleRules} as {@link String}.
     */
    public static String exportBRText(BattleRules br) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<BattleProperty<?>, PropertyValue<?>> entry : getBRProperties(br).entrySet()) {
            if (builder.length() != 0) {
                builder.append("\n");
            }

            builder.append(entry.getKey().getId()).append(": ").append(entry.getValue().get());
        }
        return builder.toString();
    }
}
