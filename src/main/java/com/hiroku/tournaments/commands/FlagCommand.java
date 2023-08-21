package com.hiroku.tournaments.commands;

import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.hiroku.tournaments.Tournaments;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.archetypes.pokemon.PokemonMatch;
import com.hiroku.tournaments.enums.EnumTournamentState;
import com.hiroku.tournaments.obj.Side;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.comm.packetHandlers.battles.EndSpectate;
import com.pixelmonmod.pixelmon.enums.battle.EnumBattleEndCause;

/**
 * Command for flagging battles as buggy. When both sides have flagged the battle, the match will end
 * as a crashed ending and it will move to decision rules before a rematch.
 * 
 * @author Hiroku
 */
public class FlagCommand implements CommandExecutor
{
	public static CommandSpec getSpec()
	{
		return CommandSpec.builder()
				.permission("tournaments.command.common.flag")
				.executor(new FlagCommand())
				.description(Text.of("Marks your tournament battle as bugged"))
				.build();
	}
	
	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
		final PokemonMatch match;
		
		if (!(src instanceof Player))
		{
			src.sendMessage(Text.of(TextColors.RED, "You have to be a player to flag your battle as bugged, obviously"));
			return CommandResult.empty();
		}

		Player player = (Player)src;
		
		if (Tournament.instance() == null || Tournament.instance().state != EnumTournamentState.ACTIVE 
				|| (Tournament.instance().getMatch(player.getUniqueId())) == null 
				|| !(Tournament.instance().getMatch(player.getUniqueId()) instanceof PokemonMatch)
				|| (match = (PokemonMatch)Tournament.instance().getMatch(player.getUniqueId())).bcb == null)
		{
			src.sendMessage(Text.of(TextColors.RED, "No match to flag."));
			return CommandResult.empty();
		}
		
		Side side = match.getSide(player.getUniqueId());
		
		if (side == match.sides[0])
			match.side1BugFlag = true;
		else
			match.side2BugFlag = true;
		
		if (match.isBattleBugged())
		{
			Tournaments.log("Match " + match.getDisplayText().toPlain() + " signalled as bugged. Attempting to force restart");
			match.listenToBattleEnd = false;
			try
			{
				match.bcb.endBattle(EnumBattleEndCause.FORCE);
			}
			catch (Exception e)
			{
				match.bcb.spectators.forEach(spectator -> spectator.sendMessage(new EndSpectate()));
				match.bcb.participants.forEach(p -> p.endBattle(EnumBattleEndCause.FORCE));
				BattleRegistry.deRegisterBattle(match.bcb);
			}
			
			match.listenToBattleEnd = true;
			Sponge.getScheduler().createSyncExecutor(Tournaments.INSTANCE).schedule(()->match.start(true), 1, TimeUnit.SECONDS);
			match.sendMessage(Text.of(TextColors.GRAY, "Match signalled as bugged. Forcefully restarting"));
			return CommandResult.success();
		}
		else
		{
			match.sendMessage(Text.of(side.getDisplayText(), TextColors.YELLOW, " has flagged the battle as bugged using /tournament flag."));
			return CommandResult.success();
		}
	}
}
