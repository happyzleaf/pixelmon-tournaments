package com.hiroku.tournaments.listeners;

import com.happyzleaf.tournaments.text.Text;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.archetypes.pokemon.PokemonMatch;
import com.hiroku.tournaments.api.requirements.RentalRequirement;
import com.hiroku.tournaments.obj.Side;
import com.pixelmonmod.pixelmon.api.battles.BattleResults;
import com.pixelmonmod.pixelmon.api.events.PokemonRetrievedEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class BattleListener {
    @SubscribeEvent
    public void onBattleEnd(BattleEndEvent event) {
        if (Tournament.instance() == null) return;

        PokemonMatch match = PokemonMatch.getMatch(event.getBattleController());
        if (match == null || !match.listenToBattleEnd)
            return;
        if (event.isAbnormal())
            match.handleCrashedBattle(Tournament.instance());
        else {
            UUID uuid = match.sides[0].teams[0].users.get(0).id;
            BattleParticipant bp = event.getBattleController().participants.stream()
                    .filter(p ->
                            p instanceof PlayerParticipant
                                    // TODO: Honestly I don't understand this line. I'll call this a day and go grab some cereals :3
                                    && ((PlayerParticipant) p).player.getUniqueID().equals(uuid)).findFirst().get();
            BattleResults result = event.getResults().get(bp);
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
    public void onBattleStarted(BattleStartedEvent.Pre event) {
        if (Tournament.instance() != null) return;

        boolean rentals = false;
        for (BattleParticipant bp : event.getBattleController().participants) {
            if (!(bp instanceof PlayerParticipant)) continue;

            PlayerParticipant pp = (PlayerParticipant) bp;
            PlayerPartyStorage storage = pp.getStorage();
            for (int i = 0; i < PlayerPartyStorage.MAX_PARTY; i++) {
                Pokemon pokemon = storage.get(i);
                if (pokemon == null || !RentalRequirement.is(pokemon)) continue;

                storage.set(i, null);
                rentals = true;
            }
        }

        if (!rentals) return;

        event.setCanceled(true);

        event.getBattleController().participants.stream()
                .filter(bp -> bp instanceof PlayerParticipant)
                .map(bp -> (PlayerParticipant) bp)
                .forEach(pp -> {
                    pp.getTeamPokemonList().forEach(pw -> pw.entity.retrieve(PokemonRetrievedEvent.Constants.FORCED));
                    pp.player.sendMessage(Text.of(TextFormatting.RED, "The battle was canceled because someone had rental pokémon."), Util.DUMMY_UUID);
                });
    }
}



