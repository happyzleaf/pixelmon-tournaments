package com.hiroku.tournaments.listeners;

import com.hiroku.tournaments.Tournaments;
import com.hiroku.tournaments.api.requirements.RentalRequirement;
import com.pixelmonmod.pixelmon.api.daycare.event.DayCareEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BreedListener {
	@SubscribeEvent
	public void onDayCareAdd(DayCareEvent.PrePokemonAdd event) {
		if (RentalRequirement.is(event.getParentOne()) || RentalRequirement.is(event.getParentTwo())) {
			event.setCanceled(true);
			Tournaments.log(event.getPlayer().getDisplayName().getString() + " is attempting to breed a rental Pokémon!");
		}
	}
}
