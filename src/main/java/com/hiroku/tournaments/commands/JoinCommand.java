package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.Text;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.enums.TournamentStates;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.rules.general.BattleType;
import com.hiroku.tournaments.rules.general.BattleType.TeamsComposition;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import static com.hiroku.tournaments.util.CommandUtils.getOptArgument;

/**
 * Subcommand for joining a tournament. This can be used to join alone or as a team. Only usable by players.
 *
 * @author Hiroku
 */
public class JoinCommand implements Command<CommandSource> {
	/**
	 * A static cache of all the invitations sent to other players for joining the tournament together. Held statically for easy access.
	 */
	public static Map<UUID, UUID> teamInvitations = new HashMap<>();

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

	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("join")
//				.description(Text.of("Joins the tournament if there is room and your team is allowed to"))
				.requires(source -> User.hasPermission(source, "tournaments.command.common.join"))
				.executes(this)
				.then(
						Commands.argument("teammate", EntityArgument.player())
								.executes(this)
								.then(
										Commands.argument(".", StringArgumentType.greedyString())
												.executes(this)
								)
				);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		PlayerEntity player = context.getSource().asPlayer();

		Tournament tournament = Tournament.instance();
		if (tournament == null || tournament.state == TournamentStates.CLOSED) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "There is no tournament to join."), true);
			return 0;
		}
		if (tournament.state == TournamentStates.ACTIVE) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "The tournament is active! You cannot join while it is in motion!"), true);
			return 0;
		}
		if (tournament.getTeam(player.getUniqueID()) != null) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "You're already in the tournament!"), true);
			return 0;
		}

		PlayerEntity partner = getOptArgument(context, "teammate", PlayerEntity.class).orElse(null);
		String arg = getOptArgument(context, ".", String.class).orElse(null);
		if (partner == null && arg != null) {
			// This doesn't make sense. Might be because of old sponge's optional weakness.
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Invalid player: ", TextFormatting.DARK_AQUA, arg), true);
			return 0;
		}

		if (partner == player) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Nice try, but you can't invite yourself."), true);
			return 0;
		}

		BattleType rule = Tournament.instance().getRuleSet().getRule(BattleType.class);

		TeamsComposition composition = TeamsComposition.SINGLE;

		if (rule != null)
			composition = rule.composition;

		if ((composition == TeamsComposition.SINGLE || composition == TeamsComposition.DOUBLE_1_PLAYER) && partner != null) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "The tournament is for single-player teams only!"), true);
			return 0;
		}

		if (partner != null) {
			if (partner.getUniqueID().equals(getInviter(player.getUniqueID())))
				acceptInvitation(partner, player);
			else if (Tournament.instance().getTeam(partner.getUniqueID()) != null) {
				player.sendMessage(Text.of(TextFormatting.DARK_AQUA, partner.getName(), TextFormatting.RED, " is already in the tournament!"), Util.DUMMY_UUID);
				return 0;
			} else
				invitePlayer(player, partner);
			return 1;
		}

		Tournament.instance().addTeams(false, Team.of(player));
		return 1;
	}

	public static void invitePlayer(PlayerEntity inviter, PlayerEntity invitee) {
		invitee.sendMessage(Text.of(TextFormatting.DARK_AQUA, inviter.getName(), TextFormatting.YELLOW, " has invited you to join the tournament with them. "
				// TODO: textactions
//				, Text.of(TextFormatting.DARK_GREEN, TextActions.executeCallback(src -> acceptInvitation(inviter, invitee)), "Accept"), TextFormatting.DARK_GRAY,
//				" | ", Text.of(TextFormatting.RED, TextActions.executeCallback(src -> rejectInvitation(inviter, invitee)), "Decline")
		), Util.DUMMY_UUID);

		teamInvitations.put(inviter.getUniqueID(), invitee.getUniqueID());

		inviter.sendMessage(Text.of(TextFormatting.GRAY, "Invitation sent to ", TextFormatting.DARK_AQUA, invitee.getName()), Util.DUMMY_UUID);
	}

	public static void rejectInvitation(PlayerEntity inviter, PlayerEntity invitee) {
		if (teamInvitations.containsKey(inviter.getUniqueID()) && teamInvitations.get(inviter.getUniqueID()).equals(invitee.getUniqueID())) {
			teamInvitations.remove(inviter.getUniqueID());
			inviter.sendMessage(Text.of(TextFormatting.DARK_AQUA, invitee.getName(), TextFormatting.RED, " rejected your invitation to team up. Sorry bro."), Util.DUMMY_UUID);
		}
		invitee.sendMessage(Text.of(TextFormatting.GRAY, "Rejected ", TextFormatting.DARK_AQUA, inviter.getName(), TextFormatting.GRAY, "'s invitation to team up."), Util.DUMMY_UUID);
	}

	public static void acceptInvitation(PlayerEntity inviter, PlayerEntity invitee) {
		if (teamInvitations.containsKey(inviter.getUniqueID()) && teamInvitations.get(inviter.getUniqueID()).equals(invitee.getUniqueID())) {
			inviter.sendMessage(Text.of(TextFormatting.DARK_AQUA, invitee.getName(), TextFormatting.DARK_GREEN, " accepted your invitation!"), Util.DUMMY_UUID);
			teamInvitations.remove(inviter.getUniqueID());
			Tournament.instance().addTeams(Team.of(inviter, invitee));
		} else
			invitee.sendMessage(Text.of(TextFormatting.DARK_AQUA, inviter.getName(), TextFormatting.RED, " has invited someone else..."), Util.DUMMY_UUID);
	}
}
