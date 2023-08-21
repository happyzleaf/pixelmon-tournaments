package com.hiroku.tournaments.listeners;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.rules.general.SetParty;
import com.hiroku.tournaments.rules.player.RandomPokemon;

public class LoginListener
{
	@Listener
	public void onLogin(ClientConnectionEvent.Join event)
	{
		if (Tournament.instance() == null 
				|| Tournament.instance().getTeam(event.getTargetEntity().getUniqueId()) == null
				|| !Tournament.instance().getTeam(event.getTargetEntity().getUniqueId()).alive)
		{
			RandomPokemon.removeRentalPokemon(event.getTargetEntity(), true);
			SetParty.restoreLevels(event.getTargetEntity());
		}
		else
			Tournament.instance().getTeam(event.getTargetEntity().getUniqueId()).refreshUser(event.getTargetEntity());
	}
}
