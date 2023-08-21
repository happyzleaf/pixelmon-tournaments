package fr.pokepixel.pokewiki.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import fr.pokepixel.pokewiki.PokeWiki;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;

import static fr.pokepixel.pokewiki.gui.ChoiceForm.openChoiceFormGUI;
import static fr.pokepixel.pokewiki.gui.DisplayInfo.displayInfoGUI;
import static fr.pokepixel.pokewiki.util.ChatColor.translateAlternateColorCodes;

public class PokeWikiCommand {
	public static LiteralArgumentBuilder<CommandSource> build() {
		return Commands.literal("pokewiki")
				.then(Commands.argument("target", StringArgumentType.greedyString())
						.suggests((context, builder) -> ISuggestionProvider.suggest(
								PokeWiki.translation.getSpeciesNames().stream(),
								builder
						))
						.executes(context -> {
							String name = context.getArgument("target", String.class);
							Species species = PokeWiki.translation.getSpecies(name);
							if (species == null) {
								context.getSource().sendFeedback(new StringTextComponent(translateAlternateColorCodes('&', PokeWiki.lang.other.pokemonNotFound)), false);
								return 0;
							}

							if (species.getForms(false).size() > 1) {
								openChoiceFormGUI(context.getSource().asPlayer(), species);
							} else {
								displayInfoGUI(context.getSource().asPlayer(), PokemonFactory.create(species));
							}

							return 1;
						})
				);
	}
}
