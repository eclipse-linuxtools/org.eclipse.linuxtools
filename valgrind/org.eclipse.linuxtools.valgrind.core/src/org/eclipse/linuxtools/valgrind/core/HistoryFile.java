/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/*
 * The history file is contained in the plugin's metadata and is a listing of the last
 * <code>LIMIT</code> launches. Each line in the file lists the program name,
 * the tool ran, and the name of the directory that contains the output data for
 * that launch. The directory is relative to the directory containing the history file.
 */
public class HistoryFile {
	public static final int LIMIT = 5;
	protected static final HistoryFile INSTANCE = new HistoryFile();
	protected static final String HISTORY_FILE = ".history"; //$NON-NLS-1$
	protected static final String SEPARATOR = "\t"; //$NON-NLS-1$
	protected static final int NUM_FIELDS = 4;
	protected File histFile;
	protected List<HistoryEntry> entries;

	protected HistoryFile() {
		entries = new ArrayList<HistoryEntry>();
		
		BufferedReader br = null;
		try {
			histFile = new File(ValgrindCommand.DATA_PATH + File.separator + HISTORY_FILE);
			if (!histFile.exists()) {
				histFile.createNewFile();
			}

			br = new BufferedReader(new FileReader(histFile));
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(SEPARATOR);
				if (parts.length != NUM_FIELDS) {
					throw new IOException(Messages.getString("HistoryFile.History_file_is_corrupt")); //$NON-NLS-1$
				}
				entries.add(new HistoryEntry(parts[0], parts[1], parts[2], parts[3]));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();	
				}				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void write(String configName, String toolID, String datadir, String processLabel) throws IOException {
		if (entries.size() >= LIMIT) {
			entries.remove(0);
		}
		entries.add(new HistoryEntry(configName, toolID, datadir, processLabel));

		flush();
	}
	
	public void moveToEnd(HistoryEntry entry) throws IOException {
		if (entries.remove(entry)) {
			entries.add(entry);
		}
		
		flush();
	}

	protected void flush() throws IOException {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(histFile);
			for (int i = 0; i < entries.size(); i++) {
				HistoryEntry entry = entries.get(i);
				pw.println(entry.getConfigName() + SEPARATOR + entry.getTool() + SEPARATOR + entry.getDatadir() + SEPARATOR + entry.getProcessLabel());
			}
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}
	
	public HistoryEntry[] getEntries() {
		return entries.toArray(new HistoryEntry[entries.size()]);
	}
	
	public HistoryEntry getRecentEntry() {
		return entries.size() > 0 ? entries.get(entries.size() - 1) : null;
	}
	
	public int getNextIndex() {
		return entries.size() % LIMIT;
	}

	public synchronized static HistoryFile getInstance() {
		return INSTANCE;
	}
}
