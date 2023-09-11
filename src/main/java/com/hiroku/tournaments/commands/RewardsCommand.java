package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.User;
import com.happyzleaf.tournaments.text.Pagination;
import com.happyzleaf.tournaments.text.Text;
import com.hiroku.tournaments.api.Mode;
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
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

import java.util.stream.Collectors;

import static com.hiroku.tournaments.util.CommandUtils.getOptPlayer;

public class RewardsCommand implements Command<CommandSource> {
    public LiteralArgumentBuilder<CommandSource> create() {
        return Commands.literal("rewards")
//				.description(Text.of("Handles adding or removing rewards for a tournament"))
                .requires(source -> User.hasPermission(source, "tournaments.command.common.rewards"))
                .executes(this)
                .then(
                        Commands.literal("add")
                                .requires(source -> User.hasPermission(source, "tournaments.command.common.rewards"))
                                .then(
                                        Commands.argument("reward", StringArgumentType.greedyString())
                                                .executes(this::add)
                                )
                )
                .then(
                        Commands.literal("remove")
                                .requires(source -> User.hasPermission(source, "tournaments.command.common.rewards"))
                                .then(
                                        Commands.argument("reward", StringArgumentType.greedyString())
                                                .executes(this::remove)
                                )
                )
                .then(
                        Commands.literal("test")
                                .requires(source -> User.hasPermission(source, "tournaments.command.common.rewards"))
                                .executes(this::test)
                                .then(
                                        Commands.argument("player", EntityArgument.player())
                                                .executes(this::test)
                                )
                );
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (Tournament.instance() == null) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No tournament to change rewards for. Try /tournament create"), false);
            return 0;
        }

        PlayerEntity player = context.getSource().getEntity() instanceof PlayerEntity ? context.getSource().asPlayer() : null;
        Pagination.builder()
                .title(Text.of(TextFormatting.GOLD, "Rewards"))
                .padding(Text.of(TextFormatting.GOLD, "-"))
                .linesPerPage(10)
                .contents(Tournament.instance().rewards.stream().filter(r -> r.canShow(player)).map(Mode::getDisplayText).collect(Collectors.toList()))
                .sendTo(context.getSource());
        return 1;
    }

    private int add(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (Tournament.instance() == null) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No tournament to change rewards for. Try /tournament create"), false);
            return 0;
        }

        String[] args = context.getArgument("reward", String.class).split(":", 2);
        String key = args[0];
        String value = args.length > 1 ? args[1] : "";

        try {
            Tournament.instance().rewards.add(RewardTypeRegistrar.parse(key, value));
            context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Successfully added reward: ", TextFormatting.DARK_AQUA, key, ":", value), false);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Error parsing reward: ", e.getMessage()), false);
            e.printStackTrace();
            return 1;
        }
    }

    private int remove(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (Tournament.instance() == null) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No tournament to change rewards for. Try /tournament create"), false);
            return 0;
        }

        String key = context.getArgument("reward", String.class).split(":", 2)[0];

        Class<? extends RewardBase> rewardTypeClass = RewardTypeRegistrar.getRewardTypeForKey(key);
        if (rewardTypeClass == null) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Invalid reward type: ", key), false);
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
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No rewards present of type: ", TextFormatting.DARK_AQUA, key), false);
            return 0;
        }

        context.getSource().sendFeedback(Text.of(TextFormatting.DARK_GREEN, "Successfully removed all ", removed, " rule/s of type ", key), false);
        return 1;
    }

    private int test(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (Tournament.instance() == null) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No tournament to change rewards for. Try /tournament create"), false);
            return 0;
        }

        PlayerEntity target = getOptPlayer(context, "player").orElse(null);
        if (target == null) {
            if (!(context.getSource().getEntity() instanceof PlayerEntity)) {
                context.getSource().sendFeedback(Text.of(TextFormatting.RED, "Missing argument: player"), false);
                return 0;
            }

            target = context.getSource().asPlayer();
        }

        context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Giving ", TextFormatting.DARK_AQUA, target.getName(), TextFormatting.GRAY, " the current rewards..."), false);
        for (RewardBase reward : Tournament.instance().rewards) {
            reward.give(target);
        }

        return 1;
    }
}
