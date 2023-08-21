package com.hiroku.tournaments.obj;

import com.hiroku.tournaments.commands.JoinCommand;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.*;

/**
 * Representation of a team, whether it be a single player or several.
 *
 * @author Hiroku
 */
public class Team {
	/**
	 * The list of {@link User}s in the team.
	 */
	public List<User> users = new ArrayList<User>();
	/**
	 * Whether the team is alive or knocked out.
	 */
	public boolean alive = true;
	/**
	 * Whether the team is currently in a tournament match or not.
	 */
	public boolean inMatch = false;

	/**
	 * Checks if the given UUID is a member of this team.
	 *
	 * @param uuid - The UUID to be checked.
	 * @return - true if the given UUID matches a member of this team, otherwise false/
	 */
	public boolean hasPlayer(UUID uuid) {
		for (User user : users)
			if (user.getUniqueId().equals(uuid))
				return true;
		return false;
	}

	/**
	 * Creates a Team based off a list of {@link Player}s.
	 *
	 * @param players - An array of {@link Player}s.
	 * @return - The newly created {@link Team}.
	 */
	public static Team of(Player... players) {
		Team team = new Team();
		for (User user : players) {
			team.users.add(user);
			JoinCommand.teamInvitations.remove(user.getUniqueId());
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
		team.users.addAll(Collections.singletonList(users));
		return team;
	}

	/**
	 * Creates a Team based off a list of {@link EntityPlayerMP}s.
	 *
	 * @param players - An array of {@link EntityPlayerMP}s.
	 * @return - The newly created {@link Team}.
	 */
	public static Team of(EntityPlayerMP... players) {
		Team team = new Team();
		for (EntityPlayerMP player : players)
			// I'm actually not entirely sure if a cast from EntityPlayerMP->User would work, so I'll play it safe
			team.users.add(Sponge.getServer().getPlayer(player.getUniqueID()).get());
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
		for (User user : users)
			if (user.isOnline() || countOffline)
				count++;
		return count;
	}

	/**
	 * Gets the UUIDs of all the members of the team.
	 */
	public ArrayList<UUID> getUUIDs() {
		ArrayList<UUID> uuids = new ArrayList<>();
		for (User user : users)
			uuids.add(user.getUniqueId());
		return uuids;
	}

	/**
	 * Gets the display text for this team.
	 *
	 * @return - The {@link Text} to display to represent this team.
	 */
	public Text getDisplayText() {
		if (users.size() == 1)
			return Text.of(TextColors.DARK_AQUA, users.get(0).getName());
		else
			return Text.of(TextColors.DARK_AQUA, users.get(0).getName(), TextColors.GOLD, " & ", TextColors.DARK_AQUA, users.get(1).getName());
	}

	/**
	 * Sends the given message to all the online players in this team.
	 */
	public void sendMessage(Text text) {
		for (User user : users)
			if (user.isOnline())
				user.getPlayer().get().sendMessage(text);
	}

	/**
	 * Restores the {@link User} reference in the {@link Team} {@link User} list.
	 */
	public void refreshUser(User user) {
		for (int i = 0; i < users.size(); i++) {
			if (users.get(i).getUniqueId().equals(user.getUniqueId())) {
				users.remove(i);
				users.add(i, user);
				return;
			}
		}
	}
}
