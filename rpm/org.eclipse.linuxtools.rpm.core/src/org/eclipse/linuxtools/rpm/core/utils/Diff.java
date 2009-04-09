/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.linuxtools.rpm.core.utils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.RPMCorePlugin;
import org.eclipse.linuxtools.rpm.core.utils.internal.ShellScript;

/**
 * A utility class for executing a diff.
 *
 */
public class Diff {
	
	private String diffCmd;
	
	/**
	 * Constructs a new object to run diff.
	 * @param baseDir the absolute path of the directory to run the diff in
	 * @param oldPath the path containing old resources to use in the diff
	 * @param newPath the path containing new resources to use in the diff
	 * @param excludes an array of paths to resources to exclude from the diff
	 * @param outputFile the path of the file to redirect the diff output to
	 */
	public Diff(String baseDir, String oldPath, String newPath, String[] excludes, 
			String outputFile) {
		Preferences prefs = RPMCorePlugin.getDefault().getPluginPreferences();
		String pathToDiff = prefs.getString(IRPMConstants.DIFF_CMD);
		
		diffCmd = "cd " + baseDir + " && "; //$NON-NLS-1$ //$NON-NLS-2$
		diffCmd += pathToDiff + " -uNr "; //$NON-NLS-1$
		diffCmd += "--ignore-matching-lines=POT-Creation-Date --exclude=autom4te.cache "; //$NON-NLS-1$
		for(int i=0; i < excludes.length; i++) {
			diffCmd += "--exclude=" + excludes[i] + " "; //$NON-NLS-1$ //$NON-NLS-2$
		}
		diffCmd += oldPath + " " + newPath + " "; //$NON-NLS-1$ //$NON-NLS-2$
		diffCmd += "> " + outputFile;
	}
	
	/**
	 * Executes the diff operation.
	 * @throws CoreException if the operation fails
	 */
	public void exec() throws CoreException {
		ShellScript script = new ShellScript(diffCmd, 1);
		script.execNoLog();
	}

}
