package com.hiroku.tournaments.rewards;

import com.happyzleaf.tournaments.Text;
import com.hiroku.tournaments.api.reward.RewardBase;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.api.pokemon.requirement.impl.LevelRequirement;
import com.pixelmonmod.api.pokemon.requirement.impl.ShinyRequirement;
import com.pixelmonmod.api.pokemon.requirement.impl.SpeciesRequirement;
import com.pixelmonmod.api.registry.RegistryValue;
import com.pixelmonmod.api.requirement.Requirement;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;

/**
 * Reward which gives the player a Pokémon as described by the PokemonSpec standard.
 *
 * @author Hiroku
 */
public class PokemonReward extends RewardBase {
	/**
	 * The specification of the Pokémon to give
	 */
	public final PokemonSpecification spec;

	public PokemonReward(String arg) throws Exception {
		super(arg);

		this.spec = PokemonSpecificationProxy.create(arg.split(","));
		// TODO: move this to an utility? vvvv
		if (!this.spec.getRequirement(SpeciesRequirement.class).map(Requirement::getValue).flatMap(RegistryValue::getValue).isPresent()) {
			throw new Exception("No Pokemon!");
		}
	}

	@Override
	public void give(PlayerEntity player) {
		Pokemon pokemon = spec.create();

		PlayerPartyStorage storage = StorageProxy.getParty(player.getUniqueID());
		storage.add(pokemon);

		player.sendMessage(Text.of(TextFormatting.DARK_GREEN, "You received a ",
				(pokemon.isShiny() ? Text.of(TextFormatting.YELLOW, "shiny ") : ""), TextFormatting.DARK_AQUA, pokemon.getSpecies().getName(),
				TextFormatting.DARK_GREEN, " as a reward!"), Util.DUMMY_UUID);
	}

	@Override
	public Text getDisplayText() {
		Text.Builder builder = Text.builder().append(Text.of(TextFormatting.GOLD, "Pokémon: "));

		Boolean shinySpec = spec.getRequirement(ShinyRequirement.class).map(Requirement::getValue).orElse(null);
		if (shinySpec != null && shinySpec) {
			builder.append(Text.of(TextFormatting.YELLOW, "Shiny", TextFormatting.GOLD, ", "));
		}

		Integer levelSpec = spec.getRequirement(LevelRequirement.class).map(Requirement::getValue).orElse(null);
		if (levelSpec != null) {
			builder.append(Text.of(TextFormatting.DARK_AQUA, "Level ", levelSpec, " "));
		}

		Species speciesSpec = spec.getRequirement(SpeciesRequirement.class).map(Requirement::getValue).flatMap(RegistryValue::getValue).orElse(PixelmonSpecies.MISSINGNO.getValueUnsafe());
		return builder.append(Text.of(TextFormatting.DARK_AQUA, speciesSpec.getName())).build();
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}

	@Override
	public String getSerializationString() {
		return "pokemon:" + spec.toString();
	}
}
