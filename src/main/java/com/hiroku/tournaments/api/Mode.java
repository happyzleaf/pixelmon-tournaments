package com.hiroku.tournaments.api;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import com.google.common.collect.ImmutableList;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.obj.Side;
import com.hiroku.tournaments.obj.Team;

/**
 * Abstract base for objects that may function as some form of mode for a tournament. This includes rules and rewards.
 * 
 * @author Hiroku
 */
public abstract class Mode
{
	/**
	 * Used when no matches remain. Returning null is default (implies this mode does not calculate winners), 
	 * otherwise provided <code>winners</code> ArrayList should be returned, empty if there were no winners.
	 * 
	 * @param tournament - The tournament to calculate winners for
	 * @param teams - The teams of the tournament (includes teams with alive = false)
	 * @param winners - The empty list of {@link Team}s to be returned with the tournament winners.
	 * 
	 * @return - The winners list either as it was or with {@link Team}s added. Returns null when this mode does
	 * 			 not calculate winners.
	 */
	public ArrayList<Team> calculateWinners(Tournament tournament, ArrayList<Team> teams, ArrayList<Team> winners)
	{
		return null;
	}
	
	/**
	 * Creates the matches for a round. Returns the given list of {@link Match}es either filled or empty to
	 * indicate the end of a tournament. If this returns null, this mode does not do matchmaking.
	 * 
	 * @param tournament - The tournament to create matches for
	 * 
	 * @return - An ArrayList of Match either empty or filled with a round of matches. Returning
	 * 			 null means this mode does not do matchmaking. 
	 */
	public ArrayList<Match> createMatches(Tournament tournament)
	{
		return null;
	}
	
	/**
	 * Runs immediately prior to when a match starts
	 * 
	 * @param tournament - The {@link Tournament} in which the match is.
	 * @param match - The {@link Match} starting.
	 */
	public void onMatchStart(Tournament tournament, Match match)
	{
		;
	}
	
	/**
	 * Runs when a match ends
	 * 
	 * @param tournament - The tournament in which a match is ending. This won't run for draws/crashes
	 * 					   unless they are decided and a winning and losing side are resolved.
	 * @param match - The {@link Match} that's ending
	 * @param winningSide - The {@link Side} that won.
	 * @param losingSide - The {@link Side} that lost.
	 */
	public void onMatchEnd(Tournament tournament, Match match, Side winningSide, Side losingSide)
	{
		;
	}
	
	/**
	 * Runs when a tournament opens.
	 * 
	 * @param tournament - The {@link Tournament} instance that's being opened.
	 */
	public void onTournamentOpen(Tournament tournament)
	{
		;
	}
	
	/**
	 * Runs when a tournament starts (where players are already in the tournament, and the first round is being arranged)
	 * 
	 * @param tournament - The {@link Tournament} being started.
	 */
	public void onTournamentStart(Tournament tournament)
	{
		;
	}

	/**
	 * Runs when a tournament ends; the winners are provided.
	 * 
	 * @param tournament - The {@link Tournament} that is ending.
	 * @param winners - The winners of the tournament. This list may be empty.
	 */
	public void onTournamentEnd(Tournament tournament, List<User> winners)
	{
		;
	}
	
	/**
	 * Determines whether a rule can be added. Defaults to true. If any {@link Mode} returns false, the rule will not be added.
	 * 
	 * @param tournament - The {@link Tournament} whose rules are being added to.
	 * @param rule - The {@link RuleBase} that is being added.
	 * 
	 * @return - <code>true</code> if the rule may be added (default), <code>false</code> if the rule can be added.
	 */
	public boolean canRuleBeAdded(Tournament tournament, RuleBase rule)
	{
		return true;
	}
	
	/**
	 * Determines whether a rule can be removed. Defaults to true. If any {@link Mode} returns false, the rule will not be removed.
	 * 
	 * @param tournament
	 * @param rule
	 * 
	 * @return - <code>true</code> if the rule may be removed (default), <code>false</code> if the rule can be removed.
	 */
	public boolean canRuleBeRemoved(Tournament tournament, RuleBase rule)
	{
		return true;
	}
	
	/**
	 * Determines whether a {@link Team} can join. Defaults to true. If any {@link Mode} returns false, the {@link Team} will be denied entry.
	 * 
	 * @param tournament - The {@link Tournament} the {@link Team} is trying to join.
	 * @param team - The {@link Team} trying to join.
	 * @param forced - Whether the team was forced into the tournament or they joined of their own volition.
	 * 
	 * @return - <code>true</code> if the team may join (default), <code>false</code> if the team may not join.
	 */
	public boolean canTeamJoin(Tournament tournament, Team team, boolean forced)
	{
		return true;
	}
	
	/**
	 * Run when a {@link Team} successfully joins the {@link Tournament}.
	 * 
	 * @param tournament - The {@link Tournament} joined.
	 * @param team - The {@link Team} that joined.
	 * @param forced - Whether the team was forced to join.
	 */
	public void onTeamJoin(Tournament tournament, Team team, boolean forced)
	{
		;
	}
	
	/**
	 * Determines whether a {@link Team} can leave. Defaults to true. If any {@link Mode} returns false, the {@link Team} will be denied leave.
	 * 
	 * @param tournament - The {@link Tournament} the {@link Team} is trying to leave.
	 * @param team - The {@link Team} trying to leave.
	 * @param forced - Whether the team was forced out of the tournament or they left of their own volition.
	 * 
	 * @return - <code>true</code> if the team may leave (default), <code>false</code> if the team may not leave.
	 */
	public boolean canTeamLeave(Tournament tournament, Team team, boolean forced)
	{
		return true;
	}

	/**
	 * Run when a {@link Team} successfully leaves the {@link Tournament}.
	 * 
	 * @param tournament - The {@link Tournament} leaving.
	 * @param team - The {@link Team} that left.
	 * @param forced - Whether the team was forced to leave.
	 */
	public void onTeamLeave(Tournament tournament, Team team, boolean forced)
	{
		;
	}
	
	/**
	 * Determines whether a defeated {@link Team} is knocked out. Defaults to true. If any mode returns false, the team will remain alive.
	 * 
	 * @param tournament - The {@link Tournament} they're being knocked out within.
	 * @param team - The {@link Team} being knocked out.
	 * 
	 * @return <code>true</code> if the team is knocked out (default), <code>false</code> if the team is to remain alive.
	 */
	public boolean isTeamKnockedOut(Tournament tournament, Team team)
	{
		return true;
	}
	
	/**
	 * Runs when a team is knocked out.
	 * 
	 * @param tournament - The {@link Tournament} in which the {@link Team} is being knocked out.
	 * @param team - The {@link Team} that was knocked out.
	 */
	public void onTeamKnockedOut(Tournament tournament, Team team)
	{
		;
	}
	
	/**
	 * Runs when a team is forfeited
	 * 
	 * @param tournament - The {@link Tournament} in which the {@link Team} is forfeiting.
	 * @param team - The {@link Team} forfeiting
	 * @param forced - <code>true</code> if the team was forced to forfeit, otherwise <code>false</code>
	 */
	public void onTeamForfeit(Tournament tournament, Team team, boolean forced)
	{
		
	}
	
	/**
	 * Runs when a {@link Team} wins a match.
	 * 
	 * @param tournament - The {@link Tournament} in which they won.
	 * @param team - The {@link Team} that won.
	 */
	public void onTeamWin(Tournament tournament, Team team)
	{
		;
	}

	/**
	 * Runs when a {@link Team} loses a match.
	 * 
	 * @param tournament - The {@link Tournament} in which they lost.
	 * @param team - The {@link Team} that lost.
	 */
	public void onTeamLose(Tournament tournament, Team team)
	{
		;
	}
	
	/**
	 * Runs when a round is beginning, after the matches have been calculated.
	 * 
	 * @param tournament - The {@link Tournament} within which the round is starting.
	 * @param matches - The round, as an immutable list of {@link Match}es.
	 */
	public void onRoundStart(Tournament tournament, ImmutableList<Match> matches)
	{
		;
	}
	
	/**
	 * Runs when a round ends.
	 * 
	 * @param tournament - The {@link Tournament} within which the round is ending.
	 */
	public void onRoundEnd(Tournament tournament)
	{
		;
	}
	
	/**
	 * Gets the text shown to a player as part of the summary of rules.
	 * 
	 * @return - The Text object.
	 */
	public Text getDisplayText()
	{
		return null;
	}
	
	/**
	 * Whether or not this will appear to everyone or only to those with permission 
	 * node <code>tournaments.commands.rules.modify</code>
	 * 
	 * @return true if everyone can see it, false if they require the modify node.
	 */
	public boolean visibleToAll()
	{
		return false;
	}
	
	/**
	 * Checks whether the given player is able to see this mode in rules/rewards. If the given argument is null and the display
	 * text isn't null, this will be guaranteed to be true. If the argument isn't null, the permission node 
	 * <code>tournaments.command.rules.modify</code> will be checked.
	 * 
	 * @return true if they can see this rule, false if they can't
	 */
	public boolean canShow(Player player)
	{
		return getDisplayText() != null && (visibleToAll() || player == null);
	}
}
