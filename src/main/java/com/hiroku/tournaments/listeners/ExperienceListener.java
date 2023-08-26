package com.hiroku.tournaments.listeners;

import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.archetypes.pokemon.PokemonMatch;
import com.hiroku.tournaments.enums.EnumTournamentState;
import com.hiroku.tournaments.rules.general.SetParty;
import com.hiroku.tournaments.util.PokemonUtils;
import com.pixelmonmod.pixelmon.api.events.ExperienceGainEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Listener for experience earning. This is to prevent experience gain from tournament battles.
 *
 * @author Hiroku
 */
public class ExperienceListener {
	@SubscribeEvent
	public void onExperienceGain(ExperienceGainEvent event) {
		if (event.pokemon == null) {
			return;
		}

		Pokemon pokemon = event.pokemon.getPokemon();
		PlayerEntity player = pokemon.getOwnerPlayer();
		if (Tournament.instance() != null && Tournament.instance().state == EnumTournamentState.ACTIVE && player != null) {
			BattleController bc = BattleRegistry.getBattle(player);
			if (bc != null && PokemonMatch.getMatch(bc) != null) {
				event.setExperience(0);
			}
		}

		if (PokemonUtils.isRental(pokemon) || pokemon.getPersistentData().contains(SetParty.OLD_LEVEL_KEY)) {
			event.setExperience(0);
		}
	}
}
