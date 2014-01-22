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

package org.eclipse.linuxtools.systemtap.structures.runnable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.structures.listeners.IGobblerListener;

/**
 * A separate thread to listen to an InputStream and pull all the data
 * out of it. When data is found a new event is fired share the data with
 * any <code>IDataListener</code> that is listening.
 * @author Ryan Morse
 */
public class StreamGobbler implements Runnable {

	public StreamGobbler(InputStream is) {
		if(null != is) {
			this.is = is;
			line = new StringBuilder();
			listeners = new ArrayList<>();
		}
	}

	/**
	 * Spawns the new thread that this class will run in.  From the Runnable
	 * interface spawning the new thread automatically calls the run() method.
	 * This must be called by the implementing class in order to start the
	 * StreamGobbler.
	 */
	//Make sure to call this method to start the StreamGobbler
	public void start() {
		reader = new Thread(this, "StreamGobbler"); //$NON-NLS-1$
		reader.start();
	}

	/**
	 * Checks to see if the gobbler is still running.
	 * @return boolean representing whether or not it is sill running
	 */
	public boolean isRunning() {
		return (null != reader);
	}

	/**
	 * The main method of this class. It monitors the provided thread to see
	 * when new data is available and then appends it to its current list of
	 * data.  When a new line is read it will fire a DataEvent for listeners
	 * to get a hold of the data.
	 */
	@Override
	public void run() {
		if (reader != Thread.currentThread())
			return;

		try {
			int val = is.read();
			while(val != -1) {
				line.append((char)val);

				if ('\n' == val)
					this.fireNewDataEvent();

				val = is.read();
			}
		} catch (IOException ioe) {}	// If stream closed before thread shuts down
	}

	/**
	 * Stops the gobbler from monitoring the stream, and fires one last data event
	 * to make sure that listeners have the entire contents of what was read in
	 * from the stream.
	 */
	public synchronized void stop() {
		if (reader != null){
			try {
				// Wait for the reader thread to finish.
				reader.join();
			} catch (InterruptedException e) {
				// The thread was interrupted; nothing to do; finish stopping.
			}
			reader = null;
		}
		notify();
		// Fire one last time to ensure listeners have gotten everything.
		this.fireNewDataEvent();
	}

	/**	 * Method for getting the most recently read line from the stream.
	 * @return String representing the current line being read from the
	 * <code>InputStream</code>
	 */
	public String readLine() {
		return line.toString();
	}

	/**
	 * Gets rid of all internal references to objects.
	 */
	public void dispose() {
		if(isRunning())
			stop();
		line = null;
		reader = null;
		is = null;
	}

	/**
	 * Fires new events to everything that is monitoring this stream. Then clears
	 * the current line of data.
	 */
	private void fireNewDataEvent() {
		this.fireNewDataEvent(line.toString());
		line.delete(0, line.length());
	}

	public void fireNewDataEvent(String l) {
		synchronized (listeners) {
			for(int i = 0; i < listeners.size(); i++){
				listeners.get(i).handleDataEvent(l);
			}
		}
	}

	/**
	 * Registers the provided listener to get data events.
	 * @param l A listener that needs to monitor the stream.
	 */
	public void addDataListener(IGobblerListener l) {
		synchronized (listeners) {
			if(l != null && !listeners.contains(l)){
				listeners.add(l);
			}
		}
	}

	/**
	 * Unregisters the provided listener from getting new data events.
	 * @param l A listener that is monitoring the stream and should be removed
	 */
	public void removeDataListener(IGobblerListener l) {
		synchronized (listeners) {
			if(listeners.contains(l))
				listeners.remove(l);
		}
	}

	/**
	 * Returns a list of all of the <code>IGobblerListeners</code> that
	 * are listening for data events.
	 * @return ArrayList of all of the listeners registered.
	 */
	public ArrayList<IGobblerListener> getDataListeners() {
		return listeners;
	}

	private ArrayList<IGobblerListener> listeners;
	private StringBuilder line;
	private Thread reader;
	private InputStream is;
}