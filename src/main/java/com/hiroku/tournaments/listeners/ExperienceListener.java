package com.hiroku.tournaments.listeners;

import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.archetypes.pokemon.PokemonMatch;
import com.hiroku.tournaments.enums.EnumTournamentState;
import com.hiroku.tournaments.rules.general.SetParty;
import com.pixelmonmod.pixelmon.api.events.ExperienceGainEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleControllerBase;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.links.DelegateLink;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.links.WrapperLink;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Listener for experience earning. This is to prevent experience gain from tournament battles.
 * 
 * @author Hiroku
 */
public class ExperienceListener
{
	@SubscribeEvent
	public void onExperienceEarned(ExperienceGainEvent event)
	{
		if (event.pokemon == null)
			return;
		
		Pokemon pokemon = null;
		if (event.pokemon instanceof DelegateLink)
			pokemon = ((DelegateLink)event.pokemon).pokemon;
		else if (event.pokemon instanceof WrapperLink)
			pokemon = ((WrapperLink)event.pokemon).getPokemon();
		
		if (Tournament.instance() != null && Tournament.instance().state == EnumTournamentState.ACTIVE)
		{
			EntityPlayerMP player = pokemon.getOwnerPlayer();
			if (player != null)
			{
				BattleControllerBase bcb = BattleRegistry.getBattle(player);
				if (bcb != null && PokemonMatch.getMatch(bcb) != null)
					event.setExperience(0);
			}
		}
		
		if (new PokemonSpec("rental").matches(pokemon) || pokemon.getPersistentData().hasKey(SetParty.OLD_LEVEL_KEY))
			event.setExperience(0);
	}
}
