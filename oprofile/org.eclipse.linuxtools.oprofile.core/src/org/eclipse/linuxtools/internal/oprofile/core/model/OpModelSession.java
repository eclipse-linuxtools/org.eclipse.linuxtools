/*******************************************************************************
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kent Sebastian <ksebasti@redhat.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.oprofile.core.model;



/**
 * This class represents oprofile sessions. Sessions contain an image
 * of the profiled binary.
 */
public class OpModelSession {
    private static final String DEFAULT_SESSION_STRING = "current"; //$NON-NLS-1$

    private String name;
    private String printTabs = "";        //for nice output //$NON-NLS-1$
    private OpModelEvent[] events;

    public OpModelSession(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public OpModelEvent[] getEvents() {
        return events;
    }


    public void setEvents(OpModelEvent[] events) {
        this.events = events;
    }

    public boolean isDefaultSession() {
        return name.equals(DEFAULT_SESSION_STRING);
    }

    public void refreshModel() {
        if (events != null) {
            for (int i = 0; i < events.length; i++) {
                events[i].refreshModel();
            }
        }
    }

    public String toString(String tabs) {
        printTabs = tabs;
        String s = toString();
        printTabs = ""; //$NON-NLS-1$
        return s;
    }

    @Override
    public String toString() {
        String s = name + "\n"; //$NON-NLS-1$
        if (events != null) {
            for (int i = 0; i < events.length; i++) {
                s += printTabs + "Event: "; //$NON-NLS-1$
                s += events[i].toString(printTabs + "\t"); //$NON-NLS-1$
            }
        }
        return s;

    }
}
