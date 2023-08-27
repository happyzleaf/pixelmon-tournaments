package com.hiroku.tournaments.rules.team;

import com.happyzleaf.tournaments.text.Text;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.api.rule.types.TeamRule;
import com.hiroku.tournaments.elo.EloStorage;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.rules.general.EloType;
import com.pixelmonmod.pixelmon.api.util.helpers.CollectionHelper;
import net.minecraft.util.text.TextFormatting;

import java.util.UUID;

public class MaxTeamElo extends TeamRule {
	public int maxTeamElo = 0;

	public MaxTeamElo(String arg) throws Exception {
		super(arg);

		maxTeamElo = Integer.parseInt(arg);
	}

	@Override
	public boolean passes(Team team) {
		int totalElo = 0;
		for (UUID uuid : team.getUserIDs())
			totalElo += EloStorage.getElo(uuid, Tournament.instance().getRuleSet().getRule(EloType.class).type);
		return CollectionHelper.find(team.users, user -> user.hasPermission("tournaments.admin.elo-bypass-team")) != null
				|| Math.round(1f * totalElo / team.users.size()) <= maxTeamElo;
	}

	@Override
	public boolean canTeamJoin(Tournament tournament, Team team, boolean forced) {
		if (!passes(team)) {
			if (!forced)
				team.sendMessage(Text.of(TextFormatting.RED, "Your Elo is too high to join this tournament. It must be less than " + maxTeamElo));
			return false;
		}

		return true;
	}

	@Override
	public Text getBrokenRuleText(Team team) {
		return Text.of(team.getDisplayText(), TextFormatting.RED, " is above the maximum Elo!");
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
	public Text getDisplayText() {
		return Text.of(TextFormatting.GOLD, "Maximum Team-Average Elo: ", TextFormatting.DARK_AQUA, maxTeamElo);
	}

	@Override
	public String getSerializationString() {
		return "maxteamelo:" + maxTeamElo;
	}
}
