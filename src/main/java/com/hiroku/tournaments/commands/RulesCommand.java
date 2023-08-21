package com.hiroku.tournaments.commands;

import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.rule.RuleSet;
import com.hiroku.tournaments.api.rule.RuleTypeRegistrar;
import com.hiroku.tournaments.api.rule.types.PlayerRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.api.rule.types.SideRule;
import com.hiroku.tournaments.api.rule.types.TeamRule;
import com.hiroku.tournaments.obj.Side;
import com.hiroku.tournaments.obj.Team;
import com.pixelmonmod.pixelmon.Pixelmon;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class RulesCommand implements CommandExecutor
{
	public static CommandSpec getSpec()
	{
		return CommandSpec.builder()
				.description(Text.of("Gets and sets rules"))
				.permission("tournaments.command.common.rules")
				.arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("[add <rule-type:argument> | remove <rule-type> | test <player>]"))))
				.executor(new RulesCommand())
				.build();
	}

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException
	{
		boolean canModify = src.hasPermission("tournaments.command.admin.rules");

		if (Tournament.instance() == null)
		{
			src.sendMessage(Text.of(TextColors.RED, "No tournament."));
			return CommandResult.empty();
		}

		if (!args.hasAny(Text.of("[add <rule-type:argument> | remove <rule-type> | test <player>]")))
		{
			Tournament.instance().getRuleSet().showRules(src);
			return CommandResult.success();
		}

		String[] argsArr = args.<String>getOne(Text.of("[add <rule-type:argument> | remove <rule-type> | test <player>]")).get().split(" ");
		if (argsArr[0].toLowerCase().equals("add") || argsArr[0].toLowerCase().equals("remove"))
		{
			if (!canModify)
			{
				src.sendMessage(Text.of(TextColors.RED, "'Stop right there'! You don't have permission to modify the rules!"));
				return CommandResult.empty();
			}

			if (argsArr.length < 2)
			{
				src.sendMessage(Text.of(TextColors.RED, "Not enough arguments. Missing: rule"));
				return CommandResult.empty();
			}

			if (argsArr[0].toLowerCase().equals("add"))
			{
				for (int i = 1 ; i < argsArr.length ; i++)
				{
					String[] splits = argsArr[i].split(":");
					try
					{
						String key = splits[0].replace(":", "");
						String arg = "";
						if (splits.length > 1)
						{
							arg = argsArr[i].substring(argsArr[i].indexOf(":") + 1);
						}
						
						RuleBase rule = RuleTypeRegistrar.parse(key, arg);
						if (rule != null)
						{
							if (Tournament.instance().getRuleSet().addRule(rule))
								src.sendMessage(Text.of(TextColors.DARK_GREEN, "Successfully added rule: ", argsArr[i]));
							else
								src.sendMessage(Text.of(TextColors.RED, "Unable to add rule"));
						}
					}
					catch (Exception e)
					{
						src.sendMessage(Text.of(TextColors.RED, e.getMessage()));
					}
				}
				return CommandResult.success();
			}
			else if (argsArr[0].toLowerCase().equals("remove"))
			{
				for (int i = 1 ; i < argsArr.length ; i++)
				{
					Class<? extends RuleBase> ruleType = RuleTypeRegistrar.getRuleTypeMatchingKey(argsArr[i]);
					if (ruleType == null)
					{
						src.sendMessage(Text.of(TextColors.RED, "Invalid rule type: ", TextColors.DARK_AQUA, argsArr[i]));
						return CommandResult.empty();
					}
					if (Tournament.instance().getRuleSet().rules.stream().noneMatch(rule -> ruleType.isInstance(rule)))
					{
						src.sendMessage(Text.of(TextColors.RED, "No rules of type: ", TextColors.DARK_AQUA, argsArr[i], TextColors.RED, " are present in the tournament."));
						return CommandResult.empty();
					}

					Tournament.instance().getRuleSet().removeRuleType(ruleType);
					src.sendMessage(Text.of(TextColors.DARK_GREEN, "Successfully removed all rules of type: ", argsArr[i].toLowerCase()));
				}
				return CommandResult.success();
			}
		}
		else if (argsArr[0].toLowerCase().equals("test"))
		{
			if (argsArr.length == 1 && !(src instanceof Player))
			{
				src.sendMessage(Text.of(TextColors.RED, "Missing argument: player"));
			}
			Player player = null;
			if (argsArr.length == 1)
				player = (Player)src;
			else
			{
				Optional<Player> optPlayer = Sponge.getServer().getPlayer(argsArr[1]);
				if (optPlayer.isPresent())
					player = optPlayer.get();
				else
				{
					src.sendMessage(Text.of(TextColors.RED, "Invalid player: ", TextColors.DARK_AQUA, argsArr[1]));
					return CommandResult.empty();
				}
			}
			
			RuleSet ruleSet = Tournament.instance().getRuleSet();
			PlayerRule playerRule = ruleSet.getBrokenRule(player, Pixelmon.storageManager.getParty(player.getUniqueId()));
			boolean brokeRule = false;
			
			if (playerRule != null)
			{
				src.sendMessage(Text.of(TextColors.GRAY, "Rule broken: ", playerRule.getBrokenRuleText(player)));
				brokeRule = true;
			}
			Team team = Tournament.instance().getTeam(player.getUniqueId());
			if (team != null)
			{
				TeamRule teamRule = ruleSet.getBrokenRule(team);
				if (teamRule != null)
				{
					src.sendMessage(Text.of(TextColors.GRAY, "Rule broken: ", teamRule.getBrokenRuleText(team)));
					brokeRule = true;
				}
				Match match = Tournament.instance().getMatch(team);
				if (match != null)
				{
					Side side = match.getSide(team);
					SideRule sideRule = ruleSet.getBrokenRule(side);
					if (sideRule != null)
					{
						src.sendMessage(Text.of(TextColors.GRAY, "Rule broken: ", sideRule.getBrokenRuleText(side)));
						brokeRule = true;
					}
				}
			}
			
			if (brokeRule)
				src.sendMessage(Text.of(TextColors.RED, "FAIL."));
			else
				src.sendMessage(Text.of(TextColors.GREEN, "PASS."));
		}
		return CommandResult.empty();
	}
}
