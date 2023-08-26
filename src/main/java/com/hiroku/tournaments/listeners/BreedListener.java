package com.hiroku.tournaments.listeners;

import com.hiroku.tournaments.Tournaments;
import com.hiroku.tournaments.util.PokemonUtils;
import com.pixelmonmod.pixelmon.api.daycare.event.DayCareEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BreedListener {
	@SubscribeEvent
	public void onDayCareAdd(DayCareEvent.PrePokemonAdd event) {
		if (PokemonUtils.isRental(event.getParentOne()) || PokemonUtils.isRental(event.getParentTwo())) {
			event.setCanceled(true);
			Tournaments.log(event.getPlayer().getDisplayName().getString() + " is attempting to breed a rental Pokémon!");
		}
	}
}
