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
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import fr.pokepixel.pokewiki.PokeWiki;
import fr.pokepixel.pokewiki.util.Utils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;
import java.util.Objects;

import static fr.pokepixel.pokewiki.gui.DisplayInfo.displayInfoGUI;
import static fr.pokepixel.pokewiki.util.ChatColor.translateAlternateColorCodes;
import static fr.pokepixel.pokewiki.util.SimpleInfo.getInfoEvo;

public class DisplayEvo {
	public static void displayEvoGUI(ServerPlayerEntity player, Pokemon pokemon) {
		Button ejectbutton = GooeyButton.builder()
				.title(translateAlternateColorCodes('&', PokeWiki.lang.evolution.back))
				.display(new ItemStack(Objects.requireNonNull(Utils.getItemById(PokeWiki.config.backButtonId))))
				.hideFlags(FlagType.All)
				.onClick(action -> displayInfoGUI(action.getPlayer(), pokemon))
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

		List<Button> buttonList = getEvoInfos(pokemon);

		ChestTemplate.Builder templateBuilder = ChestTemplate.builder(5)
				.rectangle(0, 0, 2, 9, redGlass)
				.line(LineType.HORIZONTAL, 2, 0, 9, blackGlass)
				.rectangle(3, 0, 2, 9, whiteGlass)
				.rectangle(1, 4, 3, 3, new PlaceholderButton())
				.set(2, 1, ejectbutton);

		if (buttonList.size() > 9) {
			templateBuilder.set(4, 4, previous);
			templateBuilder.set(4, 6, next);
		}

		ChestTemplate template = templateBuilder.build();

		LinkedPage.Builder page = LinkedPage.builder()
				.title(translateAlternateColorCodes('&', PokeWiki.lang.evolution.evoGUITitle));

		//Make this offthread
		LinkedPage firstPage = PaginationHelper.createPagesFromPlaceholders(template, buttonList, page);
		UIManager.openUIForcefully(player, firstPage);
	}

	public static List<Button> getEvoInfos(Pokemon pokemon) {
		List<Button> buttonList = Lists.newArrayList();
		LinkedHashMultimap<Pokemon, List<String>> listevo = getInfoEvo(pokemon);
		listevo.forEach((pokeToEvo, evo) -> {
			ItemStack item = SpriteItemHelper.getPhoto(pokeToEvo);
			buttonList.add(GooeyButton.builder()
					.display(item)
					.title("\u00A7a" + PokeWiki.translation.getTranslation(pokemon))
					.onClick(action -> displayInfoGUI(action.getPlayer(), pokeToEvo))
					.lore(evo)
					.build());
		});
		return buttonList;
	}
}
