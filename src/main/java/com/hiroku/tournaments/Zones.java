package com.hiroku.tournaments;

import com.happyzleaf.tournaments.Scheduler;
import com.happyzleaf.tournaments.User;
import com.hiroku.tournaments.api.Match;
import com.hiroku.tournaments.api.Preset;
import com.hiroku.tournaments.config.TournamentConfig;
import com.hiroku.tournaments.obj.LocationWrapper;
import com.hiroku.tournaments.obj.Side;
import com.hiroku.tournaments.obj.Team;
import com.hiroku.tournaments.obj.Zone;
import com.hiroku.tournaments.util.GsonUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Managing object for all the teleport zones.
 *
 * @author Hiroku
 * @author happyz
 */
public class Zones {
	public static final String PATH = "config/tournaments/zones.json";
	public static Zones INSTANCE = new Zones();
	private static final HashMap<Zone, Match> zoneMatches = new HashMap<Zone, Match>();

	private final ArrayList<Zone> zones = new ArrayList<Zone>();

	public LocationWrapper leaveZone = null;

	public ArrayList<Zone> getZones() {
		return zones;
	}

	public Zone getFreeZone() {
		ArrayList<Zone> engagedZones = this.getEngagedZones();
		if (engagedZones.isEmpty())
			engagedZones = zones;
		for (Zone zone : engagedZones)
			if (!zoneMatches.containsKey(zone))
				return zone;
		return null;
	}

	public void registerZoneMatch(Zone zone, Match match) {
		zoneMatches.put(zone, match);
	}

	public Zone getZone(Match match) {
		for (Zone zone : zones)
			if (zoneMatches.get(zone) == match)
				return zone;
		return null;
	}

	public Zone getZone(int uid) {
		for (Zone zone : zones)
			if (zone.uid == uid)
				return zone;
		return null;
	}

	public ArrayList<Zone> getEngagedZones() {
		ArrayList<Zone> engagedZones = new ArrayList<Zone>();
		for (Zone zone : zones)
			if (zone.engaged)
				engagedZones.add(zone);
		return engagedZones;
	}

	public Match getMatch(Zone zone) {
		return zoneMatches.get(zone);
	}

	public void matchEnded(Match match) {
		Zone matchZone = null;
		for (Zone zone : zoneMatches.keySet()) {
			if (zoneMatches.get(zone) == match) {
				matchZone = zone;
				break;
			}
		}
		if (matchZone != null) {
			zoneMatches.remove(matchZone);
		}

		if (this.leaveZone != null) {
			Scheduler.delayTicks(60, () -> {
				for (Side side : match.sides) {
					for (Team team : side.teams) {
						for (User user : team.users) {
							if (user.isOnline()) {
								leaveZone.sendPlayer(user.getPlayer());
							}
						}
					}
				}
			});
		}
	}

	public void addZone(Zone zone) {
		zone.uid = TournamentConfig.INSTANCE.getNextZoneID();
		zones.add(zone);
	}

	public void removeZone(Zone zone) {
		zones.remove(zone);

		for (Entry<String, Preset> entry : Presets.getPresets().entrySet()) {
			if (entry.getValue().zones.contains(zone)) {
				entry.getValue().zones.remove(zone);
				Presets.savePreset(entry.getKey());
			}
		}
	}

	public void clear() {
		zoneMatches.clear();
	}

	public static void load() {
		try {
			File file = new File(PATH);
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				INSTANCE = GsonUtils.prettyGson.fromJson(br, Zones.class);
				br.close();
			} else {
				file.createNewFile();
				INSTANCE.save();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save() {
		try {
			Files.write(Paths.get(PATH), GsonUtils.prettyGson.toJson(this).getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
