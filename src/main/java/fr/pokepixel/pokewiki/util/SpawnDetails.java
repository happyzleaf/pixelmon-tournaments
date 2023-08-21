package fr.pokepixel.pokewiki.util;

import com.google.common.collect.Lists;
import com.pixelmonmod.api.pokemon.requirement.impl.LevelRequirement;
import com.pixelmonmod.api.pokemon.requirement.impl.MaximumLevelRequirement;
import com.pixelmonmod.api.pokemon.requirement.impl.MinimumLevelRequirement;
import com.pixelmonmod.api.requirement.Requirement;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnInfoPokemon;
import com.pixelmonmod.pixelmon.api.world.WeatherType;
import com.pixelmonmod.pixelmon.api.world.WorldTime;
import fr.pokepixel.pokewiki.PokeWiki;
import net.minecraft.block.Block;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static fr.pokepixel.pokewiki.util.ChatColor.translateAlternateColorCodes;

public class SpawnDetails {
	public static List<String> createPokeDetails(SpawnInfoPokemon spawnInfo) {
		List<String> txt = Lists.newArrayList();
		//String txt = "";
		if (spawnInfo.locationTypes.size() > 0) {
			txt.add(translateAlternateColorCodes('&', PokeWiki.lang.spawn.typeOfLocation.replaceFirst("%spawnlocation%", String.join(", ", spawnInfo.stringLocationTypes))));
		}
		Integer level = spawnInfo.getPokemonSpec().getRequirement(LevelRequirement.class).map(Requirement::getValue).orElse(null);
		int minLevel = level != null ? level : spawnInfo.getPokemonSpec().getRequirement(MinimumLevelRequirement.class).map(Requirement::getValue).orElse(spawnInfo.minLevel);
		int maxLevel = level != null ? level : spawnInfo.getPokemonSpec().getRequirement(MaximumLevelRequirement.class).map(Requirement::getValue).orElse(spawnInfo.maxLevel);
		txt.add(translateAlternateColorCodes('&', PokeWiki.lang.spawn.minLevel.replaceFirst("%minlevel%", String.valueOf(minLevel))));
		txt.add(translateAlternateColorCodes('&', PokeWiki.lang.spawn.maxLevel.replaceFirst("%maxlevel%", String.valueOf(maxLevel))));
		if (spawnInfo.heldItems != null) {
			List<String> itemName = Lists.newArrayList();
			spawnInfo.heldItems.forEach(jsonItemStack -> {
				itemName.add(TextFormatting.DARK_AQUA + jsonItemStack.getItemStack().getDisplayName().getString());
			});
			txt.add(translateAlternateColorCodes('&', PokeWiki.lang.spawn.heldItems.replaceFirst("%helditems%", String.join(TextFormatting.YELLOW + ", ", itemName))));
		}

		if (spawnInfo.condition != null) {
			//Time
			if (spawnInfo.condition.times != null && !spawnInfo.condition.times.isEmpty()) {
				txt.add(translateAlternateColorCodes('&', PokeWiki.lang.spawn.times.replaceFirst("%times%", getTimeSpawns(spawnInfo))));
			}
			//Weather
			if (spawnInfo.condition.cachedWeathers != null && !spawnInfo.condition.cachedWeathers.isEmpty()) {
				txt.add(translateAlternateColorCodes('&', PokeWiki.lang.spawn.weathers.replaceFirst("%weathers%", getWeatherSpawns(spawnInfo))));
			}
			//Biomes
			if (spawnInfo.condition.biomes != null && !spawnInfo.condition.biomes.isEmpty()) {
				txt.add(translateAlternateColorCodes('&', PokeWiki.lang.spawn.biomes.replaceFirst("%biomes%", getBiomeSpawns(spawnInfo))));
			}
			//Nearby Blocks
			if (spawnInfo.condition.cachedNeededNearbyBlocks != null && !spawnInfo.condition.cachedNeededNearbyBlocks.isEmpty()) {
				txt.add(translateAlternateColorCodes('&', PokeWiki.lang.spawn.nearbyBlocks.replaceFirst("%nearbyblocks%", getNearbyBlocksSpawns(spawnInfo))));
			}
			if (spawnInfo.condition.cachedBaseBlocks != null && !spawnInfo.condition.cachedBaseBlocks.isEmpty()) {
				txt.add(translateAlternateColorCodes('&', PokeWiki.lang.spawn.baseBlocks.replaceFirst("%baseblocks%", getBaseBlocksSpawns(spawnInfo))));
			}
		}
		//Rarity
		txt.add(translateAlternateColorCodes('&', PokeWiki.lang.spawn.rarity.replaceFirst("%rarity%", String.valueOf(spawnInfo.rarity))));
		return txt;
	}

	public static String getBiomeSpawns(SpawnInfoPokemon info) {
		return info.condition.biomes.stream().map(r -> TextFormatting.DARK_AQUA + Utils.toHumanName(r)).collect(Collectors.joining(TextFormatting.YELLOW + ", "));
//		ArrayList<Biome> allBiomes = new ArrayList<>();
//		for (Biome biome : GameRegistry.findRegistry(Biome.class)) {
//			allBiomes.add(biome);
//		}
//		if (info.condition != null && info.condition.biomes != null && !info.condition.biomes.isEmpty()) {
//			allBiomes.removeIf(biome -> !info.condition.biomes.contains(biome));
//		}
//		if (info.anticondition != null && info.anticondition.biomes != null && !info.anticondition.biomes.isEmpty()) {
//			allBiomes.removeIf(biome -> info.anticondition.biomes.contains(biome));
//		}
//		if (info.compositeCondition != null) {
//			if (info.compositeCondition.conditions != null) {
//				info.compositeCondition.conditions.forEach(condition -> {
//					if (condition.biomes != null && !condition.biomes.isEmpty()) {
//						allBiomes.removeIf(biome -> !condition.biomes.contains(biome));
//					}
//				});
//			}
//			if (info.compositeCondition.anticonditions != null) {
//				info.compositeCondition.anticonditions.forEach(anticondition -> {
//					if (anticondition.biomes != null && anticondition.biomes.isEmpty()) {
//						allBiomes.removeIf(biome -> anticondition.biomes.contains(biome));
//					}
//				});
//			}
//		}
//		Set<Biome> avail = new HashSet<>(allBiomes);
//		ArrayList<String> biomeNames = new ArrayList<>();
//		for (Biome biome : avail) {
//			String biomeName = biome.getCategory().getName();
//			biomeNames.add(TextFormatting.DARK_AQUA + biomeName);
//		}
//		return String.join(TextFormatting.YELLOW + ", ", biomeNames);
	}

	public static String getTimeSpawns(SpawnInfoPokemon info) {
		ArrayList<WorldTime> allTimes = Lists.newArrayList(WorldTime.values());
		if (info.condition != null && info.condition.times != null && !info.condition.times.isEmpty()) {
			allTimes.removeIf(time -> !info.condition.times.contains(time));
		}
		if (info.anticondition != null && info.anticondition.times != null && !info.anticondition.times.isEmpty()) {
			allTimes.removeIf(time -> info.anticondition.times.contains(time));
		}
		if (info.compositeCondition != null) {
			if (info.compositeCondition.conditions != null) {
				info.compositeCondition.conditions.forEach(condition -> {
					if (condition.times != null && !condition.times.isEmpty()) {
						allTimes.removeIf(time -> !condition.times.contains(time));
					}
				});
			}
			if (info.compositeCondition.anticonditions != null) {
				info.compositeCondition.anticonditions.forEach(anticondition -> {
					if (anticondition.times != null && anticondition.times.isEmpty()) {
						allTimes.removeIf(time -> anticondition.times.contains(time));
					}
				});
			}
		}
		Set<WorldTime> avail = new HashSet<>(allTimes);
		ArrayList<String> worldTimeNames = new ArrayList<>();
		for (WorldTime times : avail) {
			worldTimeNames.add(TextFormatting.DARK_AQUA + PokeWiki.translation.getTranslation(times));
		}
		return String.join(TextFormatting.YELLOW + ", ", worldTimeNames);
	}


	public static String getWeatherSpawns(SpawnInfoPokemon info) {
		ArrayList<WeatherType> allWeathers = Lists.newArrayList(WeatherType.values());
		if (info.condition != null && info.condition.cachedWeathers != null && !info.condition.cachedWeathers.isEmpty()) {
			allWeathers.removeIf(weather -> !info.condition.cachedWeathers.contains(weather));
		}
		if (info.anticondition != null && info.anticondition.cachedWeathers != null && !info.anticondition.cachedWeathers.isEmpty()) {
			allWeathers.removeIf(weather -> info.anticondition.cachedWeathers.contains(weather));
		}
		if (info.compositeCondition != null) {
			if (info.compositeCondition.conditions != null) {
				info.compositeCondition.conditions.forEach(condition -> {
					if (condition.cachedWeathers != null && !condition.cachedWeathers.isEmpty()) {
						allWeathers.removeIf(weather -> !condition.cachedWeathers.contains(weather));
					}
				});
			}
			if (info.compositeCondition.anticonditions != null) {
				info.compositeCondition.anticonditions.forEach(anticondition -> {
					if (anticondition.cachedWeathers != null && !anticondition.cachedWeathers.isEmpty()) {
						allWeathers.removeIf(weather -> anticondition.cachedWeathers.contains(weather));
					}
				});
			}
		}
		Set<WeatherType> avail = new HashSet<>(allWeathers);
		ArrayList<String> weatherNames = new ArrayList<>();
		for (WeatherType weathers : avail) {
			weatherNames.add(TextFormatting.DARK_AQUA + PokeWiki.translation.getTranslation(weathers.getTranslationKey(), weathers.getLocalizedName()));
		}
		return String.join(TextFormatting.YELLOW + ", ", weatherNames);
	}

	public static String getNearbyBlocksSpawns(SpawnInfoPokemon info) {
		ArrayList<Block> allBlocks = Lists.newArrayList(ForgeRegistries.BLOCKS);
		if (info.condition != null && info.condition.cachedNeededNearbyBlocks != null && !info.condition.cachedNeededNearbyBlocks.isEmpty()) {
			allBlocks.removeIf(blocks -> !info.condition.cachedNeededNearbyBlocks.contains(blocks));
		}
		if (info.anticondition != null && info.anticondition.cachedNeededNearbyBlocks != null && !info.anticondition.cachedNeededNearbyBlocks.isEmpty()) {
			allBlocks.removeIf(block -> info.anticondition.cachedNeededNearbyBlocks.contains(block));
		}
		if (info.compositeCondition != null) {
			if (info.compositeCondition.conditions != null) {
				info.compositeCondition.conditions.forEach(condition -> {
					if (condition.cachedNeededNearbyBlocks != null && !condition.cachedNeededNearbyBlocks.isEmpty()) {
						allBlocks.removeIf(block -> !condition.cachedNeededNearbyBlocks.contains(block));
					}
				});
			}
			if (info.compositeCondition.anticonditions != null) {
				info.compositeCondition.anticonditions.forEach(anticondition -> {
					if (anticondition.cachedNeededNearbyBlocks != null && !anticondition.cachedNeededNearbyBlocks.isEmpty()) {
						allBlocks.removeIf(block -> anticondition.cachedNeededNearbyBlocks.contains(block));
					}
				});
			}
		}
		Set<Block> avail = new HashSet<>(allBlocks);
		ArrayList<String> blocksName = new ArrayList<>();
		for (Block block : avail) {
			blocksName.add(TextFormatting.DARK_AQUA + new TranslationTextComponent(block.getTranslationKey()).getString());
		}
		return String.join(TextFormatting.YELLOW + ", ", blocksName);
	}

	public static String getBaseBlocksSpawns(SpawnInfoPokemon info) {
		ArrayList<Block> allBlocks = Lists.newArrayList(ForgeRegistries.BLOCKS);
		if (info.condition != null && info.condition.cachedBaseBlocks != null && !info.condition.cachedBaseBlocks.isEmpty()) {
			allBlocks.removeIf(blocks -> !info.condition.cachedBaseBlocks.contains(blocks));
		}
		if (info.anticondition != null && info.anticondition.cachedBaseBlocks != null && !info.anticondition.cachedBaseBlocks.isEmpty()) {
			allBlocks.removeIf(block -> info.anticondition.cachedBaseBlocks.contains(block));
		}
		if (info.compositeCondition != null) {
			if (info.compositeCondition.conditions != null) {
				info.compositeCondition.conditions.forEach(condition -> {
					if (condition.cachedBaseBlocks != null && !condition.cachedBaseBlocks.isEmpty()) {
						allBlocks.removeIf(block -> !condition.cachedBaseBlocks.contains(block));
					}
				});
			}
			if (info.compositeCondition.anticonditions != null) {
				info.compositeCondition.anticonditions.forEach(anticondition -> {
					if (anticondition.cachedBaseBlocks != null && !anticondition.cachedBaseBlocks.isEmpty()) {
						allBlocks.removeIf(block -> anticondition.cachedBaseBlocks.contains(block));
					}
				});
			}
		}
		Set<Block> avail = new HashSet<>(allBlocks);
		ArrayList<String> blocksName = new ArrayList<>();
		for (Block block : avail) {
			blocksName.add(TextFormatting.DARK_AQUA + new TranslationTextComponent(block.getTranslationKey()).getString());
		}
		return String.join(TextFormatting.YELLOW + ", ", blocksName);
	}

}
