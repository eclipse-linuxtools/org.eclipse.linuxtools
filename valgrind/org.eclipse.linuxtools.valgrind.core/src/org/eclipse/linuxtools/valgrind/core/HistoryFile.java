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
	protected static final String SEPARATOR = ":"; //$NON-NLS-1$
	protected File histFile;
	protected List<String> executables;
	protected List<String> tools;
	protected List<String> datadirs;

	protected HistoryFile() {
		executables = new ArrayList<String>();
		tools = new ArrayList<String>();
		datadirs = new ArrayList<String>();
		
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
				executables.add(parts[0]);
				tools.add(parts[1]);
				datadirs.add(parts[2]);
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

	public void write(String executable, String toolID, String datadir) throws IOException {
		if (executables.size() >= LIMIT) {
			executables.remove(0);
			tools.remove(0);
			datadirs.remove(0);
		}
		executables.add(executable);
		tools.add(toolID);
		datadirs.add(datadir);

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(histFile);
			for (int i = 0; i < executables.size(); i++) {
				pw.println(executables.get(i) + SEPARATOR + tools.get(i) + SEPARATOR + datadirs.get(i));
			}
		} finally {
			if (pw != null) {
				pw.close();
			}
		}
	}

	public String[] getExecutables() {
		return executables.toArray(new String[executables.size()]);
	}
	
	public String[] getTools() {
		return tools.toArray(new String[tools.size()]);
	}
	
	public String[] getDatadirs() {
		return datadirs.toArray(new String[datadirs.size()]);
	}
	
	public int getNextIndex() {
		return executables.size() % LIMIT;
	}

	public synchronized static HistoryFile getInstance() {
		return INSTANCE;
	}
}
