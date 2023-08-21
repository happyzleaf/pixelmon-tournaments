package fr.pokepixel.pokewiki.util;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.api.pokemon.requirement.impl.FormRequirement;
import com.pixelmonmod.api.pokemon.requirement.impl.SpeciesRequirement;
import com.pixelmonmod.api.registry.RegistryValue;
import com.pixelmonmod.api.requirement.Requirement;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.drops.ItemWithChance;
import com.pixelmonmod.pixelmon.api.pokemon.drops.PokemonDropInformation;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import com.pixelmonmod.pixelmon.entities.npcs.registry.DropItemRegistry;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import fr.pokepixel.pokewiki.data.CustomInfo;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.text.WordUtils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

import static fr.pokepixel.pokewiki.PokeWiki.*;

public class Utils {
	private static final Map<Species, List<String>> cache = new HashMap<>();

	public static List<String> getDrops(Species species) {
		if (cache.containsKey(species)) {
			return cache.get(species);
		}

		Set<PokemonDropInformation> drops = DropItemRegistry.pokemonDrops.get(species);
		if (drops == null || drops.isEmpty()) {
			cache.put(species, Collections.emptyList());
			return Collections.emptyList();
		}

		List<String> results = new ArrayList<>();
		for (PokemonDropInformation info : drops) {
			for (ItemWithChance drop : info.getDrops()) {
				results.add(TextFormatting.GRAY + drop.getItemStack().getDisplayName().getString() + " " + TextFormatting.WHITE + drop.getMin() + "-" + drop.getMax());
			}
		}

		if (results.isEmpty()) {
			cache.put(species, Collections.emptyList());
			return Collections.emptyList();
		}

		results.add(0, "");
		cache.put(species, results);
		return results;
	}

	public static boolean canSpawnCustom(Pokemon pokemon) {
		if (!customSpawnPokemonInfoListInfo.containsKey(pokemon.getSpecies().getName())) {
			return false;
		}

		for (CustomInfo.CustomSpawnPokemonInfo customSpawnPokemonInfo : customSpawnPokemonInfoListInfo.get(pokemon.getSpecies().getName())) {
			PokemonSpecification spec = PokemonSpecificationProxy.create(customSpawnPokemonInfo.getSpec());
			Requirement<Pokemon, PixelmonEntity, String> formReq = spec.getRequirement(FormRequirement.class).orElse(null);
			if (formReq != null && formReq.getValue() != null) {
				if (pokemon.getSpecies().isDefaultForm(formReq.getValue()) && pokemon.getForm().isDefault()) {
					pokemon.setForm(formReq.getValue());
				}
			}
			if (spec.matches(pokemon)) {
				return true;
			}
		}
		return false;
	}

	public static String getPokemonName(Pokemon pokemon) {
		String formName = pokemon.isDefaultForm() ? null : translation.getTranslation(pokemon.getForm());
		return translation.getTranslation(pokemon) + (formName == null || formName.isEmpty() ? "" : " " + formName);
	}

	public static String getPokemonName(PokemonSpecification spec) {
		Requirement<Pokemon, PixelmonEntity, RegistryValue<Species>> species = spec.getRequirement(SpeciesRequirement.class).orElse(null);
		if (species == null || species.getValue() == null || species.getValue().getKey() == null) {
			return "NULL";
		}

		return species.getValue().getKey();
	}

	public static String getAttackName(String attackName) {
		ImmutableAttack[] attacks = Attack.getAttacks(new String[]{attackName});
		if (attacks.length == 0) {
			return "NULL";
		}

		return translation.getTranslation(attacks[0]);
	}

	public static <T> T readOrCreate(Class<T> clazz, Path path, Supplier<T> factory) {
		try {
			Files.createDirectories(path.getParent());

			if (Files.notExists(path)) {
				T instance = factory.get();
				Files.write(path, GSON.toJson(instance).getBytes(StandardCharsets.UTF_8));
				return instance;
			}

			return GSON.fromJson(Files.newBufferedReader(path), clazz);
		} catch (Exception e) {
			LOGGER.error("There was a problem while trying to load the config in '{}'.", path.toString(), e);
			return null;
		}
	}

	public static Item getItemById(String id) {
		return Registry.ITEM.getOptional(new ResourceLocation(id)).get();
	}

	public static String toHumanName(String name) {
		return WordUtils.capitalizeFully(name.replace("_", " "));
	}

	public static String toHumanName(ResourceLocation res) {
		return toHumanName(res.getPath());
	}
}
