package fr.pokepixel.pokewiki.util;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.config.PixelmonConfigProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Element;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.Evolution;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.conditions.*;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.types.InteractEvolution;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.types.LevelingEvolution;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.types.TickingEvolution;
import com.pixelmonmod.pixelmon.api.pokemon.stats.evolution.types.TradeEvolution;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import fr.pokepixel.pokewiki.PokeWiki;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.pokepixel.pokewiki.util.ChatColor.translateAlternateColorCodes;
import static fr.pokepixel.pokewiki.util.ColorUtils.getStatColor;
import static fr.pokepixel.pokewiki.util.ColorUtils.getTypeColor;

public class SimpleInfo {
	private static final List<String> NONE = Collections.singletonList(TextFormatting.DARK_GRAY + "None");

	public static String getType(Pokemon pokemon) {
		if (pokemon.getForm().getTypes().size() > 1) {
			Element type1 = pokemon.getForm().getTypes().get(0);
			Element type2 = pokemon.getForm().getTypes().get(1);
			return getTypeColor(type1) + PokeWiki.translation.getTranslation(type1) + " \u00A7f/ " + getTypeColor(type2) + PokeWiki.translation.getTranslation(type2);
		} else {
			Element type = pokemon.getForm().getTypes().get(0);
			return getTypeColor(type) + PokeWiki.translation.getTranslation(type);
		}
	}

	public static String getEggGroup(Stats baseStats) {
		return translateAlternateColorCodes('&', PokeWiki.lang.general.eggGroups.replaceFirst("%egggroups%", Arrays.toString(baseStats.getEggGroups().stream().map(PokeWiki.translation::getTranslation).toArray())));
	}

	public static String getEggSteps(Pokemon pokemon) {
		Pokemon pokeegg = PokemonFactory.create(PokemonSpecificationProxy.create(pokemon.getSpecies().getName(), "egg"));
		int steps = (pokeegg.getEggCycles() + 1) * PixelmonConfigProxy.getBreeding().getStepsPerEggCycle() - pokeegg.getEggSteps();
		return translateAlternateColorCodes('&', PokeWiki.lang.general.eggSteps.replaceFirst("%eggsteps%", String.valueOf(steps)));
	}

	public static List<String> getDrops(Species species) {
		return Utils.getDrops(species).isEmpty() ? NONE : Utils.getDrops(species);
	}

	public static List<String> getMovesByLevel(Pokemon pokemon) {
		List<String> description = new ArrayList<>();
		pokemon.getForm().getMoves().getPokemonLevelUpMoves().forEach((level, attack) -> {
			List<String> atkName = Lists.newArrayList();
			attack.forEach(attackBase -> atkName.add(PokeWiki.translation.getTranslation(attackBase)));
			description.add("\u00A76" + level + " - " + String.join(",", atkName));
		});
		return description;
	}

//	public static List<String> getBreeding(BaseStats stats) {
//		EnumMap<EnumType, HashMap<Block, Integer>> typeBlockList = ReflectionHelper.getBreedingTypeList().orElse(null);
//		if (typeBlockList == null) {
//			return NONE;
//		}
//
//		List<String> description = new ArrayList<>();
//		for (EnumType type : stats.getTypeList()) {
//			HashMap<Block, Integer> blocks = typeBlockList.get(type);
//			if (blocks == null || blocks.isEmpty()) {
//				continue;
//			}
//
//			description.add("");
//
//			String color = getTypeColor(type).toString();
//			description.add(TextFormatting.BOLD.toString() + TextFormatting.UNDERLINE + color + PokeWiki.translation.getTranslation(type));
//			List<String> one = Lists.newArrayList();
//			List<String> two = Lists.newArrayList();
//			List<String> three = Lists.newArrayList();
//			for (Map.Entry<Block, Integer> entry : blocks.entrySet()) {
//				switch (entry.getValue()) {
//					case 1:
//						one.add(PokeWiki.translation.getTranslation(entry.getKey()));
//						break;
//					case 2:
//						two.add(PokeWiki.translation.getTranslation(entry.getKey()));
//						break;
//					case 3:
//						three.add(PokeWiki.translation.getTranslation(entry.getKey()));
//						break;
//				}
//			}
//			if (!one.isEmpty()) {
//				description.add(color + "1 - " + String.join(", ", one));
//			}
//			if (!two.isEmpty()) {
//				description.add(color + "2 - " + String.join(", ", two));
//			}
//			if (!three.isEmpty()) {
//				description.add(color + "3 - " + String.join(", ", three));
//			}
//            /*for (Map.Entry<Block, Integer> entry : blocks.entrySet()) {
//                description.add(color + PokeWiki.translation.getTranslation(entry.getKey()) + " " + entry.getValue());
//            }*/
//		}
//		return description;
//	}

	public static LinkedHashMultimap<Pokemon, List<String>> getInfoEvo(Pokemon pokemon) {
		LinkedHashMultimap<Pokemon, List<String>> pokeevo = LinkedHashMultimap.create();
		if (pokemon.getForm().getEvolutions().size() > 0) {
			for (Evolution evolution : pokemon.getForm().getEvolutions()) {
				if (evolution == null) {
					continue; // Possible, though very unlikely.
				}

				List<String> info = Lists.newArrayList();
				Pokemon evolutionPokemon = PokemonFactory.create(evolution.to);

				StringBuilder baseMsg = new StringBuilder("\u00A7e" + Utils.getPokemonName(evolutionPokemon) + ": " + translateAlternateColorCodes('&', PokeWiki.lang.evolution.levelingUp));
				baseMsg.append("\u00A7r");
				if (evolution instanceof LevelingEvolution) {
					LevelingEvolution levelingEvolution = (LevelingEvolution) evolution;
					if (levelingEvolution.level != null && levelingEvolution.level > 1) {
						baseMsg.append(translateAlternateColorCodes('&', PokeWiki.lang.evolution.levelNumber.replaceFirst("%level%", String.valueOf(levelingEvolution.level))));
					}
				} else if (evolution instanceof InteractEvolution) {
					if (((InteractEvolution) evolution).item != null) {
						baseMsg = new StringBuilder("\u00A7e" + Utils.getPokemonName(evolutionPokemon) + ": " + translateAlternateColorCodes('&', PokeWiki.lang.evolution.exposedToItem.replaceFirst("%item%", ((InteractEvolution) evolution).item.getItemStack().getDisplayName().getString())));
					}
				} else if (evolution instanceof TradeEvolution) {
					TradeEvolution tradeEvo = (TradeEvolution) evolution;
					if (tradeEvo.with != null) {
						baseMsg = new StringBuilder("\u00A7e" + Utils.getPokemonName(evolutionPokemon) + ": " + translateAlternateColorCodes('&', PokeWiki.lang.evolution.tradedWith.replaceFirst("%pokemon%", Utils.getPokemonName(tradeEvo.with))));
					} else {
						baseMsg = new StringBuilder("\u00A7e" + Utils.getPokemonName(evolutionPokemon) + ": " + translateAlternateColorCodes('&', PokeWiki.lang.evolution.traded));
					}
				} else if (evolution instanceof TickingEvolution) {
					baseMsg = new StringBuilder("\u00A7e" + Utils.getPokemonName(evolutionPokemon) + ": ");
				}
				info.add(baseMsg.toString());

				if (evolution.conditions != null && !evolution.conditions.isEmpty()) {
					TextFormatting headingColour = TextFormatting.GOLD;
					TextFormatting valueColour = TextFormatting.DARK_AQUA;
					baseMsg = new StringBuilder(TextFormatting.GOLD + "    " + TextFormatting.UNDERLINE + "Conditions:");
					baseMsg.append("\u00A7r");
					for (EvoCondition condition : evolution.conditions) {
						if (condition instanceof BiomeCondition) {
							BiomeCondition biomeCondition = (BiomeCondition) condition;
							StringBuilder biomes = new StringBuilder(headingColour + "Biomes: " + valueColour);
							for (int i = 0; i < biomeCondition.biomes.size(); i++) {
								Biome b = ForgeRegistries.BIOMES.getValue(new ResourceLocation(biomeCondition.biomes.get(i)));
								String biomeName = b.getCategory().getName();
								if (i == 0) {
									biomes.append(biomeName);
								} else {
									biomes.append(headingColour).append(", ").append(valueColour).append(biomeName);
								}
							}
							info.add("      " + biomes);
						} else if (condition instanceof ChanceCondition) {
							ChanceCondition chanceCondition = (ChanceCondition) condition;
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.chanceCondition.replaceFirst("%chance%", String.valueOf(chanceCondition.chance * 100)));
							info.add("      " + valueColour + conditiontxt);
						} else if (condition instanceof EvoRockCondition) {
							EvoRockCondition evoRockCond = (EvoRockCondition) condition;
							String evorockcondition = translateAlternateColorCodes('&', PokeWiki.lang.evolution.evoRockCondition.replaceFirst("%range%", String.valueOf(Math.sqrt(evoRockCond.maxRangeSquared))).replaceFirst("%rockname%", PokeWiki.translation.getTranslation(evoRockCond.evolutionRock)));
							info.add("      " + evorockcondition);
						} else if (condition instanceof FriendshipCondition) {
							info.add("      " + translateAlternateColorCodes('&', PokeWiki.lang.evolution.friendshipCondition.replaceFirst("%friendship%", String.valueOf(((FriendshipCondition) condition).friendship))));
						} else if (condition instanceof GenderCondition) {
							GenderCondition genderCondition = (GenderCondition) condition;
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.genderCondition.replaceFirst("%gender%", PokeWiki.translation.getTranslation(genderCondition.genders.get(0))));
							StringBuilder genders = new StringBuilder(conditiontxt);
							for (int i = 1; i < genderCondition.genders.size(); i++) {
								genders.append(headingColour).append(", ").append(valueColour).append(PokeWiki.translation.getTranslation(genderCondition.genders.get(i)));
							}
							info.add("      " + genders);
						} else if (condition instanceof HeldItemCondition) {
							HeldItemCondition heldItemCondition = (HeldItemCondition) condition;
							ItemStack stack = heldItemCondition.item.getItemStack();
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.heldItemCondition.replaceFirst("%helditem%", (stack == null ? heldItemCondition.item.itemID : stack.getDisplayName().getString())));
							info.add("      " + conditiontxt);
						} else if (condition instanceof HighAltitudeCondition) {
							HighAltitudeCondition altitudeCondition = (HighAltitudeCondition) condition;
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.aboveAltitudeCondition.replaceFirst("%altitude%", String.valueOf((int) altitudeCondition.minAltitude)));
							info.add("      " + conditiontxt);
						} else if (condition instanceof LevelCondition) {
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.levelCondition.replaceFirst("%level%", String.valueOf((((LevelCondition) condition).level))));
							info.add("    " + conditiontxt);
						} else if (condition instanceof MoveCondition) {
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.moveCondition.replaceFirst("%move%", Utils.getAttackName(((MoveCondition) condition).attackName)));
							info.add("      " + conditiontxt);
						} else if (condition instanceof MoveTypeCondition) {
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.moveTypeCondition.replaceFirst("%movetype%", PokeWiki.translation.getTranslation(((MoveTypeCondition) condition).type)));
							info.add("      " + conditiontxt);
						} else if (condition instanceof PartyCondition) {
							ArrayList<PokemonSpecification> withPokemon = new ArrayList<>();
							ArrayList<Element> withTypes = new ArrayList<>();
							ArrayList<String> withForms = new ArrayList<>();
							PartyCondition partyCond = (PartyCondition) condition;
							if (partyCond.withPokemon != null) {
								withPokemon = partyCond.withPokemon;
							}
							if (partyCond.withTypes != null) {
								withTypes = partyCond.withTypes;
							}
							if (partyCond.withForms != null) {
								withForms = partyCond.withForms;
							}
							if (!withPokemon.isEmpty()) {
								String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.withPokemonCondition.replaceFirst("%pokemonlist%", Utils.getPokemonName(withPokemon.get(0))));
								StringBuilder pokemonWith = new StringBuilder("      " + conditiontxt);
								for (int i = 1; i < withPokemon.size(); i++) {
									pokemonWith.append(headingColour).append(", ").append(valueColour).append(Utils.getPokemonName(withPokemon.get(i)));
								}
								info.add(pokemonWith.toString());
							}
							if (!withTypes.isEmpty()) {
								String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.withTypeCondition.replaceFirst("%typelist%", PokeWiki.translation.getTranslation(withTypes.get(0))));
								StringBuilder typesWith = new StringBuilder("      " + conditiontxt);
								for (int i = 1; i < withTypes.size(); i++) {
									typesWith.append(headingColour).append(", ").append(valueColour).append(PokeWiki.translation.getTranslation(withTypes.get(i)));
								}
								info.add(typesWith.toString());
							}
							if (!withForms.isEmpty()) {
								String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.withFormCondition.replaceFirst("%formlist%", withForms.get(0)));
								StringBuilder formsWith = new StringBuilder("      " + conditiontxt);
								for (int i = 1; i < withForms.size(); i++) {
									formsWith.append(headingColour).append(", ").append(valueColour).append(withForms.get(i));
								}
								info.add(formsWith.toString());
							}
						} else if (condition instanceof StatRatioCondition) {
							StatRatioCondition statCond = (StatRatioCondition) condition;
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.statRatioCondition.replaceFirst("%ratio%", String.valueOf(statCond.ratio)).replaceFirst("%stat1%", PokeWiki.translation.getTranslation(statCond.stat1)).replaceFirst("%stat2%", PokeWiki.translation.getTranslation(statCond.stat2)));
							info.add("      " + conditiontxt);
						} else if (condition instanceof TimeCondition) {
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.timeCondition.replaceFirst("%time%", PokeWiki.translation.getTranslation((((TimeCondition) condition).time))));
							info.add("      " + conditiontxt);
						} else if (condition instanceof WeatherCondition) {
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.weatherCondition.replaceFirst("%weather%", String.valueOf((((WeatherCondition) condition).weather))));
							info.add("    " + conditiontxt);
						} else if (condition instanceof EvoScrollCondition) {
							EvoScrollCondition evoScrollCondition = (EvoScrollCondition) condition;
							long value = Math.round(Math.sqrt(evoScrollCondition.maxRangeSquared));
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.scrollCondition.replaceFirst("%scroll%", evoScrollCondition.evolutionScroll.getName()).replaceFirst("%range%", String.valueOf(value)));
							info.add("      " + conditiontxt);
						} else if (condition instanceof BattleCriticalCondition) {
							BattleCriticalCondition battleCriticalCondition = (BattleCriticalCondition) condition;
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.battleCriticalCondition.replaceFirst("%crit%", String.valueOf(battleCriticalCondition.critical)));
							info.add("      " + conditiontxt);
						} else if (condition instanceof AbsenceOfHealthCondition) {
							AbsenceOfHealthCondition absenceOfHealthCondition = (AbsenceOfHealthCondition) condition;
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.absenceOfHealCondition);
							info.add("      " + conditiontxt);
						} else if (condition instanceof StatusPersistCondition) {
							StatusPersistCondition statusPersistCondition = (StatusPersistCondition) condition;
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.statusPersistCondition);
							info.add("      " + conditiontxt);
						} else if (condition instanceof WithinStructureCondition) {
							WithinStructureCondition withinStructureCondition = (WithinStructureCondition) condition;
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.withinStructureCondition);
							info.add("      " + conditiontxt);
						} else if (condition instanceof NatureCondition) {
							NatureCondition natureCondition = (NatureCondition) condition;
							List<String> natureName = Lists.newArrayList();
							natureCondition.getNatures().forEach(enumNature -> natureName.add(PokeWiki.translation.getTranslation(enumNature)));
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.natureCondition.replaceFirst("%natures%", String.join(", ", natureName)));
							info.add("      " + conditiontxt);
						} else if (condition instanceof RecoilCondition) {
							String conditiontxt = translateAlternateColorCodes('&', PokeWiki.lang.evolution.recoilCondition.replaceFirst("%recoil%", String.valueOf(((RecoilCondition) condition).getRecoil())));
							info.add("      " + conditiontxt);
						}
					}
				}
				pokeevo.put(evolutionPokemon, info);
			}

		}
		return pokeevo;
	}

	public static List<String> getAbilityForPoke(Pokemon pokemon) {
		List<String> abilityList = Lists.newArrayList();
		for (Ability ability : pokemon.getForm().getAbilities().getAll()) {
			pokemon.setAbility(ability);
			if (pokemon.hasHiddenAbility()) {
				abilityList.add(translateAlternateColorCodes('&', PokeWiki.lang.general.hiddenAbility.replaceFirst("%hiddenability%", PokeWiki.translation.getTranslation(ability))));
			} else {
				abilityList.add(translateAlternateColorCodes('&', PokeWiki.lang.general.normalAbility.replaceFirst("%ability%", PokeWiki.translation.getTranslation(ability))));
			}
		}
		return abilityList;
	}

	public static List<String> getTypeEffectiveness(Pokemon pokemon) {
		List<String> description = new ArrayList<>();
		List<Element> typeList = pokemon.getForm().getTypes();
		Element.getAllTypes().forEach(type -> {
			if (Element.getTotalEffectiveness(typeList, type) != 1.0f) {
				description.add(ColorUtils.getTypeColor(type) + PokeWiki.translation.getTranslation(type) + " " + prepareEffectiveness(Element.getTotalEffectiveness(typeList, type)) + "x");
			}
		});
		return description;
	}

	public static String prepareEffectiveness(float effectiveness) {
		if (approximatelyEqual(effectiveness, 0f, 0.05f)) return "0";
		if (approximatelyEqual(effectiveness, 0.25f, 0.05f)) return "1/4";
		if (approximatelyEqual(effectiveness, 0.5f, 0.05f)) return "1/2";
		if (approximatelyEqual(effectiveness, 2f, 0.05f)) return "2";
		if (approximatelyEqual(effectiveness, 4f, 0.05f)) return "4";
		return "?";
	}

	public static boolean approximatelyEqual(float desiredValue, float actualValue, float tolerancePercentage) {
		float diff = Math.abs(desiredValue - actualValue);
		float tolerance = tolerancePercentage / 100 * desiredValue;
		return diff < tolerance;
	}

	public static List<String> getBaseStats(Stats stats) {
		List<String> description = new ArrayList<>();
		for (BattleStatsType type : BattleStatsType.getEVIVStatValues()) {
			description.add(getStatColor(type) + PokeWiki.translation.getTranslation(type) + ": " + stats.getBattleStats().getStat(type));
		}
		return description;
	}

	public static List<String> getEVYield(Stats stats) {
		List<String> description = new ArrayList<>();
		for (BattleStatsType type : BattleStatsType.getEVIVStatValues()) {
			description.add(getStatColor(type) + PokeWiki.translation.getTranslation(type) + ": " + stats.getEVYields().getYield(type));
		}
		return description;
	}

	public static List<String> getTutorMoves(Stats stats) {
		List<String> description = new ArrayList<>();
		List<String> atkname = new ArrayList<>();
		stats.getMoves().getTutorMoves().stream().sorted(Comparator.comparing(PokeWiki.translation::getTranslation)).forEachOrdered(attack -> {
			atkname.add(PokeWiki.translation.getTranslation(attack));
		});
		description.add(TextFormatting.GOLD + String.join(", ", atkname));
		return description;
	}

	public static List<String> getTMHMMoves(Stats stats) {
		List<String> description = new ArrayList<>();
		Stream<ImmutableAttack> tmList = stats.getMoves().getTMMoves().stream().sorted(Comparator.comparing(ImmutableAttack::getAttackName));
		Stream<ImmutableAttack> hmList = stats.getMoves().getHMMoves().stream().sorted(Comparator.comparing(ImmutableAttack::getAttackName));
		List<String> atkname = new ArrayList<>();
		tmList.forEachOrdered(iTechnicalMove -> atkname.add(PokeWiki.translation.getTranslation(iTechnicalMove)));
		hmList.forEachOrdered(attackBase -> atkname.add(PokeWiki.translation.getTranslation(attackBase)));
		description.add(TextFormatting.GOLD + String.join(", ", atkname));
		return description;
	}

	public static String getTRMoves(Stats stats) {
		return TextFormatting.GOLD + stats.getMoves().getTRMoves().stream()
				.map(tr -> PokeWiki.translation.getTranslation(tr.getAttack()))
				.sorted()
				.collect(Collectors.joining(", "));
	}

	public static List<String> getBreedingMoves(Stats stats) {
		List<String> description = new ArrayList<>();
		List<String> atkname = new ArrayList<>();
		stats.getMoves().getEggMoves().stream().sorted(Comparator.comparing(PokeWiki.translation::getTranslation)).forEachOrdered(attack -> {
			atkname.add(PokeWiki.translation.getTranslation(attack));
		});
		description.add(TextFormatting.GOLD + String.join(", ", atkname));
		return description;
	}
}
