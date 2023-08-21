package fr.pokepixel.pokewiki.data;

import java.util.HashMap;
import java.util.List;

public class CustomInfo {
	private final HashMap<String, List<CustomSpawnPokemonInfo>> customPokemonList;

	public CustomInfo(HashMap<String, List<CustomSpawnPokemonInfo>> customPokemonList) {
		this.customPokemonList = customPokemonList;
	}

	public HashMap<String, List<CustomSpawnPokemonInfo>> getCustomPokemonList() {
		return customPokemonList;
	}

	public static class CustomSpawnPokemonInfo {
		private final String spec;
		private final List<String> info;

		public CustomSpawnPokemonInfo(String spec, List<String> info) {
			this.spec = spec;
			this.info = info;
		}

		public String getSpec() {
			return spec;
		}

		public List<String> getInfo() {
			return info;
		}
	}

}
