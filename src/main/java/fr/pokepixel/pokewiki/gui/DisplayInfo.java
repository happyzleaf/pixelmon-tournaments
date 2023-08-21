package fr.pokepixel.pokewiki.gui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.FlagType;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.LineType;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.gender.Gender;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import fr.pokepixel.pokewiki.PokeWiki;
import fr.pokepixel.pokewiki.util.Utils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;

import static fr.pokepixel.pokewiki.gui.ChoiceForm.getSpawnInfoList;
import static fr.pokepixel.pokewiki.gui.ChoiceForm.openChoiceFormGUI;
import static fr.pokepixel.pokewiki.gui.DisplayEvo.displayEvoGUI;
import static fr.pokepixel.pokewiki.gui.DisplaySpawn.displaySpawnGUI;
import static fr.pokepixel.pokewiki.util.ChatColor.translateAlternateColorCodes;
import static fr.pokepixel.pokewiki.util.SimpleInfo.*;
import static fr.pokepixel.pokewiki.util.Utils.canSpawnCustom;

public class DisplayInfo {
	public static void displayInfoGUI(ServerPlayerEntity player, Pokemon pokemon) {
		boolean hasForm = pokemon.getSpecies().getForms(false).size() > 1;
		List<String> spriteForm = Lists.newArrayList();
		if (hasForm) {
			spriteForm.add(translateAlternateColorCodes('&', PokeWiki.lang.general.backToFormSelection));
		}

		Button pokesprite = GooeyButton.builder()
				.title("\u00A76" + Utils.getPokemonName(pokemon))
				.display(SpriteItemHelper.getPhoto(pokemon))
				.lore(spriteForm)
				.onClick(buttonAction -> {
					if (hasForm) {
						openChoiceFormGUI(buttonAction.getPlayer(), pokemon.getSpecies());
					}
				})
				.build();

		Button redGlass = GooeyButton.builder()
				.title("")
				.display(new ItemStack(Items.RED_STAINED_GLASS_PANE))
				.build();

		Button blackGlass = GooeyButton.builder()
				.title("")
				.display(new ItemStack(Items.BLACK_STAINED_GLASS_PANE))
				.build();

		Button whiteGlass = GooeyButton.builder()
				.title("")
				.display(new ItemStack(Items.WHITE_STAINED_GLASS_PANE))
				.build();

		Button type = GooeyButton.builder()
				.hideFlags(FlagType.All)
				.display(new ItemStack(PixelmonItems.marsh_badge))
				.title(translateAlternateColorCodes('&', PokeWiki.lang.general.pokemonType))
				.lore(Lists.newArrayList(getType(pokemon), getEggGroup(pokemon.getForm()), getEggSteps(pokemon)))
				.build();

		long baserate = Math.round(pokemon.getForm().getCatchRate() / 255.0D * 100.0D);
		double femalepercent = 100 - pokemon.getForm().getMalePercentage();
		//translateAlternateColorCodes('&',lang.get("backtoformselection").getString())
		List<String> ballLore = Lists.newArrayList();
		ballLore.add(translateAlternateColorCodes('&', PokeWiki.lang.general.baseRate.replaceFirst("%baserate%", String.valueOf(baserate))));
		if (pokemon.getGender().equals(Gender.NONE)) {
			ballLore.add(translateAlternateColorCodes('&', PokeWiki.lang.general.genderless));
		} else {
			ballLore.add(translateAlternateColorCodes('&', PokeWiki.lang.general.malePercent.replaceFirst("%malepercent", String.valueOf(pokemon.getForm().getMalePercentage()))));
			ballLore.add(translateAlternateColorCodes('&', PokeWiki.lang.general.femalePercent.replaceFirst("%femalepercent", String.valueOf(femalepercent))));
		}
		Button catchrate = GooeyButton.builder()
				.hideFlags(FlagType.All)
				.display(new ItemStack(PixelmonItems.poke_ball))
				.title(translateAlternateColorCodes('&', PokeWiki.lang.general.catchRate))
				.lore(ballLore)
				.build();

		boolean hasSpawn = !getSpawnInfoList(pokemon).isEmpty() || canSpawnCustom(pokemon);
		List<String> loreSpawn = Lists.newArrayList();
		if (hasSpawn) {
			loreSpawn.add(translateAlternateColorCodes('&', PokeWiki.lang.general.loreSpawnInfo1));
		} else {
			loreSpawn.add(translateAlternateColorCodes('&', PokeWiki.lang.general.loreSpawnInfo2));
		}

		Button spawninfo = GooeyButton.builder()
				.display(new ItemStack(Blocks.OAK_SAPLING))
				.title(translateAlternateColorCodes('&', PokeWiki.lang.general.spawnInfo))
				.lore(loreSpawn)
				.onClick(action -> {
					if (hasSpawn) {
						displaySpawnGUI(action.getPlayer(), pokemon);
					}
				})
				.build();

		String evotitle = pokemon.getForm().getEvolutions().size() > 0 ? translateAlternateColorCodes('&', PokeWiki.lang.general.evo) : translateAlternateColorCodes('&', PokeWiki.lang.general.noEvo);

		Button evoinfo = GooeyButton.builder()
				.display(new ItemStack(PixelmonItems.up_grade))
				.hideFlags(FlagType.All)
				.title(evotitle)
				.onClick(buttonAction -> {
					if (pokemon.getForm().getEvolutions().size() > 0) {
						displayEvoGUI(buttonAction.getPlayer(), pokemon);
					}
				})
				.build();

		Button abilityinfo = GooeyButton.builder()
				.hideFlags(FlagType.All)
				.display(new ItemStack(PixelmonItems.ability_capsule))
				.title(translateAlternateColorCodes('&', PokeWiki.lang.general.ability))
				.lore(Lists.newArrayList(getAbilityForPoke(pokemon)))
				.build();

//		Button breedinfo = GooeyButton.builder()
//				.hideFlags(FlagType.All)
//				.display(new ItemStack(PixelmonItems.ranchUpgrade))
//				.title(translateAlternateColorCodes('&', langgeneral.get("breed").getString()))
//				.lore(Lists.newArrayList(getBreeding(pokemon.getBaseStats())))
//				.build();

		Button dropinfo = GooeyButton.builder()
				.display(new ItemStack(Items.DIAMOND))
				.title(translateAlternateColorCodes('&', PokeWiki.lang.general.drops))
				.lore(Lists.newArrayList(getDrops(pokemon.getSpecies())))
				.build();

		Button movebylevelinfo = GooeyButton.builder()
				.display(new ItemStack(PixelmonItems.tm_gen1))
				.hideFlags(FlagType.All)
				.title(translateAlternateColorCodes('&', PokeWiki.lang.general.movesByLevel))
				.lore(Lists.newArrayList(getMovesByLevel(pokemon)))
				.build();

		Button typeeffectiveness = GooeyButton.builder()
				.display(new ItemStack(PixelmonItems.rumble_badge))
				.hideFlags(FlagType.All)
				.title(translateAlternateColorCodes('&', PokeWiki.lang.general.typeEffectiveness))
				.lore(Lists.newArrayList(getTypeEffectiveness(pokemon)))
				.build();

		Button basestats = GooeyButton.builder()
				.display(new ItemStack(PixelmonItems.weakness_policy))
				.hideFlags(FlagType.All)
				.title(translateAlternateColorCodes('&', PokeWiki.lang.general.baseStats))
				.lore(Lists.newArrayList(getBaseStats(pokemon.getForm())))
				.build();

		Button evyield = GooeyButton.builder()
				.display(new ItemStack(PixelmonItems.power_weight))
				.hideFlags(FlagType.All)
				.title(translateAlternateColorCodes('&', PokeWiki.lang.general.evYield))
				.lore(Lists.newArrayList(getEVYield(pokemon.getForm())))
				.build();

		Button movebytutor = GooeyButton.builder()
				.display(new ItemStack(PixelmonItems.tm_gen1))
				.hideFlags(FlagType.All)
				.title(translateAlternateColorCodes('&', PokeWiki.lang.general.tutorMoves))
				.lore(Lists.newArrayList(getTutorMoves(pokemon.getForm())))
				.build();

		Button tmhmmoves = GooeyButton.builder()
				.display(new ItemStack(PixelmonItems.tm_gen1))
				.hideFlags(FlagType.All)
				.title(translateAlternateColorCodes('&', PokeWiki.lang.general.tmhmMoves))
				.lore(Lists.newArrayList(getTMHMMoves(pokemon.getForm())))
				.build();

		Button trmoves = GooeyButton.builder()
				.display(new ItemStack(PixelmonItems.tm_gen1))
				.hideFlags(FlagType.All)
				.title(translateAlternateColorCodes('&', PokeWiki.lang.general.trMoves))
				.lore(Lists.newArrayList(getTRMoves(pokemon.getForm())))
				.build();

		Button eggmoves = GooeyButton.builder()
				.display(new ItemStack(PixelmonItems.tm_gen1))
				.hideFlags(FlagType.All)
				.title(translateAlternateColorCodes('&', PokeWiki.lang.general.eggMoves))
				.lore(Lists.newArrayList(getBreedingMoves(pokemon.getForm())))
				.build();


		ChestTemplate template = ChestTemplate.builder(5)
				.rectangle(0, 0, 2, 9, redGlass)
				.line(LineType.HORIZONTAL, 2, 0, 9, blackGlass)
				.rectangle(3, 0, 2, 9, whiteGlass)
				.set(1, 3, type)
				.set(1, 4, evoinfo)
				.set(1, 5, spawninfo)
				.set(1, 6, catchrate)
				.set(1, 7, abilityinfo)
				.set(2, 1, pokesprite)
//				.set(2, 3, breedinfo)
				.set(2, 4, dropinfo)
				.set(2, 5, evyield)
				.set(2, 6, typeeffectiveness)
				.set(2, 7, basestats)
				.set(3, 3, movebylevelinfo)
				.set(3, 4, movebytutor)
				.set(3, 5, tmhmmoves)
				.set(3, 6, trmoves)
				.set(3, 7, eggmoves)
				.build();

		LinkedPage page = LinkedPage.builder()
				.template(template)
				.title(translateAlternateColorCodes('&', PokeWiki.lang.general.mainGUITitle))
				.build();
		UIManager.openUIForcefully(player, page);
	}
}
