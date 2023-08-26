package com.hiroku.tournaments.rules.general;

import com.happyzleaf.tournaments.Text;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.rule.types.GeneralRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.obj.Team;
import net.minecraft.util.text.TextFormatting;

/**
 * Rule representing how many teams are allowed into the tournament. If autostart is true, the tournament will begin once full.
 *
 * @author Hiroku
 */
public class TeamCap extends GeneralRule {
	public int count = -1;
	public boolean autoStart = false;

	public TeamCap(String arg) throws Exception {
		super(arg);

		String[] args = arg.split(",");

		for (String option : args) {
			option = option.toLowerCase();
			if (option.equals("autostart"))
				autoStart = true;
			else {
				try {
					count = Integer.parseInt(option);
				} catch (NumberFormatException nfe) {
					continue;
				}
			}
		}

		if (count <= 1)
			throw new Exception("Missing or invalid team cap");
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextFormatting.GOLD, "Team Cap: ", TextFormatting.DARK_AQUA, count);
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
	public String getSerializationString() {
		return "teamcap:" + count + (autoStart ? ":autostart" : "");
	}

	@Override
	public boolean canTeamJoin(Tournament tournament, Team team, boolean forced) {
		if (tournament.teams.size() >= count) {
			if (!forced)
				team.sendMessage(Text.of(TextFormatting.RED, "The tournament is full."));
			return false;
		}

		return true;
	}

	@Override
	public void onTeamJoin(Tournament tournament, Team team, boolean forced) {
		if (tournament.teams.size() >= count)
			tournament.start();
	}
}
