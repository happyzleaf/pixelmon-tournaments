package com.hiroku.tournaments.api;

import com.google.common.collect.ImmutableList;
import com.happyzleaf.tournaments.Scheduler;
import com.happyzleaf.tournaments.User;
import com.happyzleaf.tournaments.text.Pagination;
import com.happyzleaf.tournaments.text.Text;
import com.hiroku.tournaments.Tournaments;
import com.hiroku.tournaments.Zones;
import com.hiroku.tournaments.api.archetypes.pokemon.PokemonMatch;
import com.hiroku.tournaments.api.events.match.MatchEndEvent;
import com.hiroku.tournaments.api.events.round.RoundStartEvent;
import com.hiroku.tournaments.api.events.tournament.*;
import com.hiroku.tournaments.api.messages.IMessageProvider;
import com.hiroku.tournaments.api.reward.RewardBase;
import com.hiroku.tournaments.api.rule.RuleSet;
import com.hiroku.tournaments.api.rule.types.DeciderRule;
import com.hiroku.tournaments.api.rule.types.RuleBase;
import com.hiroku.tournaments.config.TournamentConfig;
import com.hiroku.tournaments.elo.EloMatch;
import com.hiroku.tournaments.elo.EloStorage;
import com.hiroku.tournaments.enums.TournamentStates;
import com.hiroku.tournaments.obj.Side;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.obj.Zone;
import com.hiroku.tournaments.rules.general.EloType;
import com.hiroku.tournaments.util.TournamentUtils;
import com.pixelmonmod.pixelmon.api.util.helpers.RandomHelper;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRuleRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.PropertyValue;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Object representation of a tournament. Various of the contained functions can be
 * retracted or modified by rules and rewards and other {@link Mode} implementations.
 *
 * @author Hiroku
 */
public class Tournament extends Mode {
    /**
     * The instance of the Tournament.
     */
    private static Tournament INSTANCE = null;

    /**
     * The type of {@link Match} to be instantiating in the standard match-maker.
     */
    public Class<? extends Match> matchClass = PokemonMatch.class;
    /**
     * The state of the tournament as a member of {@link TournamentStates}.
     */
    public TournamentStates state = TournamentStates.CLOSED;
    /**
     * The implementation of {@link IMessageProvider} which provides all global messages.
     */
    private IMessageProvider messageProvider = IMessageProvider.getDefault();
    /**
     * The prefix for all global tournament messages.
     */
    private Text prefix = Text.deserialize(TournamentConfig.INSTANCE.prefix);

    /**
     * The {@link RuleSet} specification of all {@link RuleBase}s relevant to this tournament.
     */
    private RuleSet ruleSet = new RuleSet();
    /**
     * The list of {@link RewardBase}s to be given to the winner/s of the Tournament.
     */
    public final List<RewardBase> rewards = new ArrayList<>();
    /**
     * Extra modes that will not display anywhere (for programmers only)
     */
    public List<Mode> extraModes = new ArrayList<>();

    /**
     * The {@link Team}s of the tournament.
     */
    public List<Team> teams = new ArrayList<>();
    /**
     * The {@link Match} list to be executed in the current round.
     */
    public List<Match> round = new ArrayList<>();
    /**
     * The list of UUIDs representing players who don't want any tournament messages.
     */
    public List<UUID> ignoreList = new ArrayList<>();

    /**
     * Which round it is. The first round is 1, etc.
     */
    public int roundNum = 0;

    public List<UUID> tasks = new ArrayList<>();

    /**
     * Creates a new tournament with an immediate specification of some of its rules.
     *
     * @param ruleSet - The specific rules to add for a start.
     */
    public Tournament(RuleSet ruleSet) {
        INSTANCE = this;
        setRuleSet(ruleSet);
        Zones.INSTANCE.clear();
    }

    /**
     * Creates a new tournament without any rules.
     */
    public Tournament() {
        this(new RuleSet());
    }

    public static Tournament instance() {
        return INSTANCE;
    }

    /**
     * Gets the {@link IMessageProvider} for this {@link Tournament}.
     */
    public IMessageProvider getMessageProvider() {
        return messageProvider;
    }

    /**
     * Sets the {@link IMessageProvider} for the tournament. Null is rejected.
     */
    public void setMessageProvider(IMessageProvider messageProvider) {
        if (messageProvider != null)
            this.messageProvider = messageProvider;
    }

    /**
     * Gets the prefix of all global tournament messages
     */
    public Text getPrefix() {
        return prefix;
    }

    /**
     * Sets the tournament message prefix
     */
    public void setPrefix(Text prefix) {
        if (prefix != null)
            this.prefix = prefix;
    }

    /**
     * Sends the given {@link Text} to all players that aren't ignoring the tournament.
     */
    public void sendMessage(Text text) {
        if (text == null || text.toPlain().equals("")) {
            return;
        }

        ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().stream()
                .filter(p -> !ignoreList.contains(p.getUniqueID()))
                .forEach(p -> p.sendMessage(Text.of(getPrefix(), text), Util.DUMMY_UUID));
    }

    /**
     * Gets the {@link RuleSet} for the tournament.
     */
    public RuleSet getRuleSet() {
        return this.ruleSet;
    }

    /**
     * Sets the {@link RuleSet} of the tournament. Nulls are ignored.
     */
    public void setRuleSet(RuleSet ruleSet) {
        if (ruleSet != null)
            this.ruleSet = ruleSet;
    }

    /**
     * Adds the given {@link Team}s.
     *
     * @param forced - Whether the {@link Team}/s were forcibly added (true) or did so of their own volition (false)
     * @param teams  - The {@link Team}/s being added
     */
    public void addTeams(boolean forced, Team... teams) {
        for (Team team : teams) {
            boolean canAdd = true;
            for (Mode mode : getModes())
                if (!mode.canTeamJoin(this, team, forced))
                    canAdd = false;

            if (canAdd) {
                if (Tournaments.EVENT_BUS.post(new JoinTournamentEvent(team, forced)))
                    continue;

                this.teams.add(team);
                sendMessage(messageProvider.getJoinMessage(team, forced));
                getModes().forEach(mode -> mode.onTeamJoin(this, team, forced));
            }
        }
    }

    /**
     * Adds the given {@link Team}/s and assumes they were forcefully added.
     */
    public void addTeams(Team... teams) {
        addTeams(true, teams);
    }

    /**
     * Forfeits the given {@link Team}s.
     *
     * @param forced - Whether the {@link Team}/s were forcibly removed (true) or did so of their own volition (false)
     * @param teams  - The {@link Team}/s being removed
     */
    public void removeTeams(boolean forced, Team... teams) {
        for (Team team : teams) {
            if (Tournaments.EVENT_BUS.post(new LeaveTournamentEvent(team, forced)))
                continue;

            boolean canRemove = true;
            for (Mode mode : getModes())
                if (!mode.canTeamLeave(this, team, forced))
                    canRemove = false;

            if (canRemove) {
                this.teams.remove(team);
                getModes().forEach(mode -> mode.onTeamLeave(this, team, forced));
                sendMessage(messageProvider.getLeaveMessage(team, forced));
            }
        }
    }

    /**
     * Removes the given {@link Team}/s and assumes they were forcefully removed.
     */
    public void removeTeams(Team... teams) {
        removeTeams(true, teams);
    }

    /**
     * Forfeits the given {@link Team}s.
     *
     * @param forced - Whether the {@link Team}/s were forcibly forfeited (true) or did so of their own volition (false)
     * @param teams  - The {@link Team}/s being forfeited
     */
    public void forfeitTeams(boolean forced, Team... teams) {
        for (Team team : teams) {
            if (Tournaments.EVENT_BUS.post(new ForfeitTournamentEvent(team, forced)))
                continue;
            sendMessage(messageProvider.getForfeitMessage(team, forced));
            team.alive = false;
            getModes().forEach(mode -> mode.onTeamForfeit(this, team, forced));

            Match match = this.getMatch(team);
            if (match != null) {
                Side side = match.getSide(team);
                match.forceEnd();
                this.matchEnds(match, match.getOtherSide(side), side);
            }
        }
    }

    /**
     * Forfeits the given {@link Team}/s and assumes they were forcefully forfeited.
     */
    public void forfeitTeams(Team... teams) {
        forfeitTeams(true, teams);
    }

    /**
     * Gets the {@link Team} containing the given UUID, if one exists
     */
    public Team getTeam(UUID uuid) {
        for (Team team : teams)
            if (team.hasUser(uuid))
                return team;
        return null;
    }

    /**
     * Gets the {@link Match} for the given UUID, if one exists.
     */
    public Match getMatch(UUID uuid) {
        for (Match match : round)
            if (match.getTeam(uuid) != null)
                return match;
        return null;
    }

    /**
     * Gets the {@link Match} for the given {@link Team}, if one exists.
     */
    public Match getMatch(Team team) {
        for (Match match : round)
            for (Side side : match.sides)
                for (Team otherTeam : side.teams)
                    if (otherTeam == team)
                        return match;
        return null;
    }


    /**
     * Ends a match in every relevant way. This will:
     * <li>remove the {@link Match} from the round, </li>
     * <li>knock out the relevant teams (if not protected by a canceled handle of {@link TeamKnockedOutEvent}),</li>
     * <li>free up the zone that was being occupied by the {@link Match},</li>
     * <li>and call {@link Tournament}.checkForMoreBattles() and if it returns false, start the next round.</li>
     *
     * @param match       - The {@link Match} that just ended.
     * @param winningSide - The winning {@link Side}.
     * @param losingSide  - The losing {@link Side}.
     */
    public void matchEnds(Match match, Side winningSide, Side losingSide) {
        MatchEndEvent event = new MatchEndEvent(match, winningSide, losingSide);
        Tournaments.EVENT_BUS.post(event);
        winningSide = event.winningSide;
        losingSide = event.losingSide;

        if (getRuleSet().isElo())
            getRuleSet().getRule(EloType.class).eloMatches.add(new EloMatch(winningSide.getUUIDs(), losingSide.getUUIDs(), false));

        sendMessage(messageProvider.getMatchWinMessage(winningSide, losingSide));

        for (Team team : winningSide.teams)
            this.getModes().forEach(mode -> mode.onTeamWin(this, team));

        for (Team team : losingSide.teams) {
            List<Mode> modes = this.getModes();
            modes.forEach(mode -> mode.onTeamLose(this, team));

            boolean knockOut = true;
            for (Mode mode : modes)
                if (!(knockOut = mode.isTeamKnockedOut(this, team)))
                    break;

            if (!knockOut || Tournaments.EVENT_BUS.post(new TeamKnockedOutEvent(team, match)))
                continue;

            team.alive = false;
            getModes().forEach(mode -> mode.onTeamKnockedOut(this, team));
        }

        final Side winningSideFinal = winningSide;
        final Side losingSideFinal = losingSide;

        this.getModes().forEach(mode -> mode.onMatchEnd(this, match, winningSideFinal, losingSideFinal));
        Zones.INSTANCE.matchEnded(this, match);
        round.remove(match);
        if (!checkForMoreBattles() && round.isEmpty()) {
            getModes().forEach(mode -> mode.onRoundEnd(this));
            startRound();
        }
    }

    /**
     * Handles a draw by first checking for any draw decider rules. If a decision is made,
     * that {@link Side} will be declared the winner. If a decision could not be made or
     * there were no decider rules, a rematch will be scheduled.
     *
     * @param match - The match that ended in a draw end.
     */
    public void handleDraw(Match match) {
        List<DeciderRule> deciders = getRuleSet().getDrawDeciderRules();
        Side winningSide = null;
        for (DeciderRule rule : deciders)
            if ((winningSide = rule.decideWinner(match)) != null)
                break;

        if (winningSide == null) {
            if (getRuleSet().isElo())
                getRuleSet().getRule(EloType.class).eloMatches.add(new EloMatch(match.sides[0].getUUIDs(), match.sides[1].getUUIDs(), true));

            sendMessage(getMessageProvider().getMatchDrawMessage(match));
            tasks.add(Scheduler.delay(TournamentConfig.INSTANCE.timeBeforeMatch - 5, TimeUnit.SECONDS, () -> {
                if (round.contains(match)) {
                    match.start(this, true);
                }
            }));
        } else {
            this.matchEnds(match, winningSide, match.getOtherSide(winningSide));
        }
    }

    /**
     * Starts a round by calculating the matches, stating them, then scheduling a call to
     * {@link Tournament}.checkForMoreBattles(). If the calculated round is empty, ends the
     * tournament with winners calculated in {@link Tournament}.getWinners(...);
     */
    public void startRound() {
        round.clear();

        round = new ArrayList<>();

        List<Mode> modes = this.getModes();
        for (Mode mode : modes)
            if ((round = mode.createMatches(this)) != null)
                break;

        roundNum++;

        if (round == null)
            round = createMatches();

        if (!round.isEmpty()) {
            Tournaments.EVENT_BUS.post(new RoundStartEvent(round == null ? (round = new ArrayList<>()) : round));
            getModes().forEach(mode -> mode.onRoundStart(this, ImmutableList.copyOf(round)));

            sendMessage(messageProvider.getUpcomingRoundMessage(roundNum, round));

            tasks.add(Scheduler.delay(TournamentConfig.INSTANCE.timeBeforeMatch - 5, TimeUnit.SECONDS, this::checkForMoreBattles));

            return;
        }
        this.end(getWinners());
    }

    /**
     * Gets the winners based off any available win calculating modes, otherwise uses
     * the default calculation.
     *
     * @return - The list of winners, or empty if there were no winners.
     */
    public List<Team> getWinners() {
        List<Team> winners = new ArrayList<>();

        List<Mode> modes = this.getModes();
        for (Mode mode : modes)
            if ((winners = mode.calculateWinners(this, teams, winners)) != null)
                return winners;

        winners = new ArrayList<>();
        for (Team team : teams)
            if (team.alive)
                winners.add(team);

        return winners;
    }

    /**
     * Creates the matches for the next round based off the available matchmaking modes or the default
     * matchmaker. If the returned list is empty then the tournament is ready to announce winners.
     *
     * @return - The collection of matches for the next round. Empty if there are no matches to be had
     * which renders the tournament complete and ready for winners.
     */
    public List<Match> createMatches() {
        List<Match> matches;

        // Check the other modes first
        List<Mode> modes = this.getModes();
        for (Mode mode : modes)
            if (mode != this && (matches = mode.createMatches(this)) != null)
                return matches;

        matches = new ArrayList<>();

        List<Team> teams = new ArrayList<>(this.teams);
        teams.removeIf(team -> !team.alive);
        while (teams.size() > 1) {
            Team team1 = teams.get(RandomHelper.getRandomNumberBetween(0, teams.size() - 1));
            teams.remove(team1);
            Team team2 = teams.get(RandomHelper.getRandomNumberBetween(0, teams.size() - 1));
            teams.remove(team2);
            try {
                matches.add(matchClass.getConstructor(Side.class, Side.class).newInstance(Side.of(team1), Side.of(team2)));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                     | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }

        if (teams.size() == 1 && matches.size() > 0)
            for (User user : teams.get(0).users)
                if (user.isOnline())
                    if (getMessageProvider().getByeMessage() != null)
                        user.getPlayer().sendMessage(getMessageProvider().getByeMessage(), Util.DUMMY_UUID);

        return matches;
    }

    /**
     * Checks for more battles in the round. If there are matches waiting to be started, and there are zones
     * free to contain them, they will be allotted and started.
     *
     * @return - true if there are still waiting battles in this round. False if not.
     */
    public boolean checkForMoreBattles() {
        List<Match> waitingMatches = new ArrayList<>(round);
        waitingMatches.removeIf(match -> match.matchActive);

        boolean matchExists = waitingMatches.size() > 0;

        for (Match match : waitingMatches) {
            Zone zone = Zones.INSTANCE.getFreeZone();
            if (zone == null && !Zones.INSTANCE.getEngagedZones().isEmpty())
                break;
            else if (zone != null)
                Zones.INSTANCE.registerZoneMatch(zone, match);
            match.start(this);
        }

        return matchExists;
    }

    /**
     * Opens the tournament for joining.
     */
    public void open() {
        this.state = TournamentStates.OPEN;
        sendMessage(getMessageProvider().getOpenMessage(this));
        sendMessage(
                // TODO does this work? Maybe getMessageProvider().getIgnorePromptMessage().onClick? But I hope this works anyway
                Text.of(getMessageProvider().getIgnorePromptMessage())
                        .onClick((src, ctx) -> {
                            ignoreList.add(src.getUniqueID());
                            src.sendMessage(getMessageProvider().getIgnoreToggleMessage(true), Util.DUMMY_UUID);
                        })
        );
        getModes().forEach(mode -> mode.onTournamentOpen(this));

        // Just in case it didn't get cleared before.
        if (getRuleSet().isElo())
            getRuleSet().getRule(EloType.class).eloMatches.clear();
    }

    /**
     * Closes the tournament and wipes all data (state, round, teams, ignore list, rule set).
     */
    public void close() {
        sendMessage(getMessageProvider().getClosedMessage(state));
        this.state = TournamentStates.CLOSED;
        this.round = new ArrayList<>();
        this.teams = new ArrayList<>();
        this.ignoreList = new ArrayList<>();
        this.ruleSet = new RuleSet();
        tasks.forEach(Scheduler::cancel);
    }

    /**
     * Starts the tournament and initiated the first round, if it is not denied
     * by a canceled handle of {@link TournamentStartEvent}.
     */
    public void start() {
        if (Tournaments.EVENT_BUS.post(new TournamentStartEvent()))
            return;
        getModes().forEach(mode -> mode.onTournamentStart(this));
        sendMessage(getMessageProvider().getStartMessage(this));
        this.state = TournamentStates.ACTIVE;
        startRound();
    }

    /**
     * Ends the tournament given the list (potentially empty) of winning {@link Team}s.
     *
     * @param winners - The winners of the tournament. May be empty.
     */
    public void end(List<Team> winners) {
        if (winners == null)
            winners = new ArrayList<>();

        List<User> winnerUsers = new ArrayList<>();
        for (Team team : winners)
            for (User user : team.users)
                winnerUsers.add(user);

        Tournaments.EVENT_BUS.post(new TournamentEndEvent(winnerUsers));
        getModes().forEach(mode -> mode.onTournamentEnd(this, winnerUsers));

        if (winners.isEmpty())
            sendMessage(messageProvider.getNoWinnerMessage());
        else {
            sendMessage(messageProvider.getWinnerMessage(winners));

            for (User user : winnerUsers) {
                PlayerEntity player = user.getPlayer();
                if (player != null) {
                    for (RewardBase reward : rewards) {
                        reward.give(player);
                    }
                }
            }
        }

        if (getRuleSet().isElo()) {
            EloStorage.pauseSaving = true;
            getRuleSet().getRule(EloType.class).eloMatches.forEach(EloMatch::process);
            EloStorage.pauseSaving = false;
            EloStorage.save();
        }

        INSTANCE = null;
    }

    public void showTournament(CommandSource source) {
        source.sendFeedback(Text.of(TextFormatting.GOLD, "---------- Tournament -----------"), false);
        if (state == TournamentStates.CLOSED) {
            source.sendFeedback(Text.of(TextFormatting.GOLD, "State: ", TextFormatting.RED, "CLOSED"), false);
        } else if (state == TournamentStates.OPEN) {
            source.sendFeedback(Text.of(TextFormatting.GOLD, "State: ", TextFormatting.GREEN, "OPEN"), false);
        } else {
            source.sendFeedback(Text.of(TextFormatting.GOLD, "State: ", TextFormatting.YELLOW, "ACTIVE"), false);
            source.sendFeedback(Text.of(TextFormatting.GOLD, "Round: ", TextFormatting.DARK_AQUA, roundNum), false);
        }

        source.sendFeedback(Text.of(TextFormatting.GOLD, "Rules: ",
                Text.of(TextFormatting.GRAY, "[Hover to see, click for full list]")
                        .onHover(getRuleSet().getDisplayText())
                        .onClick((src, ctx) -> {
                            ctx.keepAlive();

                            List<Text> contents = new ArrayList<>();
                            for (RuleBase rule : getRuleSet().rules)
                                if (rule.canShow(src))
                                    contents.add(Text.of(rule.getDisplayText()));
                            Pagination.builder()
                                    .title(Text.of(TextFormatting.GOLD, "Rules"))
                                    .padding(Text.of(TextFormatting.GOLD, "-"))
                                    .linesPerPage(10)
                                    .contents(contents)
                                    .sendTo(src);
                        })
        ), false);

        if (!getRuleSet().br.exportText().isEmpty()) {
            source.sendFeedback(Text.of(TextFormatting.GOLD, "Battle Rules: ",
                    Text.of(TextFormatting.GRAY, "[Hover to see, click for full list]")
                            .onHover(Text.of(getRuleSet().br.exportText()))
                            .onClick((src, ctx) -> {
                                ctx.keepAlive();

                                // TODO: this is an NPE!! Also do the same for ^^^^^ onHover
                                List<Text> contents = new ArrayList<>();
                                contents.addAll(
                                        BattleRuleRegistry.getAllProperties().stream()
                                                .filter(bp -> !bp.getId().equals(BattleRuleRegistry.CLAUSES.getId()))
                                                .map(bp -> Pair.of(bp, (PropertyValue<?>) this.ruleSet.br.get(bp).orElse(null)))
                                                .filter(bp -> bp.getValue() != null)
                                                .map(bp -> Text.of(TextFormatting.LIGHT_PURPLE, bp.getLeft().getId(), ": ", bp.getRight().get().toString()))
                                                .collect(Collectors.toList())
                                );
                                contents.addAll(
                                        this.ruleSet.br.getClauseList().stream()
                                                .map(c -> Text.of(TextFormatting.DARK_PURPLE, "[C] ", c.getID(), ": ", TextFormatting.LIGHT_PURPLE, c.getDescription()))
                                                .collect(Collectors.toList())
                                );

                                Pagination.builder()
                                        .title(Text.of(TextFormatting.GOLD, "Battle Rules"))
                                        .padding(Text.of(TextFormatting.GOLD, "-"))
                                        .linesPerPage(10)
                                        .contents(contents)
                                        .sendTo(source);
                            })
            ), false);
        }
        source.sendFeedback(Text.of(TextFormatting.GOLD, "Rewards: ",
                Text.of(TextFormatting.GRAY, "[Hover to preview, click for full list]")
                        .onHover(TournamentUtils.showRewards(source))
                        .onClick((src, ctx) -> {
                            ctx.keepAlive();
                            List<Text> contents = new ArrayList<>();
                            for (RewardBase reward : rewards)
                                if (reward.canShow(src))
                                    contents.add(Text.of(reward.getDisplayText()));
                            Pagination.builder()
                                    .title(Text.of(TextFormatting.GOLD, "Rewards"))
                                    .padding(Text.of(TextFormatting.GOLD, "-"))
                                    .linesPerPage(10)
                                    .contents(contents)
                                    .sendTo(src);
                        })
        ), false);

        source.sendFeedback(Text.of(TextFormatting.GOLD, "Teams: ",
                Text.of(TextFormatting.GRAY, "[Hover to preview, click for full list]")
                        .onHover(TournamentUtils.showTeams(source))
                        .onClick((src, ctx) -> {
                            ctx.keepAlive();

                            List<Text> contents = new ArrayList<>();

                            List<Team> liveTeams = new ArrayList<>();
                            List<Team> deadTeams = new ArrayList<>();

                            for (Team team : teams) {
                                if (team.alive)
                                    liveTeams.add(team);
                                else
                                    deadTeams.add(team);
                            }

                            List<Team> orderedTeams = new ArrayList<>();
                            for (Team team : liveTeams)
                                orderedTeams.add(team);
                            for (Team team : deadTeams)
                                orderedTeams.add(team);

                            for (Team team : orderedTeams) {
                                if (team.alive)
                                    contents.add(Text.of(TextFormatting.GREEN, "*", team.getDisplayText()));
                                else
                                    contents.add(Text.of(TextFormatting.RED, "*", team.getDisplayText()));
                            }

                            Pagination.builder()
                                    .title(Text.of(TextFormatting.GOLD, "Teams"))
                                    .padding(Text.of(TextFormatting.GOLD, "-"))
                                    .header(Text.of(TextFormatting.GREEN, "*", TextFormatting.GOLD, " = alive, ", TextFormatting.RED, "*", TextFormatting.GOLD, " = knocked out, "))
                                    .linesPerPage(10)
                                    .contents(contents)
                                    .sendTo(src);
                        })
        ), false);

        source.sendFeedback(Text.of(TextFormatting.GOLD, "Current Matches: ",
                Text.of(TextFormatting.GRAY, "[Hover to preview, click for full list]")
                        .onHover(TournamentUtils.showMatches(source))
                        .onClick((src, ctx) -> {
                            ctx.keepAlive();

                            List<Text> contents = new ArrayList<>();
                            for (Match match : round)
                                contents.add(Text.of(match.getStateText(), match.getDisplayText()));
                            Pagination.builder()
                                    .title(Text.of(TextFormatting.GOLD, "Upcoming Matches"))
                                    .padding(Text.of(TextFormatting.GOLD, "-"))
                                    .header(Text.of(TextFormatting.GRAY, "*", TextFormatting.GOLD, " = waiting, ", TextFormatting.YELLOW, "*", TextFormatting.GOLD, " = starting, ", TextFormatting.RED, "*", TextFormatting.GOLD, " = active"))
                                    .linesPerPage(10)
                                    .contents(contents)
                                    .sendTo(src);
                        })
        ), false);

        source.sendFeedback(Text.of(TextFormatting.GOLD, "--------------------------------"), false);
    }

    public ImmutableList<Mode> getModes() {
        List<Mode> modes = new ArrayList<>();
        modes.addAll(rewards);
        modes.addAll(getRuleSet().rules);
        modes.addAll(extraModes);
        modes.add(this);
        return ImmutableList.copyOf(modes);
    }
}
