package com.hiroku.tournaments.commands;

import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.api.reward.RewardBase;
import com.hiroku.tournaments.api.reward.RewardTypeRegistrar;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tristate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RewardsCommand implements CommandExecutor {
	public static CommandSpec getSpec() {
		HashMap<String, Tristate> choices = new HashMap<String, Tristate>();
		choices.put("add", Tristate.TRUE);
		choices.put("remove", Tristate.FALSE);
		choices.put("test", Tristate.UNDEFINED);

		return CommandSpec.builder()
				.permission("tournaments.command.common.rewards")
				.executor(new RewardsCommand())
				.arguments(
						GenericArguments.optional(GenericArguments.choices(Text.of("option"), choices)),
						GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("rewardType[:argument]"))))
				.description(Text.of("Handles adding or removing rewards for a tournament"))
				.build();
	}


	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
		if (Tournament.instance() == null) {
			src.sendMessage(Text.of(TextColors.RED, "No tournament to change rewards for. Try /tournament create"));
			return CommandResult.empty();
		}

		if (!args.hasAny(Text.of("option"))) {
			Player player = src instanceof Player ? (Player) src : null;

			List<Text> contents = new ArrayList<>();
			for (RewardBase reward : Tournament.instance().rewards)
				if (reward.canShow(player))
					contents.add(Text.of(reward.getDisplayText()));
			PaginationList.Builder pagination = Sponge.getServiceManager().provide(PaginationService.class).get().builder();
			pagination.contents(contents)
					.padding(Text.of(TextColors.GOLD, "-"))
					.linesPerPage(10)
					.title(Text.of(TextColors.GOLD, "Rewards"));
			pagination.sendTo(src);
			return CommandResult.success();
		}

		if (!src.hasPermission("tournaments.command.common.rewards")) {
			src.sendMessage(Text.of(TextColors.RED, "You do not have permission to add, remove, or test rewards!"));
			return CommandResult.empty();
		}

		Tristate option = args.<Tristate>getOne(Text.of("option")).get();

		String key = null;
		String arg = null;

		if (!args.hasAny(Text.of("rewardType[:argument]")) && option != Tristate.UNDEFINED) {
			src.sendMessage(Text.of(TextColors.RED, "Missing argument. Usage: /tournaments rewards add/remove rewardType[:argument]"));
			return CommandResult.empty();
		}

		arg = args.<String>getOne(Text.of("rewardType[:argument]")).isPresent() ? args.<String>getOne(Text.of("rewardType[:argument]")).get() : null;

		if (arg != null && arg.contains(":")) {
			key = arg.split(":")[0];
			arg = arg.substring(arg.indexOf(":") + 1);
		} else if (arg != null && !arg.contains(":")) {
			key = arg;
			arg = "";
		}

		if (option == Tristate.TRUE) {
			try {
				Tournament.instance().rewards.add(RewardTypeRegistrar.parse(key, arg));
				src.sendMessage(Text.of(TextColors.DARK_GREEN, "Successfully added reward: ", TextColors.DARK_AQUA, key, ":", arg));
				return CommandResult.success();
			} catch (Exception e) {
				src.sendMessage(Text.of(TextColors.RED, "Error parsing reward: ", e.getMessage()));
				e.printStackTrace();
				return CommandResult.empty();
			}
		} else if (option == Tristate.FALSE) {
			Class<? extends RewardBase> rewardType = RewardTypeRegistrar.getRewardTypeForKey(key);
			if (rewardType == null) {
				src.sendMessage(Text.of(TextColors.RED, "Invalid reward type: ", key));
				return CommandResult.empty();
			}

			int removed = 0;
			for (int i = 0; i < Tournament.instance().rewards.size(); i++) {
				if (Tournament.instance().rewards.get(i).getClass() == rewardType) {
					Tournament.instance().rewards.remove(i);
					i--;
					removed++;
				}
			}

			if (removed == 0) {
				src.sendMessage(Text.of(TextColors.RED, "No rewards present of type: ", TextColors.DARK_AQUA, key));
				return CommandResult.empty();
			} else {
				src.sendMessage(Text.of(TextColors.DARK_GREEN, "Successfully removed all ", removed, " rule/s of type ", key));
				return CommandResult.success();
			}
		} else //Tristate is Tristate.UNDEFINED
		{
			Player target = null;

			if (!(src instanceof Player)) {
				if (arg == null) {
					src.sendMessage(Text.of(TextColors.RED, "Missing argument: player"));
					return CommandResult.empty();
				} else {
					if (Sponge.getServer().getPlayer(arg).isPresent())
						target = Sponge.getServer().getPlayer(arg).get();
					else {
						src.sendMessage(Text.of(TextColors.RED, "Invalid player: ", TextColors.DARK_AQUA, arg));
						return CommandResult.empty();
					}
				}
				return CommandResult.empty();
			} else {
				target = (Player) src;
			}

			if (target != null) {
				src.sendMessage(Text.of(TextColors.GRAY, "Giving ", TextColors.DARK_AQUA, target.getName(), TextColors.GRAY, " the current rewards..."));
				for (RewardBase reward : Tournament.instance().rewards)
					reward.give(target);
			}
			return CommandResult.success();
		}
	}
}
