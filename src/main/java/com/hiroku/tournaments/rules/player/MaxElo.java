package com.hiroku.tournaments.rules.player;

import java.util.UUID;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.elo.EloStorage;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.rules.general.EloType;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.util.helpers.CollectionHelper;

public class MaxElo extends PlayerRule
{
	int maxElo = 0;
	
	public MaxElo(String arg) throws Exception
	{
		super(arg);
		
		maxElo = Integer.parseInt(arg);
	}

	@Override
	public boolean passes(Player player, PlayerPartyStorage storage)
	{
		return player.hasPermission("tournaments.admin.elo-bypass") 
				|| player.hasPermission("tournaments.admin.elo-bypass-team")
				|| EloStorage.getElo(player.getUniqueId(), Tournament.instance().getRuleSet().getRule(EloType.class).eloType) <= maxElo;
	}

	@Override
	public Text getBrokenRuleText(Player player)
	{
		return Text.of(TextColors.DARK_AQUA, player.getName(), TextColors.RED, " is above the maximum Elo!");
	}
	
	@Override
	public boolean visibleToAll()
	{
		return true;
	}

	@Override
	public Text getDisplayText()
	{
		return Text.of(TextColors.GOLD, "Maximum Elo: ", TextColors.DARK_AQUA, maxElo);
	}

	@Override
	public boolean duplicateAllowed(RuleBase other)
	{
		return false;
	}

	@Override
	public boolean canTeamJoin(Tournament tournament, Team team, boolean forced)
	{
		String eloType = Tournament.instance().getRuleSet().getRule(EloType.class).eloType;
		for (UUID uuid : team.getUUIDs())
		{
			if (EloStorage.getElo(uuid, eloType) > maxElo)
			{
				if (CollectionHelper.find(team.users, user -> user.getUniqueId().equals(uuid)).hasPermission("tournaments.admin.elo-bypass"))
					continue;
				
				if (CollectionHelper.find(team.users, user -> user.hasPermission("tournaments.admin.elo-bypass-team")) != null)
					continue;
				
				if (!forced)
				{
					if (team.users.size() == 1)
						team.sendMessage(Text.of(TextColors.RED, "Your Elo is too high to join this tournament. Maximum: " + maxElo));
					else
						team.sendMessage(Text.of(TextColors.DARK_AQUA, CollectionHelper.find(team.users, u -> u.getUniqueId().equals(uuid)).getName(),
								TextColors.RED, " has an Elo that is too high to join this tournament. Maximum: " + maxElo));
				}
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public String getSerializationString()
	{
		return "maxelo:" + maxElo;
	}
}
