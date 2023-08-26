package com.hiroku.tournaments.rules.team;

import com.happyzleaf.tournaments.Text;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.api.rule.types.TeamRule;
import com.hiroku.tournaments.obj.Team;
import net.minecraft.util.text.TextFormatting;

/**
 * Rule demanding that teams have no more than a particular number of Pokémon in their combined party.
 *
 * @author Hiroku
 */
public class PartyMax extends TeamRule {
	/**
	 * The maximum number of Pokémon in a team's combined party.
	 */
	public int partyMax;

	public PartyMax(String arg) throws Exception {
		super(arg);

		partyMax = Integer.parseInt(arg);
		if (partyMax < 1)
			throw new Exception("You can't have a party maximum less than 1!");
	}

	@Override
	public boolean passes(Team team) {
		int count = 0;

		for (User user : team.users) {
			if (user.isOnline()) {
				count += user.getParty().getTeam().size();
			}
		}

		return count <= partyMax;
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextFormatting.GOLD, "Maximum Party Size: ", TextFormatting.DARK_AQUA, partyMax);
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
		return Text.of(team.getDisplayText(), TextFormatting.RED, " exceeds the maximum party size!");
	}

	@Override
	public String getSerializationString() {
		return "partymax:" + partyMax;
	}
}
