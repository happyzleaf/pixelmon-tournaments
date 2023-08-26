package com.hiroku.tournaments.listeners;

import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.rules.general.SetParty;
import com.hiroku.tournaments.rules.player.RandomPokemon;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LoginListener {
	@SubscribeEvent
	public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (Tournament.instance() == null) return;

		Team team = Tournament.instance().getTeam(event.getPlayer().getUniqueID());
		if (team == null) return;

		User user = team.getUser(event.getPlayer().getUniqueID());
		if (team.alive) {
			team.refreshUser(user);
		} else {
			RandomPokemon.removeRentalPokemon(user, true);
			SetParty.restoreLevels(user);
		}
	}
}
