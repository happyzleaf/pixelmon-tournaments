package fr.pokepixel.pokewiki.translation;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pixelmonmod.api.registry.RegistryValue;
import com.pixelmonmod.pixelmon.api.pokemon.species.Pokedex;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.util.ITranslatable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Translation {
	private static final Logger LOGGER = LogManager.getLogger("Translation");
	private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

	private final String lang;

	private final Map<String, String> translations = new HashMap<>();

	private final List<String> cachedNames = new ArrayList<>();
	private final Map<String, Species> cachedPokedex = new HashMap<>();

	public Translation(String lang, String... modIDs) {
		this.lang = lang;

		for (String modId : modIDs) {
			load(modId);
		}

		for (Species species : Pokedex.actualPokedex) {
			String name = getTranslation(species.getTranslationKey(), null);
			if (name == null) {
				continue;
			}

			cachedNames.add(name);
			cachedPokedex.put(name.toLowerCase(), species);
		}
	}

	private void load(String modId) {
		LOGGER.info("Trying to read translations of '{}'.", modId);
		Map<String, String> newTranslations = get(modId);
		LOGGER.info("Successfully read {} translations.", newTranslations.size());
		translations.putAll(newTranslations);
	}

	private Map<String, String> get(String modId) {
		String asset = String.format("assets/%s/lang/%s.json", modId, lang.toLowerCase());
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(asset);
		if (is == null) {
			LOGGER.warn("Could not load the asset '{}'.", asset);
			return Collections.emptyMap();
		}

		return GSON.fromJson(new InputStreamReader(is, StandardCharsets.UTF_8), new TypeToken<MapIgnoreDuplicates<String, String>>() {
		}.getType());
	}

	public String getTranslation(String key, String dfault) {
		return translations.getOrDefault(key, dfault);
	}

	public String getTranslation(ITranslatable translatable) {
		return getTranslation(translatable.getTranslationKey(), translatable.getLocalizedName());
	}

	public Species getSpecies(String name) {
		name = name.toLowerCase();
		return cachedPokedex.containsKey(name) ? cachedPokedex.get(name) : PixelmonSpecies.get(name).flatMap(RegistryValue::getValue).orElse(null);
	}

	public List<String> getSpeciesNames() {
		return cachedNames;
	}
}
