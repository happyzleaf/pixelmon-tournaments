package com.hiroku.tournaments.listeners;

import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.requirements.RentalRequirement;
import com.hiroku.tournaments.enums.TournamentStates;
import com.hiroku.tournaments.rules.general.SetParty;
import com.pixelmonmod.pixelmon.api.enums.ExperienceGainType;
import com.pixelmonmod.pixelmon.api.events.ExperienceGainEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
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
		if (RentalRequirement.is(pokemon) || pokemon.getPersistentData().contains(SetParty.OLD_LEVEL_KEY)) {
			event.setExperience(0);
		}

        PlayerEntity player = pokemon.getOwnerPlayer();
        if (player != null && Tournament.instance() != null
				&& Tournament.instance().state == TournamentStates.ACTIVE
				&& event.getType() != ExperienceGainType.BATTLE) {
            event.setExperience(0);
        }
    }
}
