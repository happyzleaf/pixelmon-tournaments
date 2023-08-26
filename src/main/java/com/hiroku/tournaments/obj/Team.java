package com.hiroku.tournaments.obj;

import com.happyzleaf.tournaments.Text;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.commands.JoinCommand;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Representation of a team, whether it be a single player or several.
 *
 * @author Hiroku
 * @author happyz
 */
public class Team {
	/**
	 * The list of {@link User}s in the team.
	 */
	public List<User> users = new ArrayList<>();
	/**
	 * Whether the team is alive or knocked out.
	 */
	public boolean alive = true;

	/**
	 * Checks if the given UUID is a member of this team.
	 *
	 * @param id - The UUID to be checked.
	 * @return - true if the given UUID matches a member of this team, otherwise false/
	 */
	public boolean hasUser(UUID id) {
		for (User user : users) {
			if (user.id.equals(id)) {
				return true;
			}
		}

		return false;
	}

	public User getUser(UUID id) {
		for (User user : users) {
			if (user.id.equals(id)) {
				return user;
			}
		}

		return null;
	}

	/**
	 * Creates a Team based off a list of {@link PlayerEntity}s.
	 *
	 * @param players - An array of {@link PlayerEntity}s.
	 * @return - The newly created {@link Team}.
	 */
	public static Team of(PlayerEntity... players) {
		Team team = new Team();
		for (PlayerEntity player : players) {
			team.users.add(new User(player));
			JoinCommand.teamInvitations.remove(player.getUniqueID());
		}
		return team;
	}

	/**
	 * Creates a Team based off a list of {@link User}s.
	 *
	 * @param users - An array of {@link User}s.
	 * @return - The newly created {@link Team}.
	 */
	public static Team of(User... users) {
		Team team = new Team();
		team.users.addAll(Arrays.asList(users));
		return team;
	}

	/**
	 * Gets the number of players in this team, excluding offline players if the argument is false.
	 *
	 * @param countOffline - Whether offline players should be counted.
	 * @return - The number of players in this team, excluding offline players if the argument is false.
	 */
	public int getNumPlayers(boolean countOffline) {
		int count = 0;
		for (User user : users) {
			if (user.isOnline() || countOffline) {
				count++;
			}
		}

		return count;
	}

	/**
	 * Gets the UUIDs of all the members of the team.
	 */
	public List<UUID> getUserIDs() {
		List<UUID> uuids = new ArrayList<>();
		for (User user : users) {
			uuids.add(user.id);
		}

		return uuids;
	}

	/**
	 * Gets the display text for this team.
	 *
	 * @return - The {@link ITextComponent} to display to represent this team.
	 */
	public Text getDisplayText() {
		if (users.size() == 1)
			return Text.of(TextFormatting.DARK_AQUA, users.get(0).getName());
		else
			return Text.of(TextFormatting.DARK_AQUA, users.get(0).getName(), TextFormatting.GOLD, " & ", TextFormatting.DARK_AQUA, users.get(1).getName());
	}

	/**
	 * Sends the given message to all the online players in this team.
	 */
	public void sendMessage(Text text) {
		for (User user : users)
			if (user.isOnline())
				user.getPlayer().sendMessage(text, Util.DUMMY_UUID);
	}

	/**
	 * Restores the {@link User} reference in the {@link Team} {@link User} list.
	 */
	public void refreshUser(User user) {
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).id.equals(user.id)) {
				users.remove(i);
				users.add(i, user);
				return;
			}
		}
	}
}
