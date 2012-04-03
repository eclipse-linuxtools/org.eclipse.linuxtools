/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.logging;

import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.linuxtools.internal.systemtap.ui.logging.LogEntry;
import org.eclipse.linuxtools.systemtap.ui.logging.LogManager;



/**
 * Logging daemon. This object runs on its own thread and wakes up every 100ms, at which point
 * it flushes all queued messages to the Writer. 
 * @author Henry Hughes, Ryan Morse
 */
/*
 * Strings in this class should not be externalized since they are
 * primarily for debugging purposes.
 */
public class LogDaemon extends TimerTask {
	public LogDaemon(BufferedWriter writer, int level) {
		logLevel = level % NAMES.length;
		Timer t = new Timer("LogDaemon",true);
		t.scheduleAtFixedRate(this, 100, 5000);
	}

	public void run() {
		LinkedList<?> entries = LogManager.getInstance().getEntries();
		if(writer == null || 0 == entries.size())
			return;
		StringBuilder builder = new StringBuilder();
		DateFormat df = DateFormat.getTimeInstance();
		while(!entries.isEmpty()) {
			LogEntry le = (LogEntry)(entries.removeFirst());
			if(le.level > logLevel)
				continue;
			try {
				builder.delete(0, builder.length());
				String time = df.format(new Date(System.currentTimeMillis()));
				builder.append("[" + NAMES[le.level] + "] - " + time + " - ");
				builder.append(le.message + "\n");
				writer.write(builder.toString());
				writer.flush();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int logLevel;
	private BufferedWriter writer;
	private final static String[] NAMES = {"fatal","critical","info","debug"};
}
