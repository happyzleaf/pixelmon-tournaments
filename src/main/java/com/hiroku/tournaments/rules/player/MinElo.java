package com.hiroku.tournaments.rules.player;

import com.happyzleaf.tournaments.text.Text;
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

public class MinElo extends PlayerRule {
	int minElo = 0;

	public MinElo(String arg) throws Exception {
		super(arg);

		minElo = Integer.parseInt(arg);
	}

	@Override
	public boolean passes(PlayerEntity player, PlayerPartyStorage storage) {
		return User.hasPermission(player, "tournaments.admin.elo-bypass")
				|| User.hasPermission(player, "tournaments.admin.elo-bypass-team")
				|| EloStorage.getElo(player.getUniqueID(), Tournament.instance().getRuleSet().getRule(EloType.class).type) >= minElo;
	}

	@Override
	public Text getBrokenRuleText(PlayerEntity player) {
		return Text.of(TextFormatting.DARK_AQUA, player.getName(), TextFormatting.RED, " is below the minimum Elo!");
	}

	@Override
	public boolean visibleToAll() {
		return true;
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextFormatting.GOLD, "Minimum Elo: ", TextFormatting.DARK_AQUA, minElo);
	}

	@Override
	public boolean duplicateAllowed(RuleBase other) {
		return false;
	}

	@Override
	public boolean canTeamJoin(Tournament tournament, Team team, boolean forced) {
		String eloType = Tournament.instance().getRuleSet().getRule(EloType.class).eloType;
		for (UUID uuid : team.getUserIDs()) {
			if (EloStorage.getElo(uuid, eloType) < minElo) {
				if (CollectionHelper.find(team.users, user -> user.id.equals(uuid)).hasPermission("tournaments.admin.elo-bypass"))
					continue;

				if (CollectionHelper.find(team.users, user -> user.hasPermission("tournaments.admin.elo-bypass-team")) != null)
					continue;

				if (!forced) {
					if (team.users.size() == 1)
						team.sendMessage(Text.of(TextFormatting.RED, "Your Elo is too low to join this tournament. Minimum: " + minElo));
					else
						team.sendMessage(Text.of(TextFormatting.DARK_AQUA, CollectionHelper.find(team.users, u -> u.id.equals(uuid)).getName(),
								TextFormatting.RED, " has an Elo that is too low to join this tournament. Minimum: " + minElo));
				}
				return false;
			}
		}

		return true;
	}

	@Override
	public String getSerializationString() {
		return "minelo:" + minElo;
	}
}
