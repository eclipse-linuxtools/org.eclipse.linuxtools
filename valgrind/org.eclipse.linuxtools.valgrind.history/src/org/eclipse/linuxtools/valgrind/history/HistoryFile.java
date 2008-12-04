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
package org.eclipse.linuxtools.valgrind.history;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.XMLMemento;

/*
 * The history file is contained in the plugin's metadata and is a listing of the last
 * <code>LIMIT</code> launches. Each line in the file lists the program name,
 * the tool ran, and the name of the directory that contains the output data for
 * that launch. The directory is relative to the directory containing the history file.
 */
public class HistoryFile {
	public static final int LIMIT = 5;
	
	public static final String DATA_PATH = ValgrindHistoryPlugin.getDefault().getStateLocation().toOSString();
	protected static final HistoryFile INSTANCE = new HistoryFile();
	protected static final String HISTORY_FILE = ".history"; //$NON-NLS-1$
	protected static final String OUTDIR_PREFIX = ".launch"; //$NON-NLS-1$
	protected static final String MEMENTO_FILE = ".memento"; //$NON-NLS-1$

	protected File histFile;
	protected List<HistoryEntry> entries;

	protected HistoryFile() {
		entries = new ArrayList<HistoryEntry>();
		
		BufferedReader br = null;
		try {
			histFile = new File(DATA_PATH + File.separator + HISTORY_FILE);
			if (!histFile.exists()) {
				histFile.createNewFile();
			}

			br = new BufferedReader(new FileReader(histFile));
			String line;
			while ((line = br.readLine()) != null) {
				entries.add(HistoryEntry.fromString(line));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CoreException e) {
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

	public void write(String launchStr, XMLMemento memento, File datadir) throws IOException, CoreException {
		if (entries.size() >= LIMIT) {
			entries.remove(0);
		}
		
		// save the memento
		File mementoFile = new File(datadir, HistoryFile.MEMENTO_FILE);
		FileWriter fw = new FileWriter(mementoFile);
		memento.save(fw);
		
		entries.add(new HistoryEntry(launchStr, memento, datadir));

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
				pw.println(entry.toString());
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

	public synchronized static HistoryFile getInstance() {
		return INSTANCE;
	}

	public File createOutputDir() {
		if (entries.size() == LIMIT) {
			entries.get(0).destroy();
			entries.remove(0);
		}
		return new File(DATA_PATH, OUTDIR_PREFIX + timestamp());
	}
	
	protected long timestamp() {
		return System.currentTimeMillis();
	}
}
