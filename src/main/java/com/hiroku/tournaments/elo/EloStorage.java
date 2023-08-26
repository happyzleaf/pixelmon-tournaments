package com.hiroku.tournaments.elo;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hiroku.tournaments.Tournaments;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;

public class EloStorage {
	public static final String PATH = "data/tournaments/elo.json";
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static boolean pauseSaving = false;

	private static EloStorage INSTANCE;

	public Map<UUID, Map<EloTypes, EloData>> data = new HashMap<>();

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

	public static int getElo(UUID uuid, EloTypes type) {
		if (type == null) {
			return getAverageElo(uuid);
		}

		return INSTANCE.data.getOrDefault(uuid, new HashMap<>()).getOrDefault(type, new EloData()).getElo();
	}

	public static void registerBattle(UUID winner, UUID loser, EloTypes type, boolean draw) {
		registerBattle(Collections.singletonList(winner), Collections.singletonList(loser), type, draw);
	}

	public static void registerBattle(List<UUID> winners, List<UUID> losers, EloTypes type, boolean draw) {
		List<UUID> allUUIDs = new ArrayList<>(winners);
		allUUIDs.addAll(losers);

		HashMap<UUID, EloData> previousElos = new HashMap<>();
		for (UUID uuid : allUUIDs) {
			if (!INSTANCE.data.containsKey(uuid))
				INSTANCE.data.put(uuid, new HashMap<>());
			if (!INSTANCE.data.get(uuid).containsKey(type))
				INSTANCE.data.get(uuid).put(type, new EloData());
			previousElos.put(uuid, INSTANCE.data.get(uuid).get(type));
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

	public static void clearElo(UUID uuid, EloTypes type) {
		if (INSTANCE.data.containsKey(uuid)) {
			INSTANCE.data.get(uuid).remove(type);
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

	public static void clearAllElos(EloTypes type) {
		if (type == null) {
			clearAllElos();
			return;
		}

		Tournaments.log("Wiping all " + type + " Elo ratings");
		for (Entry<UUID, Map<EloTypes, EloData>> entry : INSTANCE.data.entrySet()) {
			entry.getValue().remove(type);
		}

		save();
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

	public static List<UUID> getTopXElo(int x, EloTypes type) {
		if (type == null)
			return getTopXAverageElo(x);

		HashMap<UUID, Integer> values = new HashMap<>();
		for (Entry<UUID, Map<EloTypes, EloData>> entry : INSTANCE.data.entrySet())
			if (entry.getValue().containsKey(type))
				values.put(entry.getKey(), entry.getValue().get(type).getElo());
		return getTopX(x, values);
	}

	public static List<UUID> getTopXAverageElo(int x) {
		HashMap<UUID, Integer> values = new HashMap<>();
		for (UUID uuid : INSTANCE.data.keySet())
			values.put(uuid, getAverageElo(uuid));
		return getTopX(x, values);
	}

	private static List<UUID> getTopX(int x, HashMap<UUID, Integer> values) {
		List<UUID> leaderUUIDs = new ArrayList<>();
		List<Integer> leaderValues = new ArrayList<>();

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

		List<UUID> finalLeaderUUIDs = new ArrayList<>();

		while (!leaderUUIDs.isEmpty()) {
			int minIndex = minimumIndex(leaderValues);
			leaderValues.remove(minIndex);
			finalLeaderUUIDs.add(0, leaderUUIDs.remove(minIndex));
		}

		return finalLeaderUUIDs;
	}

	private static int minimumIndex(List<Integer> values) {
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
