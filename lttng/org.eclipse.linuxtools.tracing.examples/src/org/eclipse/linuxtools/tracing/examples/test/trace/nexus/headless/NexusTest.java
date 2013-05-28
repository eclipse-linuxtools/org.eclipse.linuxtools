/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tracing.examples.test.trace.nexus.headless;

import java.util.Vector;

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tracing.examples.trace.nexus.NexusTrace;

/**
 * Test and benchmark reading a CTF LTTng kernel trace.
 *
 * @author Matthew Khouzam
 */
public class NexusTest {

    /**
     * Run the benchmark.
     *
     * @param args
     *            The command-line arguments
     */
    public static void main(final String[] args) {
        final String TRACE_PATH = "/home/ematkho/nexusTrace";
        final int NUM_LOOPS = 1;

        // Change this to enable text output
        final boolean USE_TEXT = true;

        // Work variables
        Long nbEvent = 0L;
        final Vector<Double> benchs = new Vector<>();
        NexusTrace trace = null;
        long start, stop;
        for (int loops = 0; loops < NUM_LOOPS; loops++) {
            nbEvent = 0L;
            trace = new NexusTrace();
            try {
                trace.initTrace(null, TRACE_PATH, TmfEvent.class);
            } catch (final TmfTraceException e) {
                loops = NUM_LOOPS + 1;
                break;
            }

            start = System.nanoTime();
            if (nbEvent != -1) {
                final TmfContext context = (TmfContext) trace.seekEvent(0);

                start = System.nanoTime();
                TmfEvent current = (TmfEvent) trace.getNext(context);
                while (current != null) {
                    nbEvent++;
                    if (USE_TEXT) {

                        System.out.println("Event " + nbEvent + " Time "
                                + current.getTimestamp().toString() + " type " +
                                current.getType().getName()
                                + " on CPU " + current.getSource() + " " +
                                current.getContent().toString());
                    }
                    // advance the trace to the next event.
                    boolean hasMore = trace.getNbEvents() > context.getRank();
                    if (hasMore) {
                        // you can know the trace has more events.
                    }
                    current = (TmfEvent) trace.getNext(context);

                }
            }
            stop = System.nanoTime();
            System.out.print('.');
            final double time = (stop - start) / (double) nbEvent;
            benchs.add(time);
        }
        System.out.println("");
        double avg = 0;
        for (final Double val : benchs) {
            avg += val;
        }
        avg /= benchs.size();
        System.out.println("Time to read = " + avg + " events/ns");
        for (final Double val : benchs) {
            System.out.print(val);
            System.out.print(", ");
        }

    }
}
