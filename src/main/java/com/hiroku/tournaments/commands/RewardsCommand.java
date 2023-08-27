package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.text.Text;
import com.happyzleaf.tournaments.User;
import com.happyzleaf.tournaments.args.ChoiceSetArgument;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.reward.RewardBase;
import com.hiroku.tournaments.api.reward.RewardTypeRegistrar;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.*;

import static com.hiroku.tournaments.util.CommandUtils.getOptArgument;

public class RewardsCommand implements Command<CommandSource> {
	private static final Set<String> CHOICES = new HashSet<>(Arrays.asList("add", "remove", "test"));

	public LiteralArgumentBuilder<CommandSource> create() {
		return Commands.literal("rewards")
//				.description(Text.of("Handles adding or removing rewards for a tournament"))
				.requires(source -> User.hasPermission(source, "tournaments.command.common.rewards"))
				.executes(this)
				.then(
						Commands.argument("choice", ChoiceSetArgument.choiceSet(CHOICES))
								.executes(this)
								.then(
										Commands.argument("rewardType[:argument]", StringArgumentType.greedyString())
												.executes(this)
								)
				);
	}

	@Override
	public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
		if (Tournament.instance() == null) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No tournament to change rewards for. Try /tournament create"), true);
			return 0;
		}

		String choice = getOptArgument(context, "choice", String.class).orElse(null);
		if (choice == null) {
			PlayerEntity player = context.getSource().getEntity() instanceof PlayerEntity ? context.getSource().asPlayer() : null;

			List<Text> contents = new ArrayList<>();
			for (RewardBase reward : Tournament.instance().rewards)
				if (reward.canShow(player))
					contents.add(Text.of(reward.getDisplayText()));
			// TODO: pagination
//			PaginationList.Builder pagination = Sponge.getServiceManager().provide(PaginationService.class).get().builder();
//			pagination.contents(contents)
//					.padding(Text.of(TextFormatting.GOLD, "-"))
//					.linesPerPage(10)
//					.title(Text.of(TextFormatting.GOLD, "Rewards"));
//			pagination.sendTo(src);
			return 1;
		}

		if (!User.hasPermission(context.getSource(), "tournaments.command.common.rewards")) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "You do not have permission to add, remove, or test rewards!"), true);
			return 0;
		}

		String rewardType = getOptArgument(context, "rewardType[:argument]", String.class).orElse(null);
		if (rewardType == null && choice.equals("add")) {
			context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Missing argument. Usage: /tournaments rewards add/remove rewardType[:argument]"), true);
			return 0;
		}

		String arg = rewardType;

		String key = null;
		if (arg != null && arg.contains(":")) {
			key = arg.split(":")[0];
			arg = arg.substring(arg.indexOf(":") + 1);
		} else if (arg != null && !arg.contains(":")) {
			key = arg;
			arg = "";
		}

		if (choice.equals("add")) {
			try {
				Tournament.instance().rewards.add(RewardTypeRegistrar.parse(key, arg));
				context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Successfully added reward: ", TextFormatting.DARK_AQUA, key, ":", arg), true);
				return 1;
			} catch (Exception e) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Error parsing reward: ", e.getMessage()), true);
				e.printStackTrace();
				return 1;
			}
		} else if (choice.equals("remove")) {
			Class<? extends RewardBase> rewardTypeClass = RewardTypeRegistrar.getRewardTypeForKey(key);
			if (rewardTypeClass == null) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Invalid reward type: ", key), true);
				return 0;
			}

			int removed = 0;
			for (int i = 0; i < Tournament.instance().rewards.size(); i++) {
				if (Tournament.instance().rewards.get(i).getClass() == rewardTypeClass) {
					Tournament.instance().rewards.remove(i);
					i--;
					removed++;
				}
			}

			if (removed == 0) {
				context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No rewards present of type: ", TextFormatting.DARK_AQUA, key), true);
				return 0;
			} else {
				context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Successfully removed all ", removed, " rule/s of type ", key), true);
				return 1;
			}
		} else if (choice.equals("test")) {
			PlayerEntity target;

			if (context.getSource().getEntity() instanceof PlayerEntity) {
				target = context.getSource().asPlayer();
			} else {
				if (arg == null) {
					context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Missing argument: player"), true);
					return 0;
				} else {
					target = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUsername(arg);
					if (target == null) {
						context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Invalid player: ", TextFormatting.DARK_AQUA, arg), true);
						return 0;
					}
				}
				return 0;
			}

			if (target != null) {
				context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Giving ", TextFormatting.DARK_AQUA, target.getName(), TextFormatting.GRAY, " the current rewards..."), true);
				for (RewardBase reward : Tournament.instance().rewards)
					reward.give(target);
			}
			return 1;
		}

		return 0;
	}
}
