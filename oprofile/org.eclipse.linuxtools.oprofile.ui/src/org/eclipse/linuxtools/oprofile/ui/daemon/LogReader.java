/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/ 
package org.eclipse.linuxtools.oprofile.ui.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.linuxtools.oprofile.core.Oprofile;

/**
 * A Runnable to read oprofile's logfile and stuff it into the daemon view's TextViewer.
 */
public class LogReader implements Runnable, IRunnableWithProgress {
	private static long _lastModified = -1;
	private static String _contents = null;

	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		File logFile = new File(Oprofile.getLogFile());
		long modified = logFile.lastModified();
		if (modified != _lastModified) {
			/* Log file has changed read it in and stuff it into the
		    	log viewer. */
			_lastModified = modified;
			
			_contents = new String();
			
			try {
				BufferedReader reader = new BufferedReader(new FileReader(logFile));
				String line;
				while ((line = reader.readLine()) != null) {
					_contents += line + "\n";
				}
			} catch (FileNotFoundException e) {
				// The file doesn't exist or was erased. Try again next time.
				_contents = "Log file empty or does not exist.";
			} catch (IOException e) {
				// Error reading log. Try again next time.
				_lastModified = 0;
				_contents = "Error reading log file.";
			}
		}
	}
	
	public String getLogContents() {
		return _contents;
	}


	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		this.run();
	}
}