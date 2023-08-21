package com.hiroku.tournaments.util;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import net.minecraft.item.ItemStack;
import org.spongepowered.api.entity.living.player.User;

public class PokemonUtils {
	public static final String FORGE_KEY = "ForgeData";

	/**
	 * Converts a {@link PokemonSpec} to a string
	 */
	public static String serializePokemonSpec(PokemonSpec spec) {
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