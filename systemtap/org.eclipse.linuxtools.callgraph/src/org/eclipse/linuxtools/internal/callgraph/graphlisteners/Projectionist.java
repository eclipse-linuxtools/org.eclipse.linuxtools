/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.graphlisteners;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.internal.callgraph.StapGraph;
import org.eclipse.swt.widgets.Display;

/**
 * A Projectionist is the gguy that operates a movie camera.
 * @author chwang
 *
 */
public class Projectionist extends Job {
    private StapGraph graph;
    private int frameTime = 2000;
    private boolean pause;
    private boolean busy;


    /**
     * @param name
     * @param listener    -- the keyListener instantiating this class
     * @param time -- Amount of time between frames
     */
    public Projectionist(String name, StapGraph graph, int time) {
        super(name);
        this.graph = graph;
        this.frameTime = time;
        pause = false;
        busy = false;
    }

    @Override
    public IStatus run(IProgressMonitor monitor) {

        long snapshot = System.currentTimeMillis();
        while (true) {
            if (busy) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                continue;
            }

            if (pause) {
                return Status.OK_STATUS;
            }

            if (System.currentTimeMillis() - snapshot >= frameTime) {
                snapshot = System.currentTimeMillis();
                busy = true;
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        graph.drawNextNode();
                        busy = false;
                    }
                });

            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (monitor.isCanceled()) {
                break;
            }
        }

        return Status.CANCEL_STATUS;
    }

    /**
     * Projectionist will pause -- reschedule job to continue
     */
    public void pause() {
        pause = true;
    }

}
