package com.hiroku.tournaments.util;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.minecraft.item.ItemStack;

public class PokemonUtils {
	public static final String FORGE_KEY = "ForgeData";

	/**
	 * Converts a {@link PokemonSpecification} to a string
	 */
	public static String serializePokemonSpec(PokemonSpecification spec) {
		String line = "";
		if (spec.name != null)
			line += spec.name;
		if (spec.ability != null)
			line += ",ab:" + spec.ability;
		else if (spec.shiny != null && spec.shiny)
			line += ",s";
		else if (spec.ball != null)
			line += ",ba:" + spec.ball;
		else if (spec.boss != null)
			line += "boss:" + spec.boss;
		else if (spec.form != null)
			line += "form:" + spec.form;
		else if (spec.gender != null)
			line += "gender:" + spec.gender;
		else if (spec.growth != null)
			line += "growth:" + spec.growth;
		else if (spec.level != null)
			line += "level:" + spec.level;
		else if (spec.nature != null)
			line += "nature:" + spec.nature;
		return line;
	}

	public static void stripHeldItem(User user, Pokemon pokemon) {
		ItemStack heldItem = pokemon.getHeldItem();
		if (heldItem != ItemStack.EMPTY)
			user.getInventory().offer((org.spongepowered.api.item.inventory.ItemStack) (Object) heldItem);
	}
}