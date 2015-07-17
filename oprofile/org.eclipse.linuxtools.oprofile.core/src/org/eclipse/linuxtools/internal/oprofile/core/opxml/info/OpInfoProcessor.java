/*******************************************************************************
 * Copyright (c) 2004,2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com>
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.core.opxml.info;

import org.eclipse.linuxtools.internal.oprofile.core.daemon.OpInfo;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.OprofileSAXHandler;
import org.eclipse.linuxtools.internal.oprofile.core.opxml.XMLProcessor;
import org.xml.sax.Attributes;


/**
 * Opxml processor for the "info" command.
 * @see org.eclipse.linuxtools.internal.oprofile.core.opxml.OpxmlRunner
 */
public class OpInfoProcessor extends XMLProcessor {
    // Other XMLProcessors used by this processor
    private DefaultsProcessor defaultsProc;
    private EventListProcessor eventListProc;

    // XML tags processed by this processor
    public static final String NUM_COUNTERS_TAG = "num-counters"; //$NON-NLS-1$
    public static final String DEFAULTS_TAG = "defaults"; //$NON-NLS-1$
    public static final String EVENT_LIST_TAG = "event-list"; //$NON-NLS-1$
    public static final String CPU_FREQUENCY_TAG = "cpu-frequency"; //$NON-NLS-1$
    public static final String TIMER_MODE = "timer-mode";  //$NON-NLS-1$

    public OpInfoProcessor() {
        defaultsProc = new DefaultsProcessor();
        eventListProc = new EventListProcessor();
    }

    @Override
    public void startElement(String name, Attributes attrs, Object callData) {
        if (name.equals(DEFAULTS_TAG)) {
            OprofileSAXHandler.getInstance(callData).push(defaultsProc);
        } else if (name.equals(EVENT_LIST_TAG)) {
            OprofileSAXHandler.getInstance(callData).push(eventListProc);
            eventListProc.startElement(name, attrs, callData);
        } else {
            super.startElement(name, attrs, callData);
        }
    }

    @Override
    public void endElement(String name, Object callData) {
        if (name.equals(CPU_FREQUENCY_TAG)) {
            double speed = Double.parseDouble(characters);
            OpInfo info = (OpInfo) callData;
            info.setCPUSpeed(speed);
        } else if (name.equals(TIMER_MODE)) {
            boolean timerMode = Boolean.parseBoolean(characters);
            OpInfo info = (OpInfo) callData;
            info.setTimerMode(timerMode);
        } else if (name.equals(NUM_COUNTERS_TAG)) {
            int numCounters = 0;
            try {
                numCounters = Integer.parseInt(characters);
            } catch (NumberFormatException nfe) {
            }
            OpInfo info = (OpInfo) callData;
            info.setNrCounters(numCounters);
        } else if (name.equals(EVENT_LIST_TAG)) {
            OpInfo info = (OpInfo) callData;
            info.setEvents(eventListProc.getCounterNum(), eventListProc.getEvents());
        }
    }
}
