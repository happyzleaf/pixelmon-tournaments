package com.hiroku.tournaments.rules.general;

import com.happyzleaf.tournaments.text.Text;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.Tournaments;
import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.rule.types.GeneralRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.obj.Side;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.rules.player.LevelMax;
import com.hiroku.tournaments.rules.player.LevelMin;
import com.hiroku.tournaments.rules.player.RandomPokemon;
import com.pixelmonmod.api.pokemon.requirement.impl.LevelRequirement;
import com.pixelmonmod.api.requirement.Requirement;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class SetParty extends GeneralRule {
	public static final String OLD_LEVEL_KEY = "oldlevel"; // TODO: remove this nonsense?

	public int specificLevel = -1;

	public SetParty(String arg) throws Exception {
		super(arg);

		if (!arg.equals("")) {
			specificLevel = Integer.parseInt(arg);
			if (specificLevel < 1 || specificLevel > 100)
				throw new Exception("Invalid level: " + arg);
		}
	}

	@Override
	public Text getDisplayText() {
		return Text.of(TextFormatting.GOLD, "Party Pokémon will be temporarily set to level: ", TextFormatting.DARK_AQUA, specificLevel == -1 ? "Level max rule value" : specificLevel);
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
		return "setparty:" + (specificLevel == -1 ? "" : specificLevel);
	}

	@Override
	public void onMatchStart(Tournament tournament, Match match) {
		int targetLevel = specificLevel;
		int underLevel = specificLevel - 1;
		int overLevel = specificLevel + 1;
		if (targetLevel == -1) {
			LevelMax maxRule = tournament.getRuleSet().getRule(LevelMax.class);
			LevelMin minRule = tournament.getRuleSet().getRule(LevelMin.class);

			if (maxRule == null && minRule == null) {
				RandomPokemon randomRule = tournament.getRuleSet().getRule(RandomPokemon.class);
				Integer randomRuleLevel = randomRule == null ? null : randomRule.spec.getRequirement(LevelRequirement.class).map(Requirement::getValue).orElse(null);
				if (randomRuleLevel != null) {
					targetLevel = randomRuleLevel;
					underLevel = targetLevel - 1;
					overLevel = targetLevel + 1;
				}
				// This is bad. Assume level 100s
				else {
					targetLevel = 100;
					underLevel = 99;
					overLevel = 101;
				}
			} else if (maxRule == null) {
				targetLevel = minRule.levelMin;
				underLevel = targetLevel - 1;
				overLevel = 101;
			} else if (minRule == null) {
				targetLevel = maxRule.levelMax;
				underLevel = 0;
				overLevel = targetLevel + 1;
			} else {
				targetLevel = maxRule.levelMax;
				underLevel = minRule.levelMin - 1;
				overLevel = targetLevel + 1;
			}
		}

		for (Side side : match.sides) {
			for (Team team : side.teams) {
				for (User user : team.users) {
					PlayerPartyStorage party = user.getParty();
					for (Pokemon pokemon : party.getTeam()) {
						int level = pokemon.getPokemonLevel();
						if (level <= underLevel || level >= overLevel) {
							Tournaments.log(user.getName() + "'s " + pokemon.getSpecies().getName() + " was not an acceptable level (" + level + "). Setting to " + targetLevel);
							pokemon.setLevel(targetLevel);
							if (!pokemon.getPersistentData().contains(OLD_LEVEL_KEY)) {
								pokemon.getPersistentData().putInt(SetParty.OLD_LEVEL_KEY, level);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void onMatchEnd(Tournament tournament, Match match, Side winningSide, Side losingSide) {
		for (Team team : losingSide.teams)
			team.users.forEach(SetParty::restoreLevels);
	}

	@Override
	public void onTournamentEnd(Tournament tournament, List<User> winners) {
		winners.forEach(SetParty::restoreLevels);
	}

	@Override
	public boolean canRuleBeRemoved(Tournament tournament, RuleBase rule) {
		return !(rule instanceof LevelMax) || specificLevel != -1;
	}

	public static void restoreLevels(User user) {
		PlayerPartyStorage storage = user.getParty();
		for (Pokemon pokemon : storage.getTeam()) {
			if (pokemon.getPersistentData().contains(SetParty.OLD_LEVEL_KEY)) {
				int oldLevel = pokemon.getPersistentData().getInt(SetParty.OLD_LEVEL_KEY);
				pokemon.setLevel(oldLevel);
				pokemon.getPersistentData().remove(SetParty.OLD_LEVEL_KEY);
			}
		}
	}
}