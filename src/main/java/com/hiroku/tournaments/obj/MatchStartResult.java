package com.hiroku.tournaments.obj;

import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.api.rule.types.RuleBase;

/**
 * The result of a match start.
 *
 * @author Hiroku
 */
public abstract class MatchStartResult {
	public static Success success() {
		return new Success();
	}

	/**
	 * A successful match start.
	 *
	 * @author Hiroku
	 */
	public static class Success extends MatchStartResult {
	}

	/**
	 * Result representing when a player was offline when the match tried to start
	 *
	 * @author Hiroku
	 */
	public static class PlayerOffline extends MatchStartResult {
		/**
		 * The team the user belonged to
		 */
		public final Team team;
		/**
		 * The user that was offline
		 */
		public final User user;

		public PlayerOffline(Team team, User user) {
			this.team = team;
			this.user = user;
		}
	}

	/**
	 * Result representing when a player lacked the required number Pokemon.
	 *
	 * @author Hiroku
	 */
	public static class InsufficientPokemon extends MatchStartResult {
		/**
		 * The {@link Team} containing the {@link User} without enough Pokémon.
		 */
		public final Team team;
		/**
		 * The {@link User} without enough Pokémon.
		 */
		public final User user;

		public InsufficientPokemon(Team team, User user) {
			this.team = team;
			this.user = user;
		}
	}

	/**
	 * Result representing when a player or team or side broke a rule.
	 *
	 * @author Hiroku
	 */
	public static class RuleBroken extends MatchStartResult {
		/**
		 * The {@link Side} containing the offender.
		 */
		public final Side side;
		/**
		 * The {@link Team} containing the offender.
		 */
		public final Team team;
		/**
		 * The offending {@link User} who broke the rule.
		 */
		public final User user;
		/**
		 * The {@link RuleBase} that was broken.
		 */
		public final RuleBase rule;

		public RuleBroken(Side side, Team team, User user, RuleBase rule) {
			this.side = side;
			this.team = team;
			this.user = user;
			this.rule = rule;
		}
	}

	public static class BattleRuleBroken extends MatchStartResult {
		/**
		 * The {@link Side} containing the offender.
		 */
		public final Side side;
		/**
		 * The {@link Team} containing the offender.
		 */
		public final Team team;
		/**
		 * The offending {@link User} who broke the rule.
		 */
		public final User user;
		/**
		 * The {@link RuleBase} that was broken.
		 */
		public final String battleRule;

		public BattleRuleBroken(Side side, Team team, User user, String battleRule) {
			this.side = side;
			this.team = team;
			this.user = user;
			this.battleRule = battleRule;
		}
	}

	/**
	 * Fires when there is some kind of exception in the code execution that was not expected. This might be a PlayerNotLoadedException,
	 * or some other problem.
	 *
	 * @author Hiroku
	 */
	public static class Error extends MatchStartResult {
		/**
		 * The exception that was thrown
		 */
		public final Exception exception;

		public Error(Exception exception) {
			this.exception = exception;
		}
	}
}