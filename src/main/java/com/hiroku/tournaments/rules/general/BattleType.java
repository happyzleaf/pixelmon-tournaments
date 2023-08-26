package com.hiroku.tournaments.rules.general;

import com.happyzleaf.tournaments.Text;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.rule.types.GeneralRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.obj.Team;
import net.minecraft.util.text.TextFormatting;

/**
 * Rule representing the type of battles to take place in the tournament. Described using {@link TeamsComposition}.
 *
 * @author Hiroku
 */
public class BattleType extends GeneralRule {
	/**
	 * Enum for the different ways to put together a team for battle
	 *
	 * @author Hiroku
	 */
	public enum TeamsComposition {
		/**
		 * For simple single battles.
		 */
		SINGLE("single", 1),
		/**
		 * For double battles that involve 1 player per side.
		 */
		DOUBLE_1_PLAYER("double1", 2),
		/**
		 * For double battles that involve 2 players per side.
		 */
		DOUBLE_2_PLAYER("double2", 2),
		/**
		 * For double battles that may involve 1 or 2 players per side.
		 */
		DOUBLE_VARIABLE("double", 2);

		/**
		 * The name of the team composition. This is used when adding {@link BattleType} rules.
		 */
		public final String name;
		/**
		 * The needed Pokémon from 1 side for a battle of this type to be capable of starting.
		 */
		public final int neededPokemon;

		TeamsComposition(String name, int neededPokemon) {
			this.name = name;
			this.neededPokemon = neededPokemon;
		}

		public static TeamsComposition getForName(String name) {
			for (TeamsComposition composition : values())
				if (composition.name.equals(name.toLowerCase()))
					return composition;
			return null;
		}
	}

	/**
	 * The {@link TeamsComposition} describing how to construct the teams for matches.
	 */
	public final TeamsComposition composition;

	public BattleType(String arg) throws Exception {
		super(arg);

		composition = TeamsComposition.getForName(arg);

		if (composition == null)
			throw new Exception("Invalid battle type. Options are: single, double, double1, double2");
	}

	@Override
	public Text getDisplayText() {
		if (composition == TeamsComposition.SINGLE)
			return Text.of(TextFormatting.GOLD, "Battle Type: ", TextFormatting.DARK_AQUA, "Single");
		if (composition == TeamsComposition.DOUBLE_1_PLAYER)
			return Text.of(TextFormatting.GOLD, "Battle Type: ", TextFormatting.DARK_AQUA, "Double (1 Player Teams)");
		if (composition == TeamsComposition.DOUBLE_2_PLAYER)
			return Text.of(TextFormatting.GOLD, "Battle Type: ", TextFormatting.DARK_AQUA, "Double (2 Player Teams)");
		return Text.of(TextFormatting.GOLD, "Battle Type: ", TextFormatting.DARK_AQUA, "Double (1-2 Player Teams)");
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
		return "battletype:" + composition.name;
	}

	@Override
	public boolean canTeamJoin(Tournament tournament, Team team, boolean forced) {
		if (this.composition == TeamsComposition.DOUBLE_2_PLAYER && team.users.size() != 2) {
			team.sendMessage(Text.of(TextFormatting.RED, "This is a double battle tournament where you must bring a partner!"));
			team.sendMessage(Text.of(TextFormatting.RED, "Use /tournament join <partner>"));
			return false;
		}
		if ((this.composition == TeamsComposition.SINGLE || this.composition == TeamsComposition.DOUBLE_1_PLAYER) && team.users.size() != 1) {
			team.sendMessage(Text.of(TextFormatting.RED, "You must join this tournament alone!"));
			return false;
		}

		return true;
	}
}
