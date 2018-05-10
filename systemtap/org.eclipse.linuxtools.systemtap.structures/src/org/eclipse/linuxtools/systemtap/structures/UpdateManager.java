/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures;

import java.util.ArrayList;
import java.util.List;
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
     * Restart with the given delay.
     * @param delay The milliseconds to delay execution.
     * @since 3.0
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
    private List<IUpdateListener> updateListeners;
    private boolean stopped;
    private boolean disposed;
}
