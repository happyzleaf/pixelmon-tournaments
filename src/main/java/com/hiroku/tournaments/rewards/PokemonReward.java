package com.hiroku.tournaments.rewards;

import com.hiroku.tournaments.api.reward.RewardBase;
import com.hiroku.tournaments.util.PokemonUtils;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.config.PixelmonEntityList;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Reward which gives the player a Pokémon as described by the PokemonSpec standard.
 *
 * @author Hiroku
 */
public class PokemonReward extends RewardBase {
	/**
	 * The specification of the Pokémon to give
	 */
	public final PokemonSpec spec;

	public PokemonReward(String arg) throws Exception {
		super(arg);

		this.spec = PokemonSpec.from(arg.split(","));
		if (spec.name == null)
			throw new Exception("No Pokemon!");
	}

	@Override
	public void give(Player player) {
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
		Text.Builder builder = Text.builder().append(Text.of(TextColors.GOLD, "Pokémon: "));
		if (spec.shiny != null && spec.shiny)
			builder.append(Text.of(TextColors.YELLOW, "Shiny", TextColors.GOLD, ", "));
		if (spec.level != null)
			builder.append(Text.of(TextColors.DARK_AQUA, "Level ", spec.level, " "));
		return builder.append(Text.of(TextColors.DARK_AQUA, spec.name)).build();
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}

	@Override
	public String getSerializationString() {
		return "pokemon:" + PokemonUtils.serializePokemonSpec(spec);
	}
}
