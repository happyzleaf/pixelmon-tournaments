package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.Scheduler;
import com.happyzleaf.tournaments.text.Text;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.Tournaments;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.archetypes.pokemon.PokemonMatch;
import com.hiroku.tournaments.enums.TournamentStates;
import com.hiroku.tournaments.obj.Side;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.api.battles.BattleEndCause;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.EndSpectatePacket;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

import java.util.concurrent.TimeUnit;

/**
 * Command for flagging battles as buggy. When both sides have flagged the battle, the match will end
 * as a crashed ending, and it will move to decision rules before a rematch.
 *
 * @author Hiroku
 */
public class FlagCommand implements Command<CommandSource> {
	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("flag")
//				.description(Text.of("Marks your tournament battle as bugged"))
				.requires(source -> User.hasPermission(source, "tournaments.command.common.flag"))
				.executes(this);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		PlayerEntity player = context.getSource().asPlayer();

		final PokemonMatch match;
		if (Tournament.instance() == null || Tournament.instance().state != TournamentStates.ACTIVE
				|| (Tournament.instance().getMatch(player.getUniqueID())) == null
				|| !(Tournament.instance().getMatch(player.getUniqueID()) instanceof PokemonMatch)
				|| (match = (PokemonMatch) Tournament.instance().getMatch(player.getUniqueID())).battle == null) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No match to flag."), true);
			return 0;
		}

		Side side = match.getSide(player.getUniqueID());

		if (side == match.sides[0])
			match.side1BugFlag = true;
		else
			match.side2BugFlag = true;

		if (match.isBattleBugged()) {
			Tournaments.log("Match " + match.getDisplayText().toPlain() + " signalled as bugged. Attempting to force restart");
			match.listenToBattleEnd = false;
			try {
				match.battle.endBattle(BattleEndCause.FORCE);
			} catch (Exception e) {
				match.battle.spectators.forEach(spectator -> spectator.sendMessage(new EndSpectatePacket()));
				match.battle.participants.forEach(p -> p.endBattle(BattleEndCause.FORCE));
				BattleRegistry.deRegisterBattle(match.battle);
			}

			match.listenToBattleEnd = true;
			Tournament.instance().tasks.add(Scheduler.delay(1, TimeUnit.SECONDS, () -> match.start(true)));
			match.sendMessage(Text.of(TextFormatting.GRAY, "Match signalled as bugged. Forcefully restarting"));

			return 1;
		}

		match.sendMessage(Text.of(side.getDisplayText(), TextFormatting.YELLOW, " has flagged the battle as bugged using /tournament flag."));

		return 1;
	}
}
