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
import ca.landonjw.gooeylibs2.api.template.LineType;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.google.common.collect.Lists;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.api.pokemon.requirement.impl.FormRequirement;
import com.pixelmonmod.api.pokemon.requirement.impl.SpeciesRequirement;
import com.pixelmonmod.api.registry.RegistryValue;
import com.pixelmonmod.api.requirement.Requirement;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnInfoPokemon;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import fr.pokepixel.pokewiki.PokeWiki;
import fr.pokepixel.pokewiki.util.Utils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;
import java.util.Objects;

import static fr.pokepixel.pokewiki.PokeWiki.customSpawnPokemonInfoListInfo;
import static fr.pokepixel.pokewiki.util.ChatColor.translateAlternateColorCodes;
import static fr.pokepixel.pokewiki.gui.ChoiceForm.getSpawnInfoList;
import static fr.pokepixel.pokewiki.gui.DisplayInfo.displayInfoGUI;
import static fr.pokepixel.pokewiki.util.SpawnDetails.createPokeDetails;

public class DisplaySpawn {
	public static void displaySpawnGUI(ServerPlayerEntity player, Pokemon pokemon) {
		ItemStack backButton = new ItemStack(Objects.requireNonNull(Utils.getItemById(PokeWiki.config.backButtonId)));

		Button ejectbutton = GooeyButton.builder()
				.title(translateAlternateColorCodes('&', PokeWiki.lang.spawn.back))
				.display(backButton)
				.hideFlags(FlagType.All)
				.onClick(buttonAction -> {
					displayInfoGUI(buttonAction.getPlayer(), pokemon);
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

		List<Button> buttonList = getSpawnInfos(pokemon);

		ChestTemplate.Builder templateBuilder = ChestTemplate.builder(5)
				.rectangle(0, 0, 2, 9, redGlass)
				.line(LineType.HORIZONTAL, 2, 0, 9, blackGlass)
				.rectangle(3, 0, 2, 9, whiteGlass)
				.rectangle(1, 4, 3, 3, placeholder)
				.set(2, 1, ejectbutton);
		//.set(4,4,previous)
		//.set(4,6,next)
		//.build();

		if (buttonList.size() > 9) {
			templateBuilder.set(4, 4, previous);
			templateBuilder.set(4, 6, next);
		}

		ChestTemplate template = templateBuilder.build();

		LinkedPage.Builder page = LinkedPage.builder()
				.title(translateAlternateColorCodes('&', PokeWiki.lang.spawn.spawnGUITitle));

		//Make this offthread
		LinkedPage firstPage = PaginationHelper.createPagesFromPlaceholders(template, buttonList, page);
		UIManager.openUIForcefully(player, firstPage);
	}

	public static List<Button> getSpawnInfos(Pokemon pokemon) {
		List<Button> buttonList = Lists.newArrayList();
		List<SpawnInfoPokemon> spawnInfoPokemonList = Lists.newArrayList(getSpawnInfoList(pokemon));
		spawnInfoPokemonList.forEach(spawnInfoPokemon -> {
			Pokemon pokespawn = spawnInfoPokemon.getPokemonSpec().create();
			if (spawnInfoPokemon.rarity > 0f) {
				buttonList.add(GooeyButton.builder()
						.display(SpriteItemHelper.getPhoto(pokespawn))
						.title("\u00A76" + Utils.getPokemonName(pokemon))
						.lore(createPokeDetails(spawnInfoPokemon))
						.build());
			}
		});
		if (customSpawnPokemonInfoListInfo.containsKey(pokemon.getSpecies().getName())) {
			customSpawnPokemonInfoListInfo.get(pokemon.getSpecies().getName()).forEach(customSpawnPokemonInfo -> {
				PokemonSpecification spec = PokemonSpecificationProxy.create(customSpawnPokemonInfo.getSpec());
				spec.apply(pokemon);
//				TODO? (happyz)
//				if (spec.form != null) {
//					if (spec.form <= 0 && pokemon.getForm() <= 0) {
//						pokemon.setForm(spec.form);
//					}
//				} else {
//					spec.form = -1;
//				}
				Requirement<Pokemon, PixelmonEntity, RegistryValue<Species>> speciesReq = spec.getRequirement(SpeciesRequirement.class).orElse(null);
				Requirement<Pokemon, PixelmonEntity, String> formReq = spec.getRequirement(FormRequirement.class).orElse(null);
				if (speciesReq != null && speciesReq.getValue() != null && formReq != null && formReq.getValue() != null) {
					Species species = speciesReq.getValue().getValue().get();
					String form = formReq.getValue();
					if (pokemon.getSpecies().equals(species) && pokemon.getForm().getName().equalsIgnoreCase(form)) {
						buttonList.add(GooeyButton.builder()
								.display(SpriteItemHelper.getPhoto(pokemon))
								.title("\u00A76" + Utils.getPokemonName(pokemon))
								.lore(Lists.newArrayList(customSpawnPokemonInfo.getInfo()))
								.build());
					}
				}
			});
		}
		return buttonList;
	}
}
