package com.hiroku.tournaments.listeners;

import com.hiroku.tournaments.Tournaments;
import com.pixelmonmod.pixelmon.api.events.BreedEvent;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.user.UserStorageService;

public class BreedListener {
	@SubscribeEvent
	public void onBreeding(BreedEvent.MakeEgg event) {
		PokemonSpec rental = new PokemonSpec("rental");
		if (rental.matches(event.parent1) || rental.matches(event.parent2)) {
			event.setCanceled(true);
			UserStorageService uss = Sponge.getServiceManager().provide(UserStorageService.class).get();
			Tournaments.log(uss.get(event.parent1.getOwnerPlayerUUID()).get().getName() + " is attempting to breed a rental Pokémon!");
		}
	}
}
