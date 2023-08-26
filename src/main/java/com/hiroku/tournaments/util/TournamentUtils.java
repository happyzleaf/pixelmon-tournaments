package com.hiroku.tournaments.util;

import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.reward.RewardBase;
import com.hiroku.tournaments.api.rule.types.DeciderRule;
import com.hiroku.tournaments.obj.Team;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Utility class filled with various bits and pieces to help with Tournaments. Many of these functions are pinched
 * from Oblique's API. I wrote it so it's ok for me to steal from it \o/
 *
 * @author Hiroku
 */
public class TournamentUtils {
	/**
	 * Gives items to a player, and drops the leftover on the ground, announcing that it was dropped
	 *
	 * @param player - The Player to give the items to
	 * @param stack  - The SpongeAPI ItemStack to give to the player
	 */
	public static void giveItemsToPlayer(Player player, org.spongepowered.api.item.inventory.ItemStack stack) {
		InventoryTransactionResult result = player.getInventory().offer(stack);
		if (result.getRejectedItems().size() > 0) {
			Location<World> location = player.getLocation();
			for (ItemStackSnapshot snapshot : result.getRejectedItems()) {
				Entity item = location.getExtent().createEntity(EntityTypes.ITEM, location.getPosition());
				if (item != null) {
					item.offer(Keys.REPRESENTED_ITEM, snapshot);
					location.getExtent().spawnEntity(item);

					player.sendMessage(Text.of(TextColors.GRAY, "Your inventory was full. ",
							TextColors.BLUE, snapshot.getQuantity(), " ", item.getTranslation().get(Locale.ENGLISH),
							TextColors.GRAY, " dropped to the ground."));
				}
			}
		}
	}

	/**
	 * Simply creates a directory for a particular path.
	 *
	 * @param path - The path that will require a new directory.
	 */
	public static void createDir(String path) {
		File file = new File(path);
		if (!file.exists())
			file.mkdirs();
	}

	/**
	 * Orders a series of {@link DeciderRule}s in descending order of weight.
	 *
	 * @param deciders - The list of {@link DeciderRule}s that must be sorted. This must have at least 2 elements.
	 * @return - The sorted list where the first element has the highest weight and the last has the lowest.
	 */
	public static List<DeciderRule> getOrderedDeciderRules(List<DeciderRule> deciders) {
		if (deciders == null || deciders.size() < 2)
			return deciders;

		// Use a simple bubble sort to put them in descending order of weight.

		boolean sorted = false;
		while (!sorted) {
			sorted = true;
			for (int i = 1; i < deciders.size(); i++) {
				DeciderRule prev = deciders.get(i - 1);
				DeciderRule next = deciders.get(i);

				if (next.getWeight() > prev.getWeight()) {
					deciders.remove(next);
					deciders.add(i - 1, next);
					sorted = false;
				}
			}
		}

		return deciders;
	}

	/**
	 * Lists all the teams to the player with options for removing if they are allowed.
	 */
	public static Text showTeams(CommandSource src) {
		Tournament tournament = Tournament.instance();
		Text.Builder builder = Text.builder();
		builder.append(Text.of(TextColors.GOLD, TextStyles.UNDERLINE, "Teams:\n"));
		if (tournament.teams.isEmpty())
			builder.append(Text.of(TextColors.GRAY, "No teams."));
		else {
			List<Team> liveTeams = new ArrayList<>();
			List<Team> deadTeams = new ArrayList<>();

			for (Team team : tournament.teams) {
				if (team.alive)
					liveTeams.add(team);
				else
					deadTeams.add(team);
			}

			List<Team> orderedTeams = new ArrayList<>(liveTeams);
			orderedTeams.addAll(deadTeams);

			builder.append(Text.of(orderedTeams.get(0).alive ? Text.of(TextColors.GREEN, "*") : "", orderedTeams.get(0).getDisplayText()));
			for (int i = 1; i < tournament.teams.size() && i < 10; i++)
				builder.append(Text.of("\n", orderedTeams.get(i).alive ? Text.of(TextColors.GREEN, "*") : "", orderedTeams.get(i).getDisplayText()));
		}
		return builder.build();
	}

	/**
	 * Shows the matches that are ongoing in the round to the given CommandSource.
	 */
	public static Text showMatches(CommandSource src) {
		Tournament tournament = Tournament.instance();
		if (tournament == null)
			return Text.of(TextColors.GRAY, "No tournament");
		Text.Builder builder = Text.builder();
		builder.append(Text.of(TextColors.GOLD, TextStyles.UNDERLINE, "Upcoming Matches:\n"));
		if (tournament.round.isEmpty())
			builder.append(Text.of(TextColors.GRAY, "No matches in current round."));
		else {
			for (int i = 0; i < tournament.round.size() && i < 10; i++) {
				Match match = tournament.round.get(i);
				builder.append(Text.of((i == 0 ? "" : "\n"), match.getStateText(), match.getDisplayText()));
			}
		}
		return builder.build();
	}

	/**
	 * Shows the rewards for the tournament to the given CommandSource
	 */
	public static Text showRewards(CommandSource src) {
		Tournament tournament = Tournament.instance();
		if (tournament == null)
			return Text.of(TextColors.GRAY, "No tournament");

		Player player = src instanceof Player ? (Player) src : null;

		Text.Builder builder = Text.builder();
		builder.append(Text.of(TextColors.GOLD, TextStyles.UNDERLINE, "Rewards:\n"));
		List<RewardBase> visibleRewards = new ArrayList<>(tournament.rewards);
		visibleRewards.removeIf(reward -> !reward.canShow(player));
		if (visibleRewards.isEmpty())
			builder.append(Text.of(TextColors.GRAY, "No rewards in tournament"));
		else {
			builder.append(Text.of(visibleRewards.get(0).getDisplayText()));
			for (int i = 1; i < visibleRewards.size() && i < 10; i++)
				builder.append(Text.of("\n", visibleRewards.get(i).getDisplayText()));
		}

		return builder.build();
	}
}