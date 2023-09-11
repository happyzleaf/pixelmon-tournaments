package com.hiroku.tournaments.commands;

import com.happyzleaf.tournaments.User;
import com.happyzleaf.tournaments.text.Text;
import com.hiroku.tournaments.api.Tournament;
import com.hiroku.tournaments.enums.TournamentStates;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.rules.player.RandomPokemon;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TextFormatting;

/**
 * Base command executor for all tournament commands
 *
 * @author Hiroku
 */
public class TournamentCommand implements Command<CommandSource> {
    public LiteralArgumentBuilder<CommandSource> create() {
        return Commands.literal("tournament")
//				.description(Text.of("Base tournament command"))
                .requires(source -> User.hasPermission(source, "tournaments.command.common.tournament"))
                .executes(this)
                .then(
                        Commands.literal("open")
                                .requires(source -> User.hasPermission(source, "tournaments.command.admin.tournament"))
                                .executes(this::open)
                )
                .then(
                        Commands.literal("close")
                                .requires(source -> User.hasPermission(source, "tournaments.command.admin.tournament"))
                                .executes(this::close)
                )
                .then(
                        Commands.literal("start")
                                .requires(source -> User.hasPermission(source, "tournaments.command.admin.tournament"))
                                .executes(this::start)
                )
                .then(new ReloadCommand().create())
                .then(new RerollCommand().create())
                .then(new FlagCommand().create())
                .then(new PresetsCommand().create())
                .then(new RewardsCommand().create())
                .then(new LeaveCommand().create())
                .then(new ForfeitCommand().create())
                .then(new ZoneCommand().create())
                .then(new CreateCommand().create())
                .then(new JoinCommand().create())
                .then(new RulesCommand().create())
                .then(new IgnoreCommand().create())
                .then(new BattleRulesCommand().create());
    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (Tournament.instance() == null) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "No tournament"), false);
            return 0;
        }

        Tournament.instance().showTournament(context.getSource());
        return 1;
    }

    private int open(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (Tournament.instance() == null) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "There is no tournament to open. Try /tournament create"), false);
            return 0;
        } else if (Tournament.instance().state == TournamentStates.OPEN) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "The tournament is already open. Pay attention."), false);
            return 0;
        } else if (Tournament.instance().state == TournamentStates.ACTIVE) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "I mean, the tournament is up and running, so dunno what you're trying to do"), false);
            return 0;
        }

        Tournament.instance().open();
        return 1;
    }

    private int close(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (Tournament.instance() == null) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "It's a tad difficult to close a tournament that doesn't exist"), false);
            return 0;
        }

        if (Tournament.instance().teams != null) {
            for (Team team : Tournament.instance().teams) {
                for (User user : team.users) {
                    RandomPokemon.removeRentalPokemon(user, true);
                }
            }
        }

        Tournament.instance().close();
        context.getSource().sendFeedback(Text.of(TextFormatting.GRAY, "Tournament closed."), false);
        return 1;
    }

    private int start(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (Tournament.instance() == null || Tournament.instance().state == TournamentStates.CLOSED) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "There is no open tournament to start. Have you tried /tournament open?"), false);
            return 0;
        } else if (Tournament.instance().state == TournamentStates.ACTIVE) {
            context.getSource().sendFeedback(Text.of(TextFormatting.RED, "A tournament is already active. Close it if you want to make a new one."), false);
            return 0;
        }

        Tournament.instance().start();
        return 1;
    }
}