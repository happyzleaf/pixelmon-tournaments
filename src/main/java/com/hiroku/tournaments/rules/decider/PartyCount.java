package com.hiroku.tournaments.rules.decider;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.api.rule.types.DeciderRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.obj.Side;
import com.hiroku.tournaments.obj.Team;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;

/**
 * Decides the winner based on who has the most living party Pokémon. Matching party counts
 * will result in indecision. Applies to crashes.
 * 
 * @author Hiroku
 */
public class PartyCount extends DeciderRule
{
	int weight = 3;
	
	public PartyCount(String arg) throws Exception
	{
		super(arg);
		
		if (!arg.equals(""))
			weight = Integer.parseInt(arg);
	}

	@Override
	public Side decideWinner(Match match)
	{
		int[] sideCounts = new int[] {0, 0};
		for (int i = 0 ; i < 2 ; i++)
		{
			for (Team team : match.sides[i].teams)
			{
				for (User user : team.users)
				{
					PlayerPartyStorage storage =  Pixelmon.storageManager.getParty(user.getUniqueId());
					sideCounts[i] += storage.findAll(pokemon -> !pokemon.isEgg() && pokemon.getHealth() > 0).size(); 
				}
			}
		}
		
		if (sideCounts[0] > sideCounts[1])
			return match.sides[0];
		else if (sideCounts[0] < sideCounts[1])
			return match.sides[1];
		else
			return null;
	}

	@Override
	public int getWeight()
	{
		return weight;
	}

	@Override
	public boolean applyToDraws()
	{
		return false;
	}

	@Override
	public boolean applyToCrashes()
	{
		return true;
	}

	@Override
	public Text getDisplayText()
	{
		return Text.of(TextColors.GOLD, "Decide crashes: ", TextColors.DARK_AQUA, "Party count [" + getWeight() + "]");
	}

	@Override
	public boolean duplicateAllowed(RuleBase other)
	{
		return false;
	}

	@Override
	public boolean visibleToAll()
	{
		return false;
	}

	@Override
	public String getSerializationString()
	{
		return "partycount:" + getWeight();
	}
}
