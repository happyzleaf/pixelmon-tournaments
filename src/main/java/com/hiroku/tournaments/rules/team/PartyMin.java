package com.hiroku.tournaments.rules.team;

import com.happyzleaf.tournaments.text.Text;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.api.rule.types.TeamRule;
import com.hiroku.tournaments.obj.Team;
import net.minecraft.util.text.TextFormatting;

/**
 * Rule demanding that teams have no less than a particular number of Pokémon in their combined party.
 *
 * @author Hiroku
 */
public class PartyMin extends TeamRule {
	/**
	 * The minimum number of Pokémon in a team's combined party.
	 */
	public int partyMin;

	public PartyMin(String arg) throws Exception {
		super(arg);

		partyMin = Integer.parseInt(arg);
		if (partyMin > 6)
			throw new Exception("You can't have a party minimum greater than 6!");
	}

	@Override
	public boolean passes(Team team) {
		if (partyMin == 1)
			return true;

		int count = 0;

		for (User user : team.users) {
			if (user.isOnline()) {
				count += user.getParty().getTeam().size();
			}
		}

		return count >= partyMin;
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextFormatting.GOLD, "Minimum Party Size: ", TextFormatting.DARK_AQUA, partyMin);
	}

	@Override
	public boolean duplicateAllowed(RuleBase other) {
		return false;
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}

	@Override
	public Text getBrokenRuleText(Team team) {
		return Text.of(team.getDisplayText(), TextFormatting.RED, " went below the minimum party size!");
	}

	@Override
	public String getSerializationString() {
		return "partymin:" + partyMin;
	}
}
