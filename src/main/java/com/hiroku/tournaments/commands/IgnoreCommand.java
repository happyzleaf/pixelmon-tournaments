package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.User;
import com.happyzleaf.tournaments.text.Text;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.enums.TournamentStates;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;

public class IgnoreCommand implements Command<CommandSource> {
    public LiteralArgumentBuilder<CommandSource> create() {
        return Commands.literal("ignore")
//				.description(Text.of("Toggles tournament messages"))
                .requires(source -> User.hasPermission(source, "tournaments.command.common.ignore"))
                .executes(this)
                .then(Commands.literal("yes").executes(context -> ignore(context, true)))
                .then(Commands.literal("no").executes(context -> ignore(context, false)));
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        return ignore(context, null);
    }

    private int ignore(CommandContext<CommandSource> context, Boolean ignore) throws CommandSyntaxException {
        if (Tournament.instance() == null || Tournament.instance().state == TournamentStates.CLOSED) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "There's nothing to ignore; there's no tournament open"), false);
            return 0;
        }

        PlayerEntity player = context.getSource().asPlayer();
        ignore = ignore == null ? !Tournament.instance().ignoreList.contains(player.getUniqueID()) : ignore;

        if (ignore) {
            Tournament.instance().ignoreList.add(player.getUniqueID());
        } else {
            Tournament.instance().ignoreList.remove(player.getUniqueID());
        }

        context.getSource().sendFeedback(Tournament.instance().getMessageProvider().getIgnoreToggleMessage(ignore), false);
        return 1;
    }
}
