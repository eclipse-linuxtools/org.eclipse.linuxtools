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

package org.eclipse.linuxtools.rpm.ui.editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.linuxtools.rpm.ui.editor.preferences.PreferenceConstants;

/**
 * This class is used to retrieve and manage the RPM macro 
 * proposals list.
 *
 */
public class RpmMacroProposalsList {

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
	public void buildMacroList() {
		for (String definedMacro: SpecfileScanner.DEFINED_MACROS){
			macroMap.put(definedMacro, "Buildin rpm macro");
			//TODO find way to provide info about buildin macros.
		}
		String macroProposalsPaths = Activator.getDefault()
				.getPreferenceStore().getString(
						PreferenceConstants.P_MACRO_PROPOSALS_FILESPATH);
		String[] paths = macroProposalsPaths.split(";");
		// paths must be reversed because the last value added
		// into a Map overwrites the first.
		paths = reverseStringArray(paths);
		for (int i = 0; i < paths.length; i++) {
			if (!paths[i].equals("")) {
				File pathFile = new File(paths[i]);
				if (pathFile.exists()){
					if (pathFile.isDirectory()){
						File[] macrosFiles =pathFile.listFiles();
						for (int j = 0; j < macrosFiles.length; j++) {
							addMacroToMap(macrosFiles[j].getAbsolutePath());
						}
					} else {
						addMacroToMap(paths[i]);
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
		String line = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename)));
			line = reader.readLine();
			String key = "", value = "";
			while (line != null) {
				if (line.startsWith("%")) {
					String[] item = line.split("\t+| ", 2);
					try {
						// Get values on more than one line
						if (line.trim().endsWith("\\")) {
							value = "\n";
							boolean isKeyLine = true;
							while (line.trim().endsWith("\\")) {
								if (isKeyLine) {
									isKeyLine = false;
									key = item[0];
									if (item.length > 1)
										value += item[1].replaceAll("\\", "\n\n");
								} else {
									value += line.substring(0,
											line.length() - 1).trim()
											+ "\n\t";
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
						toStringStr += key + ": " + value + "\n";
					} catch (Exception e) {
						line = reader.readLine();
						continue;
					}
					value = "";
					key = "";
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			SpecfileLog.logError(e);
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
	 * @param prefix
	 *            to search
	 * @return a <code>Map</code> of proposals.
	 */
	public Map<String, String> getProposals(String prefix) {
		Map<String, String> proposalsMap = new HashMap<String, String>(macroMap.size());
		int i = 0;
		for (Map.Entry<String, String> entry: macroMap.entrySet()) {
			// Get proposals for macro begin with { char too.
			if (entry.getKey().startsWith(prefix.replaceFirst("\\{", ""))) {
				proposalsMap.put(entry.getKey(), entry.getValue());
			}
			i++;
		}
		// Sort proposals
		Map<String, String> sortedMap = new TreeMap<String, String>(proposalsMap);
		return sortedMap;
	}

	/**
	 * Get the value for a given macro.
	 * 
	 * @param key
	 *            key to retrieve value.
	 * @return a string representation of the value
	 */
	public String getValue(String key) {
		String value = macroMap.get("%" + key);
		// get proposals for macro contain ? too.
		if (value == null) {
			value = macroMap.get(("%" + key).replaceFirst("\\?", ""));
		}
		return value;
	}
	
	/**
	 * Find a key in the macroMap
	 * 
	 * @param keyToFind
	 *            the key to find.
	 * @return return the value
	 */
	public boolean findKey(String keyToFind) {
		return macroMap.containsKey(keyToFind);
	}
	
	/**
	 * Return the ouput of the <code>rpm --eval</code> command for a given
	 * macro.
	 *  
	 * @param macroName
	 *            the macro name to eval.
	 * @return the resolved macro content.
	 */
	public static String getMacroEval(String macroName) {
		String 	eval = "";
		String[] cmd = {"rpm", "--eval", macroName};
		try {
			Process child = new ProcessBuilder(cmd).start();
			InputStream in = child.getInputStream();
			int c;
			while ((c = in.read()) != -1) {
				eval += ((char) c);
			}
			in.close();
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
