package com.hiroku.tournaments.config;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.hiroku.tournaments.util.GsonUtils;

import java.io.*;
import java.util.ArrayList;

/**
 * JSON object for storing all configurable elements for tournaments
 *
 * @author Hiroku
 */
public class TournamentConfig {
	public static final String PATH = "config/tournaments/tournaments.json";
	public static TournamentConfig INSTANCE;

	public ArrayList<String> baseCommandAliases = Lists.newArrayList("tournaments", "tournament", "tourneys", "tourney");
	public ArrayList<String> baseEloCommandAliases = Lists.newArrayList("elo");
	public int timeBeforeMatch = 30;

	private int nextZoneID = 0;

	public int eloFactor = 400;
	/**
	 * The number of people on the elo leaderboard by default when using /elo list.
	 */
	public int defaultEloTopNumber = 5;

	// All tournament messages
	public String prefix = "&l&dTournament &6\u00BB &r";
	public String joinMessage = "{{team}} &2joined the tournament!";
	public String leaveMessage = "{{team}} &cleft the tournament.";
	public String forfeitMessage = "{{team}} &cforfeited!";
	public String openMessage = "&6A new tournament has been opened! Use &3/tournament rules &6to check the rules, and &3/tournament join &6to join!";
	public String startMessage = "&6The tournament has started!";
	public String closeMessage = "&7The tournament was closed.";
	public String noWinnerMessage = "&6The tournament ended with no winners! What a bummer.";
	public String winnerMessage = "&6The tournament has ended! Congratulations {{winners}}&6!";
	public String matchWinMessage = "{{winners}} &2defeated {{losers}}&2!";
	public String matchDrawMessage = "{{match}}&e ended in a draw! Rematch in&3 {{time}}&e seconds.";
	public String matchErrorMessage = "{{match}}&c errored! Restarting match in&3 {{time}}&c seconds.";
	public String upcomingRoundMessage = "&6Upcoming Round:\n{{round}}";
	public String byeMessage = "&2You've been given a bye for this round!";
	public String insufficientPokemonMessage = "{{side}}&c had insufficient Pokémon! Disqualified!";
	public String offlinePlayerMessage = "{{side}}&c was offline! Disqualified!";
	public String offlinePlayersMessage = "{{side}}&c had at least 1 too many players offline! Disqualified!";
	public String ruleBreakMessage = "{{ruleerror}}&c Disqualified!";
	public String battleRuleBreakMessage = "&3{{user}} &cbroke the &e{{clause}} &cclause! Disqualified!";
	public String ignorePromptMessage = "&7Click here to ignore all tournament messages";
	public String ignoreToggleOnMessage = "&7Tournament messages: &2on";
	public String ignoreToggleOffMessage = "&7Tournament messages: &coff";


	public static void load() {
		INSTANCE = new TournamentConfig();

		File file = new File(PATH);
		if (!file.exists())
			INSTANCE.save();
		else {
			try (InputStreamReader isr = new InputStreamReader(new FileInputStream(file), Charsets.UTF_8)) {
				INSTANCE = GsonUtils.prettyGson.fromJson(isr, TournamentConfig.class);
				isr.close();
				// Save for when new options have been added
				INSTANCE.save();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public void save() {
		File file = new File(PATH);
		try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8)) {
			String json = GsonUtils.prettyGson.toJson(this);
			osw.write(json);
			osw.flush();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public int getNextZoneID() {
		nextZoneID++;
		save();
		return nextZoneID;
	}
}
