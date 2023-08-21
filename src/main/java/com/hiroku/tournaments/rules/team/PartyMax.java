package com.hiroku.tournaments.rules.team;

import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.api.rule.types.TeamRule;
import com.hiroku.tournaments.obj.Team;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

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
				PlayerPartyStorage storage = Pixelmon.storageManager.getParty(user.getUniqueId());
				count += storage.getTeam().size();
			}
		}

		return count <= partyMax;
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextColors.GOLD, "Maximum Party Size: ", TextColors.DARK_AQUA, partyMax);
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
		return Text.of(team.getDisplayText(), TextColors.RED, " exceeds the maximum party size!");
	}

	@Override
	public String getSerializationString() {
		return "partymax:" + partyMax;
	}
}
