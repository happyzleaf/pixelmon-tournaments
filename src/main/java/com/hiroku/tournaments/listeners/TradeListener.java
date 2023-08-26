package com.hiroku.tournaments.listeners;

import com.happyzleaf.tournaments.Text;
import com.hiroku.tournaments.util.PokemonUtils;
import com.pixelmonmod.pixelmon.api.events.PixelmonTradeEvent;
import com.pixelmonmod.pixelmon.api.events.PokegiftEvent;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TradeListener {
	@SubscribeEvent
	public void onTrade(PixelmonTradeEvent event) {
		if (event.getPokemon1() == null || event.getPokemon2() == null || event.getPlayer1() == null || event.getPlayer2() == null) {
			return;
		}

		if (PokemonUtils.isRental(event.getPokemon1()) || PokemonUtils.isRental(event.getPokemon2())) {
			event.setCanceled(true);
			event.getPlayer1().sendMessage(Text.of(TextFormatting.RED, "Trade cancelled; one of the Pokémon was rented for a tournament!"), Util.DUMMY_UUID);
			event.getPlayer2().sendMessage(Text.of(TextFormatting.RED, "Trade cancelled; one of the Pokémon was rented for a tournament!"), Util.DUMMY_UUID);
		}
	}

	@SubscribeEvent
	public void onPokegift(PokegiftEvent event) {
		if (event.getPokemon() == null || event.getGiver() == null || event.getReceiver() == null) {
			return;
		}

		if (PokemonUtils.isRental(event.getPokemon())) {
			event.setCanceled(true);
			event.getGiver().sendMessage(Text.of(TextFormatting.RED, "Trade cancelled; the Pokémon was rented for a tournament!"), Util.DUMMY_UUID);
			event.getReceiver().sendMessage(Text.of(TextFormatting.RED, "Trade cancelled; the Pokémon was rented for a tournament!"), Util.DUMMY_UUID);
		}
	}
}
