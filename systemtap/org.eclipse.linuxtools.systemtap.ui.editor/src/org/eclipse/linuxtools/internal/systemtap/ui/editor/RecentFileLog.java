/*******************************************************************************
 * Copyright (c) 2006
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.editor;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.linuxtools.systemtap.ui.editor.RecentFileMenuManager;



public final class RecentFileLog {
	private RecentFileLog() {
		File f = new File(fileName);
		try {
			if(!f.exists()) {
				f.createNewFile();
				EditorPlugin.getDefault().getDialogSettings().save(fileName);
			}
		} catch(IOException ioe) {}
	}
	
	public static RecentFileLog getInstance() {
		return log;
	}
	
	public static String getString(String key) {
		try {
			IDialogSettings settings = EditorPlugin.getDefault().getDialogSettings();
			settings.load(fileName);
			return settings.get(key);
		} catch(IOException e) {}
		return null;
	}
	
	private static boolean setString(String key, String val) {
		try {
			IDialogSettings settings = EditorPlugin.getDefault().getDialogSettings();
			settings.load(fileName);
			settings.put(key, val);
			settings.save(fileName);
			return true;
		} catch(IOException e) {}
		return false;
	}

	/**
	 * Updates the list of recently opened files.
	 * @param file the file that was just opened
	 */
	public static boolean updateRecentFiles(File file) {
		String[] temps = new String[RecentFileMenuManager.MAX_RECENT_FILES];
		String[] files = new String[RecentFileMenuManager.MAX_RECENT_FILES];
		String[] paths = new String[RecentFileMenuManager.MAX_RECENT_FILES];

		for(int i=0; i<RecentFileMenuManager.MAX_RECENT_FILES; i++) {
			files[i] = getString("file" + i);
			paths[i] = getString("path" + i);
		}

		if(!file.getAbsolutePath().equals(paths[0])) {
			temps[0] = files[0];
			temps[1] = paths[0];
			files[0] = file.getName();
			paths[0] = file.getAbsolutePath();
			
			if(file.getAbsolutePath().equals(paths[1])) {
				files[1] = temps[0];
				paths[1] = temps[1];
			} else {
				temps[2] = files[1];
				temps[3] = paths[1];
				files[1] = temps[0];
				paths[1] = temps[1];

				if(file.getAbsolutePath().equals(paths[2])) {
					files[2] = temps[2];
					paths[2] = temps[3];
				} else {
					temps[0] = files[2];
					temps[1] = paths[2];
					files[2] = temps[2];
					paths[2] = temps[3];

					if(file.getAbsolutePath().equals(paths[3])) {
						files[3] = temps[0];
						paths[3] = temps[1];
					} else {
						files[3] = temps[0];
						paths[3] = temps[1];
					}
				}
			}	
		}
		
		for(int i=0; i<RecentFileMenuManager.MAX_RECENT_FILES; i++) {
			if(null != files[i] && null != paths[i]) {
				setString("file" + i, files[i]);
				setString("path" + i, paths[i]);
				RecentFileMenuManager.update();
			}
		}
		return true;
	}

	private static final String fileName = EditorPlugin.getDefault().getStateLocation().append("recentFiles.log").toOSString();
	private static final RecentFileLog log = new RecentFileLog();
}
