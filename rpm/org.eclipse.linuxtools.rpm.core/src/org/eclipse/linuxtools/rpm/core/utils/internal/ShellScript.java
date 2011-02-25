/*
 * (c) 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
 */

package org.eclipse.linuxtools.rpm.core.utils.internal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.RPMCorePlugin;
import org.eclipse.linuxtools.rpm.core.internal.Messages;

/**
 * A utility class for constructing and executing shell scripts on the system.
 *
 */
public class ShellScript {
	
	private File script;
	private String scriptContents;
	private int successCode;
	
	/**
	 * Constructs a new shell script object.
	 * @param command the command to execute
	 * @param successCode the return code that indicated command execution was successful
	 */
	public ShellScript(String command, int successCode)  {
		scriptContents = "#!/bin/sh" + IRPMConstants.LINE_SEP + command; //$NON-NLS-1$
		this.successCode = successCode;
	}

	/**
	 * Executes the shell script without logging standard output.
	 * @return standard output from execution
	 * @throws CoreException if the operation fails
	 */
	public String execNoLog() throws CoreException {
		byte[] buf = scriptContents.getBytes();
		File file = null;
		try {
			file = RPMCorePlugin.getDefault().getShellScriptFile();
			BufferedOutputStream os = 
				new BufferedOutputStream(new FileOutputStream(file));
			for(int i = 0; i < buf.length; i++) {
				os.write(buf[i]);
		}
		os.close();
		} catch(IOException e) {
			String throw_message = Messages.getString("RPMCore.Error_trying_to_write_to__8") + //$NON-NLS-1$
			  file.getAbsolutePath();
			IStatus error = new Status(IStatus.ERROR, IRPMConstants.ERROR, 1,
					throw_message, null);
			throw new CoreException(error);
		}
        	script = file;
		Command.exec("chmod +x " + script.getAbsolutePath(), 0); //$NON-NLS-1$
        return Command.exec("sh " + script.getAbsolutePath(), successCode); //$NON-NLS-1$
	}
	
	/**
	 * Executes the shell script and logs standard output to the log file.
	 * @return standard output from execution
	 * @throws CoreException if the operation fails
	 */
	public String exec() throws CoreException {
		scriptContents += " >> " + 
			RPMCorePlugin.getDefault().getExternalLogFile().getAbsolutePath(); //$NON-NLS-1$
		return execNoLog();
	}
	
}
