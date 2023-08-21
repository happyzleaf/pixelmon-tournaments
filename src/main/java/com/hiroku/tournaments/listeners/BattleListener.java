package com.hiroku.tournaments.listeners;

import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.archetypes.pokemon.PokemonMatch;
import com.hiroku.tournaments.obj.Side;
import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonSpec;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.enums.battle.BattleResults;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.UUID;

public class BattleListener {
	@SubscribeEvent
	public void onBattleEnd(BattleEndEvent event) {
		if (Tournament.instance() == null)
			return;
		PokemonMatch match = PokemonMatch.getMatch(event.bc);
		if (match == null || !match.listenToBattleEnd)
			return;
		if (event.abnormal)
			match.handleCrashedBattle();
		else {
			UUID uuid = match.sides[0].teams[0].users.get(0).getUniqueId();
			BattleParticipant bp = event.bc.participants.stream().filter(p -> p instanceof PlayerParticipant
					&& ((PlayerParticipant) p).player.getUniqueID().equals(uuid)).findFirst().get();
			BattleResults result = event.results.get(bp);
			Side side1 = match.getSide(uuid);
			Side side2 = match.getOtherSide(side1);

			if (result == BattleResults.VICTORY)
				Tournament.instance().matchEnds(match, side1, side2);
			else if (result == BattleResults.DRAW)
				Tournament.instance().handleDraw(match);
			else
				Tournament.instance().matchEnds(match, side2, side1);
		}
	}

	@SubscribeEvent
	public void onBattleStarted(BattleStartedEvent event) {
		if (Tournament.instance() == null) {
			boolean hadRentals = false;
			for (BattleParticipant bp : event.bc.participants) {
				if (bp instanceof PlayerParticipant) {
					boolean individualHadRentals = false;
					PlayerParticipant pp = (PlayerParticipant) bp;
					for (int i = 0; i < 6; i++) {
						if (pp.getStorage().get(i) != null && new PokemonSpec("rental").matches(pp.getStorage().get(i))) {
							pp.getStorage().set(i, null);
							individualHadRentals = hadRentals = true;
						}
					}
					if (individualHadRentals)
						pp.player.sendMessage(new TextComponentString(TextFormatting.RED + "You had rental Pokémon in your party! Don't worry, they've been deleted for you."));
				}
			}
			if (hadRentals)
				event.setCanceled(true);
		}
	}
}