/*
 * (c) 2004 Red Hat, Inc.
 *
 * This program is open source software licensed under the
 * Eclipse Public License ver. 1
*/

package org.eclipse.cdt.oprofile.ui.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.eclipse.cdt.oprofile.core.Oprofile;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.widgets.Display;

/**
 * A Runnable to read oprofile's logfile and stuff it into the daemon view's TextViewer.
 * @author Keith Seitz  <keiths@redhat.com>
 */
public class LogReader implements Runnable {
	private static long _lastModified = 0;
	private static TextViewer _viewer = null;
	
	/**
	 * Constructor
	 * @param viewer	the TextViewer in which to display log file
	 */
	public LogReader(TextViewer viewer) {
		_viewer = viewer;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		// Don't do anything if the logviewer was disposed since the last time
		if (_viewer != null) {
			File logFile = new File(Oprofile.getLogFile());
			long modified = logFile.lastModified();
			if (modified != _lastModified) {
				/* Log file has changed read it in and stuff it into the
			    	log viewer. */
				_lastModified = modified;
				
				try {
					BufferedReader reader = new BufferedReader(new FileReader(logFile));
					String contents = new String();
					String line;
					while ((line = reader.readLine()) != null) {
						contents += line + "\n";
					}
						
					// Content changed, notify logviewer
					_viewer.setDocument(new Document(contents));
				} catch (FileNotFoundException e) {
					// The file doesn't exist or was erased. Try again next time.
				} catch (IOException e) {
					// Error reading log. Try again next time.
				}
			}
			
			// Re-register ourselves to be run after the timeout
			Display.getCurrent().timerExec(1000, this);
		}
	}
	
	/**
	 * Stop updating log file view because viewer was disposed
	 */
	public void dispose() {
		_viewer = null;
	}
}