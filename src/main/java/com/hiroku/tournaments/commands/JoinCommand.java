package com.hiroku.tournaments.commands;

import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.enums.EnumTournamentState;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.rules.general.BattleType;
import com.hiroku.tournaments.rules.general.BattleType.TeamsComposition;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Subcommand for joining a tournament. This can be used to join alone or as a team. Only usable by players.
 *
 * @author Hiroku
 */
public class JoinCommand implements CommandExecutor {
	/**
	 * A static cache of all the invitations sent to other players for joining the tournament together. Held statically for easy access.
	 */
	public static HashMap<UUID, UUID> teamInvitations = new HashMap<UUID, UUID>();

	/**
	 * Gets the UUID of the player who invited the given UUID, if someone did and is still waiting on a response.
	 *
	 * @param uuid - The UUID of the person who was allegedly invited.
	 * @return - The UUID of the player who invited them, or null if no one did or if the inviter has invited someone else instead.
	 */
	public static UUID getInviter(UUID uuid) {
		for (Entry<UUID, UUID> entry : teamInvitations.entrySet())
			if (entry.getValue().equals(uuid))
				return entry.getKey();
		return null;
	}

	/**
	 * Gets the UUID of the player who was invited by the given UUID, if someone was and hasn't responded.
	 *
	 * @param uuid - The UUID of the person who was doing the inviting
	 * @return - The UUID of the player who they invited them, or null if no one was or if the invited has answered or joined another team.
	 */
	public static UUID getInvited(UUID uuid) {
		return teamInvitations.get(uuid);
	}

	public static CommandSpec getSpec() {
		return CommandSpec.builder()
				.executor(new JoinCommand())
				.description(Text.of("Joins the tournament if there is room and your team is allowed to"))
				.permission("tournaments.command.common.join")
				.arguments(
						GenericArguments.optional(GenericArguments.player(Text.of("teammate"))),
						GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of(".")))
				)
				.build();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (!(src instanceof Player)) {
			src.sendMessage(Text.of(TextColors.RED, "You may only join the tournament if you're a player, dummy"));
			return CommandResult.empty();
		}

		Player player = (Player) src;

		Tournament tournament = Tournament.instance();
		if (tournament == null || tournament.state == EnumTournamentState.CLOSED) {
			src.sendMessage(Text.of(TextColors.RED, "There is no tournament to join."));
			return CommandResult.empty();
		}
		if (tournament.state == EnumTournamentState.ACTIVE) {
			src.sendMessage(Text.of(TextColors.RED, "The tournament is active! You cannot join while it is in motion!"));
			return CommandResult.empty();
		}
		if (tournament.getTeam(player.getUniqueId()) != null) {
			src.sendMessage(Text.of(TextColors.RED, "You're already in the tournament!"));
			return CommandResult.empty();
		}

		Player partner = null;

		if (args.hasAny("teammate"))
			partner = args.<Player>getOne(Text.of("teammate")).get();
		else if (args.hasAny(Text.of("."))) {
			src.sendMessage(Text.of(TextColors.RED, "Invalid player: ", TextColors.DARK_AQUA, args.<String>getOne(Text.of(".")).get()));
			return CommandResult.empty();
		}

		if (partner == player) {
			src.sendMessage(Text.of(TextColors.RED, "Nice try, but you can't invite yourself."));
			return CommandResult.empty();
		}

		BattleType rule = Tournament.instance().getRuleSet().getRule(BattleType.class);

		TeamsComposition composition = TeamsComposition.SINGLE;

		if (rule != null)
			composition = rule.composition;

		if ((composition == TeamsComposition.SINGLE || composition == TeamsComposition.DOUBLE_1_PLAYER) && partner != null) {
			src.sendMessage(Text.of(TextColors.RED, "The tournament is for single-player teams only!"));
			return CommandResult.empty();
		}

		if (partner != null) {
			if (partner.getUniqueId().equals(getInviter(player.getUniqueId())))
				acceptInvitation(partner, player);
			else if (Tournament.instance().getTeam(partner.getUniqueId()) != null) {
				player.sendMessage(Text.of(TextColors.DARK_AQUA, partner.getName(), TextColors.RED, " is already in the tournament!"));
				return CommandResult.empty();
			} else
				invitePlayer(player, partner);
			return CommandResult.success();
		}

		Tournament.instance().addTeams(false, Team.of(player));
		return CommandResult.success();
	}

	public static void invitePlayer(Player inviter, Player invitee) {
		invitee.sendMessage(Text.of(TextColors.DARK_AQUA, inviter.getName(), TextColors.YELLOW, " has invited you to join the tournament with them. ",
				Text.of(TextColors.DARK_GREEN, TextActions.executeCallback(src -> acceptInvitation(inviter, invitee)), "Accept"), TextColors.DARK_GRAY,
				" | ", Text.of(TextColors.RED, TextActions.executeCallback(src -> rejectInvitation(inviter, invitee)), "Decline")));

		teamInvitations.put(inviter.getUniqueId(), invitee.getUniqueId());

		inviter.sendMessage(Text.of(TextColors.GRAY, "Invitation sent to ", TextColors.DARK_AQUA, invitee.getName()));
	}

	public static void rejectInvitation(Player inviter, Player invitee) {
		if (teamInvitations.containsKey(inviter.getUniqueId()) && teamInvitations.get(inviter.getUniqueId()).equals(invitee.getUniqueId())) {
			teamInvitations.remove(inviter.getUniqueId());
			inviter.sendMessage(Text.of(TextColors.DARK_AQUA, invitee.getName(), TextColors.RED, " rejected your invitation to team up. Sorry bro."));
		}
		invitee.sendMessage(Text.of(TextColors.GRAY, "Rejected ", TextColors.DARK_AQUA, inviter.getName(), TextColors.GRAY, "'s invitation to team up."));
	}

	public static void acceptInvitation(Player inviter, Player invitee) {
		if (teamInvitations.containsKey(inviter.getUniqueId()) && teamInvitations.get(inviter.getUniqueId()).equals(invitee.getUniqueId())) {
			inviter.sendMessage(Text.of(TextColors.DARK_AQUA, invitee.getName(), TextColors.DARK_GREEN, " accepted your invitation!"));
			teamInvitations.remove(inviter.getUniqueId());
			Tournament.instance().addTeams(Team.of(inviter, invitee));
		} else
			invitee.sendMessage(Text.of(TextColors.DARK_AQUA, inviter.getName(), TextColors.RED, " has invited someone else..."));
	}
}
