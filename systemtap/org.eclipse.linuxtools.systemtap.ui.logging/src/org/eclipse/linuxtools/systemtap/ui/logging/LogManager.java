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

package org.eclipse.linuxtools.systemtap.ui.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.LinkedList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.linuxtools.internal.systemtap.ui.logging.LogDaemon;
import org.eclipse.linuxtools.internal.systemtap.ui.logging.LogEntry;
import org.eclipse.linuxtools.internal.systemtap.ui.logging.LoggingPlugin;
import org.eclipse.linuxtools.internal.systemtap.ui.logging.preferences.PreferenceConstants;



/**
 * This class is responsible for all of the application's logging.
 * @author Henry Hughes, Ryan Morse
 */
/*
 * Strings in this class should not be externalized since they are
 * primarily for debugging purposes.
 */
public class LogManager implements IPropertyChangeListener {
	private LogManager() {}

	/**
	 * Initializes the logging service: hooks a property change listener to the Logging Plugin,
	 * causes self-initialization, and starts logging.
	 */
	public void begin() {
		LoggingPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		init();
	}

	/**
	 * Gets user logging preferences and prepares the logging system to operate.
	 */
	private void init() {
		BufferedWriter writer = null;
		IPreferenceStore store = LoggingPlugin.getDefault().getPreferenceStore();
		boolean enabled = store.getBoolean(PreferenceConstants.P_LOG_ENABLED);
		int level = Integer.parseInt(store.getString(PreferenceConstants.P_LOG_LEVEL));
		int type = Integer.parseInt(store.getString(PreferenceConstants.P_LOG_TYPE));
		String filename = store.getString(PreferenceConstants.P_LOG_FILE);

		if(enabled) {
			if(CONSOLE == type)
				writer = new BufferedWriter(new OutputStreamWriter(System.out));
			if(FILE == type) {
				try {
					File file = new File(filename);
					if(!file.exists()) {
						file.getParentFile().mkdirs();
						file.createNewFile();
					}
					writer = new BufferedWriter(new FileWriter(file));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(null != writer) {
				new LogDaemon(writer, level);
			}
		}
	}

	/**
	 * Static accessor method, returns the shared instance of LogManager.
	 * @return Returns the shared instance of <code>LogManager</code>
	 */
	public static LogManager getInstance() {
		return instance;
	}

	/**
	 * Overridden in order to unregister our property change event.
	 */
	@Override
	protected void finalize() throws Throwable {
		//unregister ourselves
		LoggingPlugin plugin = LoggingPlugin.getDefault();
		if(plugin != null) {
			IPreferenceStore store = plugin.getPreferenceStore();
			if(store != null)
				store.removePropertyChangeListener(this);
		}
		//let the JRE take care of the rest
		super.finalize();
	}

	/**
	 * Property change event handler, notifies the logging system when the user has changed
	 * logging related preferences.
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if(property.equals(PreferenceConstants.P_LOG_ENABLED) || property.equals(PreferenceConstants.P_LOG_FILE)
				|| property.equals(PreferenceConstants.P_LOG_LEVEL) || property.equals(PreferenceConstants.P_LOG_TYPE)) {
			init();
		}
	}

	/**
	 * Gets the entries needing to be logged still
	 * @return all entries that have not yet been logged
	 */
	public LinkedList<LogEntry> getEntries() {
		return entries;
	}

	private static LogManager instance = new LogManager();
	private LinkedList<LogEntry> entries = new LinkedList<LogEntry>();

	public static final int DEBUG=3,INFO=2,CRITICAL=1,FATAL=0;
	public static final int CONSOLE = 0, FILE = 1;
}
