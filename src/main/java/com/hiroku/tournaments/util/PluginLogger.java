package com.hiroku.tournaments.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Logger for plugins. This will by default print into both the console and to a file with the
 * given MODID as the file name.
 *
 * Taken from the ObliqueAPI
 *
 * @author Hiroku
 */
public class PluginLogger {
	public static final SimpleDateFormat ft = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	public static final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("EST"));

	public final boolean useFile;
	public final String MODID;
	public final File file;

	private final Logger consoleLogger;

	public PluginLogger(String MODID) {
		this(MODID, true);
	}

	public PluginLogger(String MODID, boolean useFile) {
		this.MODID = MODID;
		this.useFile = useFile;

		if (!useFile) {
			this.file = null;
			this.consoleLogger = null;
			return;
		}

		this.file = new File("logs/" + MODID + ".log");
		try {
			if (!this.file.exists())
				this.file.createNewFile();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		this.consoleLogger = LogManager.getLogger(MODID);
	}

	public void log(String msg) {
		consoleLogger.log(Level.INFO, msg);
		if (!useFile)
			return;
		try {
			FileWriter fw = new FileWriter(file, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw);
			out.println("[" + ft.format(calendar.getTime()) + "] " + msg);
			out.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
