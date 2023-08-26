package com.happyzleaf.tournaments;

import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;

public class Scheduler {
	private static final Map<UUID, Ticker> tickers = new HashMap<>();

	public static UUID delayTicks(long ticks, Runnable task) {
		checkArgument(ticks >= 0, "ticks >= 0");

		UUID id = UUID.randomUUID();
		Ticker ticker = new Ticker(id, ticks, task);
		tickers.put(id, ticker);
		return id;
	}

	public static UUID delayTime(long time, TimeUnit unit, Runnable task) {
		return delayTicks(unit.toSeconds(time) * 20, task);
	}

	public static boolean cancel(UUID id) {
		return tickers.remove(id) != null;
	}

	private static class Ticker implements Runnable {
		private final UUID id;
		private long ticks;
		private final Runnable task;

		public Ticker(UUID id, long ticks, Runnable task) {
			this.id = id;
			this.ticks = ticks;
			this.task = task;
			ServerLifecycleHooks.getCurrentServer().registerTickable(this);
		}

		@Override
		public void run() {
			if (ticks-- == 0) {
				task.run();
				ServerLifecycleHooks.getCurrentServer().tickables.remove(this);
				cancel(id);
			}
		}
	}
}
