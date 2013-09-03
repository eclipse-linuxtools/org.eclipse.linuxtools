/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.linuxtools.internal.rpm.ui.editor.preferences.PreferenceConstants;
import org.eclipse.linuxtools.internal.rpm.ui.editor.scanners.SpecfileScanner;
import org.eclipse.linuxtools.rpm.core.utils.Utils;

/**
 * This class is used to retrieve and manage the RPM macro
 * proposals list.
 *
 */
public class RpmMacroProposalsList {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private Map<String, String> macroMap = new HashMap<String, String>();

	private String toStringStr;

	/**
	 * Default contructor
	 */
	public RpmMacroProposalsList() {
		buildMacroList();
	}

	/**
	 * Build the macro list.
	 */
	public final void buildMacroList() {
		for (String definedMacro: SpecfileScanner.DEFINED_MACROS){
			macroMap.put(definedMacro, Messages.RpmMacroProposalsList_0);
			//TODO find way to provide info about buildin macros.
		}
		String macroProposalsPaths = Activator.getDefault()
				.getPreferenceStore().getString(
						PreferenceConstants.P_MACRO_PROPOSALS_FILESPATH);
		String[] paths = macroProposalsPaths.split(";"); //$NON-NLS-1$
		// paths must be reversed because the last value added
		// into a Map overwrites the first.
		paths = reverseStringArray(paths);
		for (String path : paths) {
			if (!path.equals(EMPTY_STRING)) {
				File pathFile = new File(path);
				if (pathFile.exists()) {
					if (pathFile.isDirectory()) {
						File[] macrosFiles = pathFile.listFiles();
						for (File macrosFile : macrosFiles) {
							addMacroToMap(macrosFile.getAbsolutePath());
						}
					} else {
						addMacroToMap(path);
					}
				}
			}
		}
	}

	/**
	 * Add macro definition to the map
	 *
	 * @param filename
	 *            macro file definition.
	 */
	private void addMacroToMap(String filename) {
		String line = EMPTY_STRING;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename)));
			line = reader.readLine();
			String key = EMPTY_STRING, value = EMPTY_STRING;
			while (line != null) {
				if (line.startsWith("%")) { //$NON-NLS-1$
					String[] item = line.split("\t+| ", 2); //$NON-NLS-1$
					try {
						// Get values on more than one line
						if (line.trim().endsWith("\\")) { //$NON-NLS-1$
							value = "\n"; //$NON-NLS-1$
							boolean isKeyLine = true;
							while (line.trim().endsWith("\\")) { //$NON-NLS-1$
								if (isKeyLine) {
									isKeyLine = false;
									key = item[0];
									if (item.length > 1) {
										value += item[1].replaceAll("\\\\", "\n\n");  //$NON-NLS-1$//$NON-NLS-2$
									}
								} else {
									value += line.substring(0,
											line.length() - 1).trim()
											+ "\n\t"; //$NON-NLS-1$
								}
								line = reader.readLine();
							}
						} else {
							key = item[0];
							value = item[1];
						}
						key = key.trim();
						value = value.trim();
						macroMap.put(key, value);
						toStringStr += key + ": " + value + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
					} catch (Exception e) {
						line = reader.readLine();
						continue;
					}
					value = EMPTY_STRING;
					key = EMPTY_STRING;
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			SpecfileLog.logError(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Reverse a <code>String</code> array.
	 *
	 * @param stringArrayToReverse
	 *            the string array to reverse.
	 * @return the reversed <code>String</code> array.
	 */
	private String[] reverseStringArray(String[] stringArrayToReverse) {
		int left = 0;
		int right = stringArrayToReverse.length - 1;
		while (left < right) {
			String tmp = stringArrayToReverse[left];
			stringArrayToReverse[left] = stringArrayToReverse[right];
			stringArrayToReverse[right] = tmp;
			left++;
			right--;
		}
		return stringArrayToReverse;
	}

	/**
	 * Get proposals for a given prefix
	 *
	 * @param prefix The prefix to search.
	 * @return a <code>Map</code> of proposals.
	 */
	public Map<String, String> getProposals(String prefix) {
		Map<String, String> proposalsMap = new HashMap<String, String>(macroMap.size());
		for (Map.Entry<String, String> entry: macroMap.entrySet()) {
			// Get proposals for macro begin with { char too.
			if (entry.getKey().startsWith(prefix.replaceFirst("\\{", EMPTY_STRING))) { //$NON-NLS-1$
				proposalsMap.put(entry.getKey(), entry.getValue());
			}
		}
		// Sort proposals
		return new TreeMap<String, String>(proposalsMap);
	}

	/**
	 * Get the value for a given macro.
	 *
	 * @param key Key to retrieve value.
	 * @return a string representation of the value
	 */
	public String getValue(String key) {
		String value = macroMap.get("%" + key); //$NON-NLS-1$
		// get proposals for macro contain ? too.
		if (value == null) {
			value = macroMap.get(("%" + key).replaceFirst("\\?", EMPTY_STRING)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return value;
	}

	/**
	 * Find a key in the macroMap
	 *
	 * @param keyToFind The key to find.
	 * @return return the value
	 */
	public boolean findKey(String keyToFind) {
		return macroMap.containsKey(keyToFind);
	}

	/**
	 * Return the ouput of the <code>rpm --eval</code> command for a given
	 * macro.
	 *
	 * @param macroName The macro name to eval.
	 * @return the resolved macro content.
	 */
	public static String getMacroEval(String macroName) {
		String eval = EMPTY_STRING;
		try {
			eval = Utils.runCommandToString( "rpm", "--eval", macroName); //$NON-NLS-1$//$NON-NLS-2$
		} catch (IOException e) {
			SpecfileLog.logError(e);
		}
		return eval.trim();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return toStringStr;
	}

}
