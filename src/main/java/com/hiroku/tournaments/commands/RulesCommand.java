package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.Text;
import com.happyzleaf.tournaments.User;
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
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import static com.hiroku.tournaments.util.CommandUtils.getOptArgument;

public class RulesCommand implements Command<CommandSource> {
	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("rules")
//				.description(Text.of("Gets and sets rules"))
				.requires(source -> User.hasPermission(source, "tournaments.command.common.rules"))
				.executes(this)
				.then(
						Commands.argument("[add <rule-type:argument> | remove <rule-type> | test <player>]", StringArgumentType.greedyString())
								.executes(this)
				);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		boolean canModify = User.hasPermission(context.getSource(), "tournaments.command.admin.rules");

		if (Tournament.instance() == null) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No tournament."), true);
			return 0;
		}

		String[] argsArr = getOptArgument(context, "[add <rule-type:argument> | remove <rule-type> | test <player>]", String.class).map(s -> s.split(" ")).orElse(null);
		if (argsArr == null) {
			Tournament.instance().getRuleSet().showRules(context.getSource());
			return 1;
		}

		if (argsArr[0].equalsIgnoreCase("add") || argsArr[0].equalsIgnoreCase("remove")) {
			if (!canModify) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "'Stop right there'! You don't have permission to modify the rules!"), true);
				return 0;
			}

			if (argsArr.length < 2) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Not enough arguments. Missing: rule"), true);
				return 0;
			}

			if (argsArr[0].equalsIgnoreCase("add")) {
				for (int i = 1; i < argsArr.length; i++) {
					String[] splits = argsArr[i].split(":");
					try {
						String key = splits[0].replace(":", "");
						String arg = "";
						if (splits.length > 1) {
							arg = argsArr[i].substring(argsArr[i].indexOf(":") + 1);
						}

						RuleBase rule = RuleTypeRegistrar.parse(key, arg);
						if (rule != null) {
							if (Tournament.instance().getRuleSet().addRule(rule))
								context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Successfully added rule: ", argsArr[i]), true);
							else
								context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Unable to add rule"), true);
						}
					} catch (Exception e) {
						context.getSource().sendFeedback(Text.of(TextFormatting.RED, e.getMessage()), true);
					}
				}
				return 1;
			} else if (argsArr[0].equalsIgnoreCase("remove")) {
				for (int i = 1; i < argsArr.length; i++) {
					Class<? extends RuleBase> ruleType = RuleTypeRegistrar.getRuleTypeMatchingKey(argsArr[i]);
					if (ruleType == null) {
						context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Invalid rule type: ", TextFormatting.DARK_AQUA, argsArr[i]), true);
						return 0;
					}
					if (Tournament.instance().getRuleSet().rules.stream().noneMatch(ruleType::isInstance)) {
						context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No rules of type: ", TextFormatting.DARK_AQUA, argsArr[i], TextFormatting.RED, " are present in the tournament."), true);
						return 0;
					}

					Tournament.instance().getRuleSet().removeRuleType(ruleType);
					context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Successfully removed all rules of type: ", argsArr[i].toLowerCase()), true);
				}
				return 1;
			}
		} else if (argsArr[0].equalsIgnoreCase("test")) {
			if (argsArr.length == 1 && !(context.getSource().getEntity() instanceof PlayerEntity)) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Missing argument: player"), true);
			}
			PlayerEntity player = null;
			if (argsArr.length == 1) {
				player = context.getSource().asPlayer();
			} else {
				player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(argsArr[1]);
				if (player == null) {
					context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Invalid player: ", TextFormatting.DARK_AQUA, argsArr[1]), true);
					return 0;
				}
			}

			RuleSet ruleSet = Tournament.instance().getRuleSet();
			PlayerRule playerRule = ruleSet.getBrokenRule(player, StorageProxy.getParty(player.getUniqueID()));
			boolean brokeRule = false;

			if (playerRule != null) {
				context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Rule broken: ", playerRule.getBrokenRuleText(player)), true);
				brokeRule = true;
			}
			Team team = Tournament.instance().getTeam(player.getUniqueID());
			if (team != null) {
				TeamRule teamRule = ruleSet.getBrokenRule(team);
				if (teamRule != null) {
					context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Rule broken: ", teamRule.getBrokenRuleText(team)), true);
					brokeRule = true;
				}
				Match match = Tournament.instance().getMatch(team);
				if (match != null) {
					Side side = match.getSide(team);
					SideRule sideRule = ruleSet.getBrokenRule(side);
					if (sideRule != null) {
						context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Rule broken: ", sideRule.getBrokenRuleText(side)), true);
						brokeRule = true;
					}
				}
			}

			if (brokeRule)
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "FAIL."), true);
			else
				context.getSource().sendFeedback(Text.of(TextFormatting.GREEN, "PASS."), true);
		}
		return 0;
	}
}
