package com.hiroku.tournaments.api.rule;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import com.hiroku.tournaments.api.Mode;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.rule.types.DeciderRule;
import com.hiroku.tournaments.api.rule.types.GeneralRule;
import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.api.rule.types.SideRule;
import com.hiroku.tournaments.api.rule.types.TeamRule;
import com.hiroku.tournaments.obj.Side;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.rules.general.EloType;
import com.hiroku.tournaments.rules.player.Healing;
import com.hiroku.tournaments.util.TournamentUtils;
import com.pixelmonmod.pixelmon.battles.rules.BattleRules;
import com.pixelmonmod.pixelmon.storage.PlayerPartyStorage;

/**
 * Object representing a {@link Tournament}'s rules.
 * 
 * @author Hiroku
 */
public class RuleSet
{
	/** The Pixelmon battle rules. */
	public BattleRules br = new BattleRules();
	/** The rules contained within this {@link RuleSet}.*/
	public final ArrayList<RuleBase> rules = new ArrayList<RuleBase>();
	
	/**
	 * Creates a RuleSet based off an array of various rule arguments. E.g. {"levelmin:50" , "levelmax:100"}
	 *
	 * @param ruleStrings - An array of various rule arguments. E.g. {"levelmin:50", "levelmax:100"}
	 */
	public RuleSet(String... ruleStrings)
	{
		this(ruleStrings, null);
	}
	
	/**
	 * Creates a {@link RuleSet} based on an array of various rule arguments e.g. {"levelmin:50" , "levelmax:100"} with a 
	 * {@link CommandSource} to refer errors to.
	 *
	 * @param ruleStrings - An array of various rule arguments. E.g. {"levelmin:50", "levelmax:100"}
	 * @param src - A {@link CommandSource} to refer errors to. (null if not relevant)
	 */
	public RuleSet(String[] ruleStrings, CommandSource src)
	{
		for (String ruleString : ruleStrings)
		{
			try
			{
				addRule(RuleTypeRegistrar.parse(ruleString.split(":")[0], ruleString.substring(ruleString.indexOf(":") + 1)));
			}
			catch (Exception e)
			{
				if (src != null)
					src.sendMessage(Text.of(TextColors.RED, "Error with rule: ", ruleString, " ", e.getMessage()));
			}
		}
	}
	
	/**
	 * Adds a rule to the list, provided it is not null. Unacceptable duplicates will remove the existing.
	 * 
	 * @param rule - The rule being added. Nulls will be ignored, unacceptable duplicates will cause the existing to
	 * 				 be removed.
	 */
	public boolean addRule(RuleBase rule)
	{
		if (rule == null)
			return false;
		boolean canAdd = true;
		// This may be null if a RuleSet is being created in the create command, or programmatically.
		if (Tournament.instance() != null)
			for (Mode mode : Tournament.instance().getModes())
				if (!mode.canRuleBeAdded(Tournament.instance(), rule))
					canAdd = false;
		if (!canAdd)
			return false;
		RuleBase existing = null;
		for (RuleBase existingRule : rules)
			if (existingRule.getClass() == rule.getClass() && !existingRule.duplicateAllowed(rule))
				existing = existingRule;
		if (existing != null)
			rules.remove(existing);
		return rules.add(rule);
	}

	/**
	 * Removes a specific rule.
	 * 
	 * @param rule - The rule to be deleted.
	 */
	public void removeRule(RuleBase rule)
	{
		boolean canRemove = true;
		for (Mode mode : Tournament.instance().getModes())
			if (!mode.canRuleBeRemoved(Tournament.instance(), rule))
				canRemove = false;
		if (canRemove)
			rules.remove(rule);
	}
	
	/**
	 * Gets all the {@link RuleBase}s of the given type in the set.
	 * 
	 * @return - A list of all the {@link RuleBase}s of this class in the set. This list may be empty.
	 */
	public <T extends RuleBase> List<T> getRules(Class<T> clazz)
	{
		ArrayList<T> matchingRules = new ArrayList<T>();
		for (RuleBase rule : rules)
		{
			try
			{
				matchingRules.add(clazz.cast(rule));
			}
			catch (ClassCastException cce) { ; }
		}
		return matchingRules;
	}
	
	/**
	 * Gets the first {@link RuleBase} for the given type.
	 * 
	 * @param  - The class of {@link RuleBase} to check for.
	 * 
	 * @return - The first {@link RuleBase} with the same class as the given, or null if no 
	 * 			 rule of this type exists in the set.
	 */
	public <T extends RuleBase> T getRule(Class<T> clazz)
	{
		for (RuleBase rule : rules)
		{
			try
			{
				return clazz.cast(rule);
			}
			catch (ClassCastException cce) { ; }
		}
		return null;
	}
	
	/**
	 * Removes all rules of a particular type.
	 * 
	 * @param ruleType - The type of rules to remove.
	 */
	public void removeRuleType(Class<? extends RuleBase> ruleType)
	{
		ArrayList<RuleBase> matchingRules = new ArrayList<RuleBase>();
		for (RuleBase rule : rules)
		{
			try
			{
				ruleType.cast(rule);
				matchingRules.add(rule);
			}
			catch (ClassCastException cce) { ; }
		}
		
		for (RuleBase rule : matchingRules)
		{
			boolean canRemove = true;
			for (Mode mode : Tournament.instance().getModes()) {
				if (!mode.canRuleBeRemoved(Tournament.instance(), rule)) {
					canRemove = false;
				}
			}
			if (canRemove) {
				rules.remove(rule);
			}
		}
	}
	
	/**
	 * Gets the rule that the given {@link Player} currently breaks (if they are breaking a rule) given the {@link }
	 * and {@link Team}.
	 * 
	 * @param player - The {@link Player} being examined
	 * @param storage - The {@link } for the player (for convenience).
	 * 
	 * @return - The {@link PlayerRule} for the broken rule, or null if they pass all the rules.
	 */
	public PlayerRule getBrokenRule(Player player, PlayerPartyStorage storage)
	{
		for (PlayerRule rule : this.<PlayerRule>getRules(PlayerRule.class))
			if (!rule.passes(player, storage))
				return rule;
		return null;
	}
	
	/**
	 * Gets the rule that the given {@link Team} currently breaks (if they are breaking a rule).
	 * 
	 * @param team - The {@link Team} being checked.
	 * 
	 * @return - The {@link TeamRule} for the broken rule, or null if they pass all the rules.
	 */
	public TeamRule getBrokenRule(Team team)
	{
		for (TeamRule rule : this.<TeamRule>getRules(TeamRule.class))
			if (!rule.passes(team))
				return rule;
		return null;
	}
	
	/**
	 * Gets the rule that the given {@link Side} currently breaks (if they are breaking a rule).
	 * 
	 * @param side - The {@link } being checked.
	 * 
	 * @return - The {@link SideRule} for the broken rule, or null if they pass all the rules.
	 */
	public SideRule getBrokenRule(Side side)
	{
		for (SideRule rule : this.<SideRule>getRules(SideRule.class)) {
			if (!rule.passes(side))
				return rule;
		}
		return null;
	}
	
	/**
	 * Gets the rules that will apply to deciding draws in order of weight.
	 * 
	 * @return - A list of {@link DeciderRule}s that apply to draws.
	 */
	public ArrayList<DeciderRule> getDrawDeciderRules()
	{
		ArrayList<DeciderRule> deciders = new ArrayList<DeciderRule>();
		for (DeciderRule rule : this.<DeciderRule>getRules(DeciderRule.class))
			if (rule.applyToDraws())
				deciders.add(rule);
		
		return TournamentUtils.getOrderedDeciderRules(deciders);
	}
	
	/**
	 * Gets the rules that will apply to deciding crash/error battles in descending order of weight.
	 * 
	 * @return - A list of {@link DeciderRule}s that apply to crash/error battles in descending order of weight.
	 */
	public ArrayList<DeciderRule> getCrashDeciderRules()
	{
		ArrayList<DeciderRule> deciders = new ArrayList<DeciderRule>();
		for (DeciderRule rule : this.<DeciderRule>getRules(DeciderRule.class))
			if (rule.applyToCrashes())
				deciders.add(rule);
		
		return TournamentUtils.getOrderedDeciderRules(deciders);
	}
	
	/**
	 * Helper function to spare me the agony of checking repeatedly for whether players are allowed healing in this rule set
	 * 
	 * @return - true if they are allowed to heal, false otherwise.
	 */
	public boolean healingAllowed()
	{
		for (RuleBase rule : rules)
			if (rule instanceof Healing)
				return ((Healing)rule).healingAllowed;
		return true;
	}
	
	/**
	 * Helper function to check quickly if a tournament is going to be updating Elo values.
	 * 
	 * @return true if Elo values will be updated, otherwise false.
	 */
	public boolean isElo()
	{
		return this.getRule(EloType.class) != null;
	}
	
	/**
	 * Shows the rules of the tournament to a {@link CommandSource}.
	 * 
	 * @param  - The {@link MessageReceiver}s (normally just {@link Player}s) that the rules will be shown to.
	 */
	public void showRules(CommandSource src)
	{
		//Order: General rules, 
		//       descending weight draw deciding rules, 
		//       descending weight crash deciding rules, 
		//       player/team/side rules.
		
		ArrayList<Text> contents = new ArrayList<Text>();
		
		Player player = src instanceof Player ? (Player)src : null;
		
		for (RuleBase rule : rules)
			if (rule instanceof GeneralRule && rule.canShow(player))
				contents.add(rule.getDisplayText());
		for (DeciderRule rule : TournamentUtils.getOrderedDeciderRules(this.getDrawDeciderRules()))
			if (rule.canShow(player))
				contents.add(rule.getDisplayText());
		for (DeciderRule rule : TournamentUtils.getOrderedDeciderRules(this.getCrashDeciderRules()))
			if (rule.canShow(player))
				contents.add(rule.getDisplayText());
		for (RuleBase rule : rules)
			if ((rule instanceof PlayerRule || rule instanceof TeamRule || rule instanceof SideRule) && rule.canShow(player))
				contents.add(rule.getDisplayText());
		
		PaginationList.Builder pagination = Sponge.getServiceManager().provide(PaginationService.class).get().builder();
		pagination.contents(contents)
			.padding(Text.of(TextColors.GOLD, "-"))
			.linesPerPage(10)
			.title(Text.of(TextColors.GOLD, "Rules"));
		pagination.sendTo(src);
	}
	
	/** 
	 * Gets this {@link RuleSet} as a multi-line {@link Text}. This is for usage in preset hover text but may be used elsewhere.
	 * The {@link Text} this returns will have no header, but will instead be the series of rules in their display text form.
	 */
	public Text getDisplayText()
	{
		Text.Builder builder = Text.builder();
		builder.append(Text.of(TextColors.GOLD, TextStyles.UNDERLINE, "Rules:\n"));
		if (!rules.isEmpty() && rules.get(0).getDisplayText() != null)
			builder.append(rules.get(0).getDisplayText());
		else if (rules.isEmpty())
			builder.append(Text.of(TextColors.RED, "None."));
		for (int i = 1 ; i < rules.size() ; i++)
			if (rules.get(i).getDisplayText() != null)
				builder.append(Text.of("\n", rules.get(i).getDisplayText()));
		return builder.build();
	}
}