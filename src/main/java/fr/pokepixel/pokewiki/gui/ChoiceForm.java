package fr.pokepixel.pokewiki.gui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.FlagType;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.spawning.SpawnInfo;
import com.pixelmonmod.pixelmon.api.spawning.SpawnSet;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnInfoPokemon;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import com.pixelmonmod.pixelmon.spawning.PixelmonSpawning;
import fr.pokepixel.pokewiki.PokeWiki;
import fr.pokepixel.pokewiki.util.Utils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;

import static fr.pokepixel.pokewiki.gui.DisplayInfo.displayInfoGUI;
import static fr.pokepixel.pokewiki.util.ChatColor.translateAlternateColorCodes;

public class ChoiceForm {
	public static void openChoiceFormGUI(ServerPlayerEntity player, Species species) {
		Button glass = GooeyButton.builder()
				.display(new ItemStack(Items.BLACK_STAINED_GLASS_PANE))
				.build();

		Button previous = LinkedPageButton.builder()
				.linkType(LinkType.Previous)
				.display(new ItemStack(PixelmonItems.trade_holder_left))
				.hideFlags(FlagType.All)
				.title("\u00A7e<----")
				.build();

		Button next = LinkedPageButton.builder()
				.linkType(LinkType.Next)
				.display(new ItemStack(PixelmonItems.trade_holder_right))
				.hideFlags(FlagType.All)
				.title("\u00A7e---->")
				.build();

		PlaceholderButton placeholder = new PlaceholderButton();

		List<Button> buttonList = createButtonFromList(species);

		ChestTemplate.Builder templateBuilder = ChestTemplate.builder(4)
				//.line(LineType.HORIZONTAL,5,0,9,glass)
				.rectangle(0, 2, 2, 5, placeholder)
				.fill(glass);
		//.set(3,3,previous)
		//.set(3,5,next)
		//.build();

		if (buttonList.size() > 10) {
			templateBuilder.set(3, 3, previous);
			templateBuilder.set(3, 5, next);
		}

		LinkedPage.Builder page = LinkedPage.builder()
				.title(translateAlternateColorCodes('&', PokeWiki.lang.general.formGUITitle));

		//Make this offthread
		LinkedPage firstPage = PaginationHelper.createPagesFromPlaceholders(templateBuilder.build(), buttonList, page);
		UIManager.openUIForcefully(player, firstPage);
	}

	public static List<Button> createButtonFromList(Species species) {
		List<Button> list = Lists.newArrayList();
		for (Stats form : species.getForms(false)) {
			Pokemon pokemon = PokemonFactory.create(species);
			pokemon.setForm(form);
			list.add(GooeyButton.builder()
					.display(SpriteItemHelper.getPhoto(pokemon))
					.title("\u00A7a" + Utils.getPokemonName(pokemon))
					.onClick(buttonAction -> displayInfoGUI(buttonAction.getPlayer(), pokemon))
					.build());
		}
		return list;
	}

	public static List<SpawnInfoPokemon> getSpawnInfoList(Pokemon pokemon) {
		List<SpawnInfoPokemon> result = Lists.newArrayList();
		ArrayList<SpawnSet> setinfos = Lists.newArrayList();
		setinfos.addAll(PixelmonSpawning.standard);
		setinfos.addAll(PixelmonSpawning.legendaries);
		for (SpawnSet set : setinfos) {
			for (SpawnInfo info : set.spawnInfos) {
				if (info instanceof SpawnInfoPokemon) {
					SpawnInfoPokemon infoPokemon = (SpawnInfoPokemon) info;
					if (pokemon.getSpecies().equals(infoPokemon.getSpecies()) && infoPokemon.rarity > 0f) {
						if (infoPokemon.getForm().isDefault() || pokemon.getForm().equals(infoPokemon.getForm())) {
							result.add(infoPokemon);
						}
					}
				}
			}
		}
		return result;
	}
}
