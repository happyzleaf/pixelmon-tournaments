package com.hiroku.tournaments.api.messages;

import java.util.ArrayList;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.api.rule.types.SideRule;
import com.hiroku.tournaments.api.rule.types.TeamRule;
import com.hiroku.tournaments.config.TournamentConfig;
import com.hiroku.tournaments.enums.EnumTournamentState;
import com.hiroku.tournaments.obj.MatchStartResult;
import com.hiroku.tournaments.obj.Side;
import com.hiroku.tournaments.obj.Team;

/**
 * Interface for objects that provide the messages of a tournament. All methods
 * default to usage of the configuration, {@link TournamentConfig}.
 * 
 * @author Hiroku
 */
public interface IMessageProvider
{
	/** Gets the default message provider which uses {@link TournamentConfig} */
	public static IMessageProvider getDefault()
	{
		return new DefaultMessageProvider();
	}
	
	static class DefaultMessageProvider implements IMessageProvider
	{
		;
	}
	
	default public String textToString(Text text)
	{
		return TextSerializers.FORMATTING_CODE.serialize(text);
	}
	
	default public Text stringToText(String string)
	{
		return TextSerializers.FORMATTING_CODE.deserialize(string);
	}
	
	default public Text getJoinMessage(Team team, boolean forced)
	{
		return stringToText(TournamentConfig.INSTANCE.joinMessage.replaceAll("\\{\\{team\\}\\}", textToString(team.getDisplayText())));
	}
	
	default public Text getLeaveMessage(Team team, boolean forced)
	{
		return stringToText(TournamentConfig.INSTANCE.leaveMessage.replaceAll("\\{\\{team\\}\\}", textToString(team.getDisplayText())));
	}
	
	default public Text getForfeitMessage(Team team, boolean forced)
	{
		return stringToText(TournamentConfig.INSTANCE.forfeitMessage.replaceAll("\\{\\{team\\}\\}", textToString(team.getDisplayText())));
	}

	default public Text getOpenMessage(Tournament tournament)
	{
		return stringToText(TournamentConfig.INSTANCE.openMessage);
	}
	
	default public Text getStartMessage(Tournament tournament)
	{
		return stringToText(TournamentConfig.INSTANCE.startMessage);
	}
	
	default public Text getClosedMessage(EnumTournamentState state)
	{
		if (state == EnumTournamentState.ACTIVE || state == EnumTournamentState.OPEN)
			return stringToText(TournamentConfig.INSTANCE.closeMessage);
		return null;
	}
	
	default public Text getNoWinnerMessage()
	{
		return stringToText(TournamentConfig.INSTANCE.noWinnerMessage);
	}
	
	default public Text getWinnerMessage(ArrayList<Team> winners)
	{
		Text.Builder builder = Text.builder();
		builder.append(winners.get(0).getDisplayText());
		for (int i = 1 ; i < winners.size() ; i++)
			builder.append(Text.of(TextColors.GOLD, ", ", winners.get(i).getDisplayText()));
		
		return stringToText(TournamentConfig.INSTANCE.winnerMessage.replaceAll("\\{\\{\\winners\\}\\}", textToString(builder.build())));
	}
	
	default public Text getMatchWinMessage(Side winningSide, Side losingSide)
	{
		return stringToText(TournamentConfig.INSTANCE.matchWinMessage
				.replaceAll("\\{\\{winners\\}\\}", textToString(winningSide.getDisplayText()))
				.replaceAll("\\{\\{losers\\}\\}", textToString(losingSide.getDisplayText())));
	}
	
	default public Text getMatchDrawMessage(Match match)
	{
		return stringToText(TournamentConfig.INSTANCE.matchDrawMessage
				.replaceAll("\\{\\{match\\}\\}", textToString(match.getDisplayText()))
				.replaceAll("\\{\\{time\\}\\}", TournamentConfig.INSTANCE.timeBeforeMatch + ""));
	}
	
	default public Text getMatchErrorMessage(Match match)
	{
		return stringToText(TournamentConfig.INSTANCE.matchErrorMessage
				.replaceAll("\\{\\{match\\}\\}", textToString(match.getDisplayText()))
				.replaceAll("\\{\\{time\\}\\}", TournamentConfig.INSTANCE.timeBeforeMatch + ""));
	}
	
	default public Text getUpcomingRoundMessage(int roundNum, ArrayList<Match> round)
	{
		Text.Builder builder = Text.builder();
		builder.append(round.get(0).getDisplayText());
		for (int i = 1 ; i < round.size() ; i++)
			builder.append(Text.of("\n", round.get(i).getDisplayText()));
		
		return stringToText(TournamentConfig.INSTANCE.upcomingRoundMessage
				.replaceAll("\\{\\{round\\}\\}", textToString(builder.build())));
	}
	
	default public Text getInsufficientPokemonMessage(Side side)
	{
		return stringToText(TournamentConfig.INSTANCE.insufficientPokemonMessage
				.replaceAll("\\{\\{side\\}\\}", textToString(side.getDisplayText())));
	}
	
	default public Text getPlayersOfflineMessage(Side side)
	{
		if (side.getNumPlayers(true) > 1)
			return stringToText(TournamentConfig.INSTANCE.offlinePlayerMessage
					.replaceAll("\\{\\{side\\}\\}", textToString(side.getDisplayText())));
		else
			return stringToText(TournamentConfig.INSTANCE.offlinePlayerMessage
					.replaceAll("\\{\\{side\\}\\}", textToString(side.getDisplayText())));
	}
	
	default public Text getRuleBreakMessage(MatchStartResult.RuleBroken ruleResult)
	{
		RuleBase rule = ruleResult.rule;
		Text breakMessage = null;
		if (rule instanceof PlayerRule)
			breakMessage = ((PlayerRule) rule).getBrokenRuleText(ruleResult.user.getPlayer().get());
		else if (rule instanceof TeamRule)
			breakMessage = ((TeamRule) rule).getBrokenRuleText(ruleResult.team);
		else if (rule instanceof SideRule)
			breakMessage = ((SideRule) rule).getBrokenRuleText(ruleResult.side);
		
		if (breakMessage == null)
			return null;
		
		return stringToText(TournamentConfig.INSTANCE.ruleBreakMessage
				.replaceAll("\\{\\{ruleerror\\}\\}", textToString(breakMessage)));
	}
	
	default public Text getBattleRuleBreakMessage(MatchStartResult.BattleRuleBroken ruleResult)
	{
		String battleRule = ruleResult.battleRule;
		return stringToText(TournamentConfig.INSTANCE.battleRuleBreakMessage
				.replaceAll("\\{\\{user\\}\\}", ruleResult.user.getName())
				.replaceAll("\\{\\{clause\\}\\}", battleRule));
	}
	
	default public Text getByeMessage()
	{
		return stringToText(TournamentConfig.INSTANCE.byeMessage);
	}
	
	default public Text getIgnorePromptMessage()
	{
		return stringToText(TournamentConfig.INSTANCE.ignorePromptMessage);
	}
	
	default public Text getIgnoreToggleMessage(boolean ignore)
	{
		if (ignore)
			return stringToText(TournamentConfig.INSTANCE.ignoreToggleOnMessage);
		else
			return stringToText(TournamentConfig.INSTANCE.ignoreToggleOffMessage);
	}
}