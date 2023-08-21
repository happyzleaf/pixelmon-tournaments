package com.hiroku.tournaments.listeners;

import com.pixelmonmod.pixelmon.api.events.PixelmonTradeEvent;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TradeListener
{
	@SubscribeEvent
	public void onTrade(PixelmonTradeEvent event)
	{
		if (event.pokemon1 == null || event.pokemon2 == null || event.player1 == null || event.player2 == null)
			return;
		PokemonSpec rental = new PokemonSpec("rental");
		if (rental.matches(event.pokemon1) || rental.matches(event.pokemon2))
		{
			event.setCanceled(true);
			event.player1.sendMessage(new TextComponentString(TextFormatting.RED + "Trade cancelled; one of the Pokémon was rented for a tournament!"));
			event.player2.sendMessage(new TextComponentString(TextFormatting.RED + "Trade cancelled; one of the Pokémon was rented for a tournament!"));
		}
	}
}
