/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoPreferenceConstants;
import org.eclipse.linuxtools.rpm.createrepo.ICreaterepoConstants;

/**
 * Class to create and format the commands.
 */
public class CreaterepoCommandCreator {

	// commands that are either used or not used
	private static final String[] BOOLEAN_COMMANDS = {
		// general
		CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME,
		CreaterepoPreferenceConstants.PREF_GENERATE_DB,
		CreaterepoPreferenceConstants.PREF_IGNORE_SYMLINKS,
		CreaterepoPreferenceConstants.PREF_PRETTY_XML,
		CreaterepoPreferenceConstants.PREF_CHECK_TS,
	};

	// commands that determine used state by arguments passed with it
	private static final String[] STRING_COMMANDS = {
		// general
		CreaterepoPreferenceConstants.PREF_CHECKSUM,
		CreaterepoPreferenceConstants.PREF_COMPRESSION_TYPE,
	};

	private static final String[] STRING_META_COMMANDS = {
		// metadata
		CreaterepoPreferenceConstants.PREF_REVISION,
		CreaterepoPreferenceConstants.PREF_DISTRO_TAG,
		CreaterepoPreferenceConstants.PREF_CONTENT_TAG,
		CreaterepoPreferenceConstants.PREF_REPO_TAG,
	};

	private static final String[] STRING_DELTA_COMMANDS = {
		// deltas
		CreaterepoPreferenceConstants.PREF_OLD_PACKAGE_DIRS,
	};

	// commands that determine used state by int passed with it
	private static final String[] INT_COMMANDS = {
		// general
		CreaterepoPreferenceConstants.PREF_WORKERS,
		CreaterepoPreferenceConstants.PREF_CHANGELOG_LIMIT,
	};

	private static final String[] INT_DELTA_COMMANDS = {
		// deltas
		CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE,
		CreaterepoPreferenceConstants.PREF_NUM_DELTAS,
	};

	private IEclipsePreferences projectPreferences;
	private IPreferenceStore preferenceStore;
	private boolean project;
	private boolean delta;

	public CreaterepoCommandCreator(IEclipsePreferences projectPreferences) {
		project = Activator.isProjectPrefEnabled();
		delta = Activator.isDeltaPrefEnabled();
		this.projectPreferences = projectPreferences;
		preferenceStore = Activator.getDefault().getPreferenceStore();
	}

	/**
	 * Get all the command arguments.
	 *
	 * @return A list of all the command arguments.
	 */
	public List<String> getCommands() {
		List<String> commands = new ArrayList<String>();
		commands.addAll(prepareBooleanCommands());
		commands.addAll(prepareStringCommands());
		commands.addAll(prepareIntCommands());
		return commands;
	}

	/**
	 * These commands are either added to the execution or not, depending
	 * on the enabled status from the preferences.
	 *
	 * @return The command options to add.
	 */
	public List<String> prepareBooleanCommands() {
		List<String> commands = new ArrayList<String>();
		if (delta) {
			commands.add(ICreaterepoConstants.DASH.concat(CreaterepoPreferenceConstants.PREF_DELTA_ENABLE));
		}
		for (String arg : BOOLEAN_COMMANDS) {
			// if project preferences are enabled, use the preferences from there
			boolean value = project ? projectPreferences.getBoolean(arg, preferenceStore.getDefaultBoolean(arg))
					: preferenceStore.getBoolean(arg);
			// if the value returned is true, that means the switch should be added
			if (value) {
				if (arg.equals(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME)) {
					arg = ICreaterepoConstants.DASH.concat("unique-").concat(arg); //$NON-NLS-1$
				} else {
					arg = ICreaterepoConstants.DASH.concat(arg);
				}
				commands.add(arg);
			} else {
				// only add the switch if its one of these options
				if (arg.equals(CreaterepoPreferenceConstants.PREF_UNIQUE_MD_NAME)) {
					arg = ICreaterepoConstants.DASH.concat("simple-").concat(arg); //$NON-NLS-1$
					commands.add(arg);
				} else if (arg.equals(CreaterepoPreferenceConstants.PREF_GENERATE_DB)) {
					arg = ICreaterepoConstants.DASH.concat("no-").concat(arg); //$NON-NLS-1$
					commands.add(arg);
				}
			}
		}
		return commands;
	}

	/**
	 * Prepare the commands that require a string passed with them
	 * when executing.
	 *
	 * @return The command options to add.
	 */
	public List<String> prepareStringCommands() {
		List<String> commands = new ArrayList<String>();
		for (String arg : STRING_COMMANDS) {
			String value = project ? projectPreferences.get(arg, preferenceStore.getDefaultString(arg))
					: preferenceStore.getString(arg);
			arg = ICreaterepoConstants.DASH.concat(arg);
			if (!value.isEmpty()) {
				commands.add(arg);
				commands.add(value);
			}
		}
		for (String arg : STRING_META_COMMANDS) {
			String value = projectPreferences.get(arg, preferenceStore.getDefaultString(arg));
			arg = ICreaterepoConstants.DASH.concat(arg);
			for (String tag : value.split(ICreaterepoConstants.DELIMITER)) {
				if (!tag.isEmpty()) {
					commands.add(arg);
					commands.add(tag);
				}
			}
		}
		if (delta) {
			for (String arg : STRING_DELTA_COMMANDS) {
				String value = projectPreferences.get(arg, preferenceStore.getDefaultString(arg));
				arg = ICreaterepoConstants.DASH.concat(arg);
				for (String dirs : value.split(ICreaterepoConstants.DELIMITER)) {
					if (!dirs.isEmpty()) {
						commands.add(arg);
						commands.add(dirs);
					}
				}
			}
		}
		return commands;
	}

	/**
	 * Prepare the commands that require an integer passed with them
	 * when executing. Differs from prepareStringCommands() by how it
	 * retrieves the values from the preferences.
	 *
	 * @return The command options to add.
	 */
	public List<String> prepareIntCommands() {
		List<String> commands = new ArrayList<String>();
		if (delta) {
			for (String arg : INT_DELTA_COMMANDS) {
				long value = projectPreferences.getInt(arg, preferenceStore.getDefaultInt(arg));
				if (arg.equals(CreaterepoPreferenceConstants.PREF_MAX_DELTA_SIZE)) {
					// 1048576 = bytes in a megabyte
					value *= 1048576;
				}
				arg = ICreaterepoConstants.DASH.concat(arg);
				commands.add(arg);
				commands.add(Long.toString(value));
			}
		}
		for (String arg : INT_COMMANDS) {
			// if project preferences are enabled, use the preferences from there
			int value = project ? projectPreferences.getInt(arg, preferenceStore.getDefaultInt(arg))
					: preferenceStore.getInt(arg);
			arg = ICreaterepoConstants.DASH.concat(arg);
			commands.add(arg);
			commands.add(Integer.toString(value));
		}
		return commands;
	}

}
