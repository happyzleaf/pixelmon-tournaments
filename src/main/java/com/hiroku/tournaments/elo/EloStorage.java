package com.hiroku.tournaments.elo;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hiroku.tournaments.Tournaments;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

public class EloStorage {
	public static final String PATH = "data/tournaments/elo.json";
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static boolean pauseSaving = false;

	private static EloStorage INSTANCE;

	public HashMap<UUID, HashMap<String, EloData>> data = new HashMap<>();

	public static void load() {
		File file = new File(PATH);
		file.getParentFile().mkdirs();

		try {
			if (file.exists())
				INSTANCE = GSON.fromJson(new FileReader(file), EloStorage.class);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (INSTANCE == null || INSTANCE.data == null) {
				INSTANCE = new EloStorage();
				Tournaments.log("Loaded new Elo storage");
				save();
			} else
				Tournaments.log("Loaded Elo storage from data/tournaments/elo.json");
		}
	}

	public static void save() {
		if (pauseSaving)
			return;

		File file = new File(PATH);
		file.getParentFile().mkdirs();

		try {
			file.createNewFile();
			PrintWriter pw = new PrintWriter(file);
			pw.write(GSON.toJson(INSTANCE));
			pw.flush();
			pw.close();
			Tournaments.log("Saved Elo ratings");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int getElo(UUID uuid, String eloType) {
		if (eloType == null)
			return getAverageElo(uuid);

		return INSTANCE.data.getOrDefault(uuid, new HashMap<String, EloData>()).getOrDefault(eloType.toLowerCase(), new EloData()).getElo();
	}

	public static void registerBattle(UUID winner, UUID loser, String eloType, boolean draw) {
		registerBattle(Lists.newArrayList(winner), Lists.newArrayList(loser), eloType.toLowerCase(), draw);
	}

	public static void registerBattle(ArrayList<UUID> winners, ArrayList<UUID> losers, String eloType, boolean draw) {
		eloType = eloType.toLowerCase();

		ArrayList<UUID> allUUIDs = new ArrayList<>(winners);
		allUUIDs.addAll(losers);

		HashMap<UUID, EloData> previousElos = new HashMap<>();
		for (UUID uuid : allUUIDs) {
			if (!INSTANCE.data.containsKey(uuid))
				INSTANCE.data.put(uuid, new HashMap<String, EloData>());
			if (!INSTANCE.data.get(uuid).containsKey(eloType))
				INSTANCE.data.get(uuid).put(eloType, new EloData());
			previousElos.put(uuid, INSTANCE.data.get(uuid).get(eloType));
		}

		int loserEloAverage = 0;
		for (UUID loser : losers)
			loserEloAverage += previousElos.get(loser).getElo();
		loserEloAverage = Math.round(1f * loserEloAverage / losers.size());

		int winnerEloAverage = 0;
		for (UUID winner : winners)
			winnerEloAverage += previousElos.get(winner).getElo();
		winnerEloAverage = Math.round(1f * winnerEloAverage / winners.size());

		for (UUID loser : losers) {
			previousElos.get(loser).totalBattles++;
			if (!draw)
				previousElos.get(loser).losses++;
			previousElos.get(loser).totalOpponentElo += winnerEloAverage;
		}

		for (UUID winner : winners) {
			previousElos.get(winner).totalBattles++;
			if (!draw)
				previousElos.get(winner).wins++;
			previousElos.get(winner).totalOpponentElo += loserEloAverage;
		}

		save();
	}

	public static void clearElo(UUID uuid, String eloType) {
		if (INSTANCE.data.containsKey(uuid)) {
			INSTANCE.data.get(uuid).remove(eloType.toLowerCase());
			save();
		}
	}

	public static void clearElo(UUID uuid) {
		INSTANCE.data.remove(uuid);
		save();
	}

	public static void clearAllElos() {
		Tournaments.log("Wiping all Elo ratings");
		INSTANCE.data.clear();
		save();
	}

	public static void clearAllElos(String eloType) {
		if (eloType == null)
			clearAllElos();
		else {
			Tournaments.log("Wiping all " + eloType + " Elo ratings");
			eloType = eloType.toLowerCase();
			for (Entry<UUID, HashMap<String, EloData>> entry : INSTANCE.data.entrySet())
				entry.getValue().remove(eloType);

			save();
		}
	}

	public static int getAverageElo(UUID uuid) {
		int total = 0;
		if (!INSTANCE.data.containsKey(uuid))
			return 0;

		int totalTypes = INSTANCE.data.get(uuid).size();
		for (EloData eloData : INSTANCE.data.get(uuid).values())
			total += eloData.getElo();

		return Math.round(1f * total / totalTypes);
	}

	public static List<UUID> getTopXElo(int x, String eloType) {
		if (eloType == null)
			return getTopXAverageElo(x);

		eloType = eloType.toLowerCase();

		HashMap<UUID, Integer> values = new HashMap<>();
		for (Entry<UUID, HashMap<String, EloData>> entry : INSTANCE.data.entrySet())
			if (entry.getValue().containsKey(eloType))
				values.put(entry.getKey(), entry.getValue().get(eloType).getElo());
		return getTopX(x, values);
	}

	public static List<UUID> getTopXAverageElo(int x) {
		HashMap<UUID, Integer> values = new HashMap<>();
		for (UUID uuid : INSTANCE.data.keySet())
			values.put(uuid, getAverageElo(uuid));
		return getTopX(x, values);
	}

	private static List<UUID> getTopX(int x, HashMap<UUID, Integer> values) {
		ArrayList<UUID> leaderUUIDs = new ArrayList<>();
		ArrayList<Integer> leaderValues = new ArrayList<>();

		for (Entry<UUID, Integer> entry : values.entrySet()) {
			if (leaderUUIDs.size() < x) {
				leaderUUIDs.add(entry.getKey());
				leaderValues.add(entry.getValue());
			} else {
				int minIndex = minimumIndex(leaderValues);
				int minValue = leaderValues.get(minIndex);
				if (minValue < entry.getValue()) {
					leaderUUIDs.set(minIndex, entry.getKey());
					leaderValues.set(minIndex, entry.getValue());
				}
			}
		}

		ArrayList<UUID> finalLeaderUUIDs = new ArrayList<>();

		while (!leaderUUIDs.isEmpty()) {
			int minIndex = minimumIndex(leaderValues);
			leaderValues.remove(minIndex);
			finalLeaderUUIDs.add(0, leaderUUIDs.remove(minIndex));
		}

		return finalLeaderUUIDs;
	}

	private static int minimumIndex(ArrayList<Integer> values) {
		int minIndex = -1;
		int minValue = Integer.MAX_VALUE;

		for (int i = 0; i < values.size(); i++) {
			if (values.get(i) < minValue) {
				minIndex = i;
				minValue = values.get(i);
			}
		}

		return minIndex;
	}
}
