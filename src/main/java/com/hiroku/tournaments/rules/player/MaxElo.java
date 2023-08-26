package com.hiroku.tournaments.rules.player;

import com.happyzleaf.tournaments.Text;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.elo.EloStorage;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.rules.general.EloType;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.util.helpers.CollectionHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

import java.util.UUID;

public class MaxElo extends PlayerRule {
	int maxElo = 0;

	public MaxElo(String arg) throws Exception {
		super(arg);

		maxElo = Integer.parseInt(arg);
	}

	@Override
	public boolean passes(PlayerEntity player, PlayerPartyStorage storage) {
		return User.hasPermission(player, "tournaments.admin.elo-bypass")
				|| User.hasPermission(player, "tournaments.admin.elo-bypass-team")
				|| EloStorage.getElo(player.getUniqueID(), Tournament.instance().getRuleSet().getRule(EloType.class).eloType) <= maxElo;
	}

	@Override
	public Text getBrokenRuleText(PlayerEntity player) {
		return Text.of(TextFormatting.DARK_AQUA, player.getName(), TextFormatting.RED, " is above the maximum Elo!");
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextFormatting.GOLD, "Maximum Elo: ", TextFormatting.DARK_AQUA, maxElo);
	}

	@Override
	public boolean duplicateAllowed(RuleBase other) {
		return false;
	}

	@Override
	public boolean canTeamJoin(Tournament tournament, Team team, boolean forced) {
		String eloType = Tournament.instance().getRuleSet().getRule(EloType.class).eloType;
		for (UUID uuid : team.getUUIDs()) {
			if (EloStorage.getElo(uuid, eloType) > maxElo) {
				if (CollectionHelper.find(team.users, user -> user.id.equals(uuid)).hasPermission("tournaments.admin.elo-bypass"))
					continue;

				if (CollectionHelper.find(team.users, user -> user.hasPermission("tournaments.admin.elo-bypass-team")) != null)
					continue;

				if (!forced) {
					if (team.users.size() == 1)
						team.sendMessage(Text.of(TextFormatting.RED, "Your Elo is too high to join this tournament. Maximum: " + maxElo));
					else
						team.sendMessage(Text.of(TextFormatting.DARK_AQUA, CollectionHelper.find(team.users, u -> u.id.equals(uuid)).getName(),
								TextFormatting.RED, " has an Elo that is too high to join this tournament. Maximum: " + maxElo));
				}
				return false;
			}
		}

		return true;
	}

	@Override
	public String getSerializationString() {
		return "maxelo:" + maxElo;
	}
}
