package com.hiroku.tournaments.rewards;

import com.happyzleaf.tournaments.Text;
import com.hiroku.tournaments.api.reward.RewardBase;
import com.hiroku.tournaments.util.PokemonUtils;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.api.pokemon.requirement.impl.SpeciesRequirement;
import com.pixelmonmod.api.registry.RegistryValue;
import com.pixelmonmod.api.requirement.Requirement;
import com.pixelmonmod.pixelmon.Pixelmon;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

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
		EntityPixelmon pokemon = (EntityPixelmon) PixelmonEntityList.createEntityByName(spec.name, (World) player.getWorld());
		spec.apply(pokemon);

		PlayerPartyStorage storage = Pixelmon.storageManager.getParty((EntityPlayerMP) player);//.pokeBallManager.getPlayerStorage((EntityPlayerMP)player).get();
		storage.add(pokemon.getPokemonData());//.addToParty(pokemon);

		player.sendMessage(Text.of(TextColors.DARK_GREEN, "You received a ",
				(pokemon.getPokemonData().isShiny() ? Text.of(TextColors.YELLOW, "shiny ") : ""), TextColors.DARK_AQUA, pokemon.getName(),
				TextColors.DARK_GREEN, " as a reward!"));
	}

	@Override
	public Text getDisplayText() {
		Text.Builder builder = Text.builder().append(Text.of(TextFormatting.GOLD, "Pokémon: "));
		if (spec.shiny != null && spec.shiny)
			builder.append(Text.of(TextFormatting.YELLOW, "Shiny", TextFormatting.GOLD, ", "));
		if (spec.level != null)
			builder.append(Text.of(TextFormatting.DARK_AQUA, "Level ", spec.level, " "));
		return builder.append(Text.of(TextFormatting.DARK_AQUA, spec.name)).build();
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}

	@Override
	public String getSerializationString() {
//		spec.write TODO: write to tag then serialize

		return "pokemon:" + PokemonUtils.serializePokemonSpec(spec);
	}
}
