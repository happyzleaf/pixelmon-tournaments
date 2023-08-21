package com.hiroku.tournaments.rules.decider;

import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.api.rule.types.DeciderRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.obj.Side;
import com.hiroku.tournaments.obj.Team;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Decides the winner based on which team has the most health. Matching healths
 * will result in indecision. Though that's bloody rare. Applies to crashes.
 *
 * @author Hiroku
 */
public class HealthTotal extends DeciderRule {
	int weight = 1;

	public HealthTotal(String arg) throws Exception {
		super(arg);

		if (!arg.equals(""))
			this.weight = Integer.parseInt(arg);
	}

	@Override
	public Side decideWinner(Match match) {
		int[] sidesHealths = new int[]{0, 0};

		for (int i = 0; i < 2; i++) {
			for (Team team : match.sides[i].teams) {
				for (User user : team.users) {
					PlayerPartyStorage storage = Pixelmon.storageManager.getParty(user.getUniqueId());
					for (Pokemon pokemon : storage.getTeam())
						sidesHealths[i] += pokemon.getHealth();
				}
			}
		}

		if (sidesHealths[0] > sidesHealths[1])
			return match.sides[0];
		else if (sidesHealths[0] < sidesHealths[1])
			return match.sides[1];
		else
			return null;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	@Override
	public boolean applyToDraws() {
		return false;
	}

	@Override
	public boolean applyToCrashes() {
		return true;
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextColors.GOLD, "Decide crashes: ", TextColors.DARK_AQUA, "Health total [" + getWeight() + "]");
	}

	@Override
	public boolean duplicateAllowed(RuleBase other) {
		return false;
	}

	@Override
	public boolean visibleToAll() {
		return false;
	}

	@Override
	public String getSerializationString() {
		return "healthdecider:" + weight;
	}
}
