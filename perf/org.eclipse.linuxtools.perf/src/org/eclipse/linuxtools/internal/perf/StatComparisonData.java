/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.linuxtools.internal.perf.handlers.Messages;
import org.eclipse.linuxtools.internal.perf.model.PMStatEntry;
import org.eclipse.linuxtools.internal.perf.model.PMStatEntry.Type;

/**
 * Class containing all functionality for comparting perf statistics data.
 */
public class StatComparisonData {
	// Old stats file.
	private File oldFile;

	// New stats file.
	private File newFile;

	// Comparison result string.
	private String result = ""; //$NON-NLS-1$

	// Title for this comparison run.
	private String title;

	public StatComparisonData(String title, File oldFile, File newFile) {
		this.title = title;
		this.oldFile = oldFile;
		this.newFile = newFile;
	}

	public String getResult() {
		return result;
	}

	public String getTitle() {
		return title;
	}

	/**
	 * Compare stat data files and store the result in the result field.
	 */
	public void runComparison() {
		ArrayList<PMStatEntry> statsDiff = getComparisonStats();

		if (!statsDiff.isEmpty()) {
			String[][] statsDiffStr = new String[statsDiff.size()][];
			int currentRow = 0;

			// gather comparison results in a string array
			for (PMStatEntry statEntry : statsDiff) {
				statsDiffStr[currentRow] = statEntry.toStringArray();
				currentRow++;
			}

			// apply format to each entry and set the result
			String format = getFormat(statsDiffStr);
			String curLine;
			for (String[] statEntry : statsDiffStr) {
				curLine = String.format(format, (Object[]) statEntry);
				curLine = curLine.contains(PMStatEntry.TIME) ? "\n" + curLine //$NON-NLS-1$
						: curLine;
				result += curLine;
			}
		} else{

		}
	}

	/**
	 * Return a PMStatEntry array with the result of the comparison between the
	 * old and new stat data files
	 * @return
	 */
	public ArrayList<PMStatEntry> getComparisonStats() {
		ArrayList<PMStatEntry> oldStats = collectStats(oldFile);
		ArrayList<PMStatEntry> newStats = collectStats(newFile);
		ArrayList<PMStatEntry> result = new ArrayList<PMStatEntry>();

		for (PMStatEntry oldEntry : oldStats) {
			for (PMStatEntry newEntry : newStats) {
				if (oldEntry.equalEvents(newEntry)) {
					result.add(oldEntry.compare(newEntry));
					continue;
				}
			}
		}

		return result;
	}

	/**
	 * Collect statistics entries from the specified stat data file.
	 *
	 * @param file file to collect from
	 * @return List containing statistics entries from the given file.
	 */
	public static ArrayList<PMStatEntry> collectStats(File statFile) {
		ArrayList<PMStatEntry> result = new ArrayList<PMStatEntry>();
		BufferedReader statReader = null;
		try {
			statReader = new BufferedReader(new FileReader(statFile));

			// pattern for a valid perf stat entry
			Pattern entryPattern = Pattern.compile(PMStatEntry.getString(Type.ENTRY_PATTERN));

			// pattern for last stat entry (seconds elapsed):
			Pattern totalTimePattern = Pattern.compile(PMStatEntry.getString(Type.TIME_PATTERN));

			String line;
			while((line = statReader.readLine()) != null ){
				line = line.trim();
				Matcher match = entryPattern.matcher(line);
				String samples, event, usage, units, delta, scale;
				PMStatEntry statEntry;

				if(match.find()){

					// extract information from groups
					samples = match.group(1);
					event = match.group(2);
					usage = match.group(4);
					units = match.group(5);
					delta = match.group(7);
					scale = match.group(11);

					// create stat entry
					statEntry = new PMStatEntry(toFloat(samples), event,
							toFloat(usage), units, toFloat(delta),
							toFloat(scale));

					// add stat entry to results list
					result.add(statEntry);

				} else if(line.contains(PMStatEntry.TIME)){

					// match seconds elapsed pattern
					match = totalTimePattern.matcher(line);
					if(match.find()){
						samples = match.group(1);
						event = match.group(2);
						delta = match.group(4);

						// create stat entry
						statEntry = new PMStatEntry(toFloat(samples),
								event, 0, null, toFloat(delta), 0);

						result.add(statEntry);
					}
				}
			}
			return result;
		} catch (FileNotFoundException e) {
			PerfPlugin.getDefault().openError(e, Messages.MsgError);
		} catch (IOException e) {
			PerfPlugin.getDefault().openError(e, Messages.MsgError);
		} finally {
			try {
				if (statReader != null) {
					statReader.close();
				}
			} catch (IOException e) {
				PerfPlugin.getDefault().openError(e, Messages.PerfResourceLeak_title);
			}
		}

		return result;
	}

	/**
	 * Get formatting string from unformatted table.
	 *
	 * @param table array to construct formatting for.
	 * @return Formatting string representing the proper way to format the given
	 *         table.
	 */
	public String getFormat(String[][] table) {
		// all entries have the same number of columns
		int[] maxCharLen = new int[table[0].length];

		// collect max number of characters per column
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[i].length; j++) {
				maxCharLen[j] = Math.max(maxCharLen[j], table[i][j].length());
			}
		}

		// prepare format arguments
		ArrayList<Integer> arguments = new ArrayList<Integer>();
		for (int length : maxCharLen) {
			arguments.add(length);
		}

		// generate format string
		String entryFormat = String.format(
				PMStatEntry.getString(Type.ENTRY_FORMAT),
				arguments.toArray());

		return entryFormat;
	}

	/**
	 * Get float representation of specified string.
	 *
	 * @param str String convert
	 * @return Float representation of string.
	 */
	public static float toFloat(String str) {
		try {
			// remove commas from number string representation
			return (str == null) ? 0
					: Float.parseFloat(str.replace(",", "")); //$NON-NLS-1$//$NON-NLS-2$
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
