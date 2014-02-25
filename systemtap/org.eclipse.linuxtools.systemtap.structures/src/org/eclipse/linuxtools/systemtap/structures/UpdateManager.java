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

package org.eclipse.linuxtools.systemtap.structures;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.linuxtools.systemtap.structures.listeners.IUpdateListener;



public class UpdateManager {
	public UpdateManager(int delay) {
		updateListeners = new ArrayList<>();
		stopped = false;
		disposed = false;
		restart(delay);
	}

	/**
	 * @since 2.2
	 */
	public void restart(int delay) {
		if (timer != null) {
			timer.cancel();
		}
		timer = new Timer("Update Manager", true); //$NON-NLS-1$
		timer.scheduleAtFixedRate(new Notify(), delay, delay);
	}

	/**
	 * Terminates the timer and removes all update listeners.
	 */
	public void stop() {
		if(!stopped) {
			stopped = true;
			timer.cancel();
			synchronized (updateListeners) {
				for(int i=0; i<updateListeners.size(); i++) {
					removeUpdateListener(updateListeners.get(i));
				}
			}
		}
	}

	public void addUpdateListener(IUpdateListener l) {
		if(!updateListeners.contains(l)) {
			updateListeners.add(l);
		}
	}
	public void removeUpdateListener(IUpdateListener l) {
		if(updateListeners.contains(l)) {
			updateListeners.remove(l);
		}
	}

	public boolean isRunning() {
		return !stopped;
	}

	public void dispose() {
		if(!disposed) {
			disposed = true;
			stop();
			timer = null;
			updateListeners = null;
		}
	}

	/**
	 * Handle any events that are timed to occur.
	 */
	private class Notify extends TimerTask {
		@Override
		public void run() {
			if(!stopped) {
				synchronized (updateListeners) {
					for(int i = 0; i < updateListeners.size(); i++) {
						(updateListeners.get(i)).handleUpdateEvent();
					}
				}
			}
		}
	}

	private Timer timer;
	private ArrayList<IUpdateListener> updateListeners;
	private boolean stopped;
	private boolean disposed;
}
