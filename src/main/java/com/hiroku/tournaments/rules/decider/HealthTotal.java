package com.hiroku.tournaments.rules.decider;

import com.happyzleaf.tournaments.Text;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.api.rule.types.DeciderRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.obj.Side;
import com.hiroku.tournaments.obj.Team;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import net.minecraft.util.text.TextFormatting;

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
					for (Pokemon pokemon : user.getParty().getTeam())
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
		return Text.of(TextFormatting.GOLD, "Decide crashes: ", TextFormatting.DARK_AQUA, "Health total [" + getWeight() + "]");
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
