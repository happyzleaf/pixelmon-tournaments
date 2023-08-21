package fr.pokepixel.pokewiki.util;

import com.google.common.base.Preconditions;
import fr.pokepixel.pokewiki.PokeWiki;
import fr.pokepixel.pokewiki.data.CustomInfo;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

public class GsonUtils {
	public static void writeJson(CustomInfo info) {
		try (PrintWriter writer = new PrintWriter(PokeWiki.CUSTOM_INFO_PATH.toFile(), "UTF-8")) {
			PokeWiki.GSON.toJson(info, writer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static CustomInfo readJson() {
		try (Reader reader = new InputStreamReader(Files.newInputStream(PokeWiki.CUSTOM_INFO_PATH), StandardCharsets.UTF_8)) {
			return PokeWiki.GSON.fromJson(reader, CustomInfo.class);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static HashMap<String, List<CustomInfo.CustomSpawnPokemonInfo>> getAllCustom() {
		return Preconditions.checkNotNull(readJson()).getCustomPokemonList();
	}
}
