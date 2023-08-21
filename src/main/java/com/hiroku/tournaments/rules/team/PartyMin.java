package com.hiroku.tournaments.rules.team;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.api.rule.types.TeamRule;
import com.hiroku.tournaments.obj.Team;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;

/**
 * Rule demanding that teams have no less than a particular number of Pokémon in their combined party.
 * 
 * @author Hiroku
 */
public class PartyMin extends TeamRule
{
	/** The minimum number of Pokémon in a team's combined party. */
	public int partyMin;

	public PartyMin(String arg) throws Exception
	{
		super(arg);
		
		partyMin = Integer.parseInt(arg);
		if (partyMin > 6)
			throw new Exception("You can't have a party minimum greater than 6!");
	}

	@Override
	public boolean passes(Team team)
	{
		if (partyMin == 1)
			return true;
		
		int count = 0;
		
		for (User user : team.users)
		{
			if (user.isOnline())
			{
				PlayerPartyStorage storage = Pixelmon.storageManager.getParty( user.getUniqueId());
				count += storage.getTeam().size();
			}
		}
		
		return count >= partyMin;
	}

	@Override
	public Text getDisplayText()
	{
		return Text.of(TextColors.GOLD, "Minimum Party Size: ", TextColors.DARK_AQUA, partyMin);
	}

	@Override
	public boolean duplicateAllowed(RuleBase other)
	{
		return false;
	}

	@Override
	public boolean visibleToAll()
	{
		return true;
	}

	@Override
	public Text getBrokenRuleText(Team team)
	{
		return Text.of(team.getDisplayText(), TextColors.RED, " went below the minimum party size!");
	}

	@Override
	public String getSerializationString()
	{
		return "partymin:" + partyMin;
	}
}
