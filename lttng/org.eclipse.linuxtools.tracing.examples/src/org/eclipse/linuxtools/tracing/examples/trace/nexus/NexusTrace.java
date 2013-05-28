/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tracing.examples.trace.nexus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TraceValidationStatus;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;
import org.eclipse.linuxtools.tracing.examples.ui.Activator;

/**
 * Nexus trace type
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class NexusTrace extends TmfTrace
    implements
      ITmfEventParser,
      ITmfPersistentlyIndexable {

  /* 64 values of types according to the spec */
  private static final int NO_OF_EVENTS = 64;

  private static final int CHUNK_SIZE = 65536;
  private static final int EVENT_SIZE = 8;

  private TmfLongLocation fCurrent;

  private long fSize;
  private long fNbEvents;
  private long fOffset;
  private File fFile;
  private String[] fEventTypes;
  private TmfEvent fCurrentEvent;
  private FileChannel fFileChannel;
  private MappedByteBuffer fMappedByteBuffer;

  @Override
  public IStatus validate(IProject project, String path) {
    File f = new File(path);
    if (!f.exists()) {
      return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
          "File does not exist"); //$NON-NLS-1$
    }
    if (!f.isFile()) {
      return new Status(IStatus.ERROR, Activator.PLUGIN_ID, path
          + " is not a file"); //$NON-NLS-1$
    }
    String[] eventTypes = readHeader(f);
    if (eventTypes != null && eventTypes.length == NO_OF_EVENTS) {
      return new TraceValidationStatus(21,
          "org.eclipse.linuxtools.tracing.examples.trace.nexustrace"); //$NON-NLS-1$
    }
    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, path
        + " does not have a header"); //$NON-NLS-1$
  }

  @Override
  public ITmfLocation getCurrentLocation() {
    return fCurrent;
  }

  @Override
  public void initTrace(IResource resource, String path,
      Class<? extends ITmfEvent> type) throws TmfTraceException {
    super.initTrace(resource, path, type);
    fFile = new File(path);
    fSize = fFile.length();
    fEventTypes = readHeader(fFile);
    try {
      fFileChannel = new FileInputStream(fFile).getChannel();
      seek(0);
    } catch (IOException e) {
    }
  }

  private String[] readHeader(File file) {
    String header = new String();
    try (BufferedReader br = new BufferedReader(new FileReader(file));) {
      header = br.readLine();
    } catch (IOException e) {
    }
    fOffset = header.length() + 1;
    fNbEvents = (fSize - fOffset) / EVENT_SIZE;
    return header.split(",", NO_OF_EVENTS); //$NON-NLS-1$
  }

  @Override
  public double getLocationRatio(ITmfLocation location) {
    return ((TmfLongLocation) location).getLocationInfo().doubleValue()
        / fNbEvents;
  }

  @Override
  public ITmfContext seekEvent(ITmfLocation location) {
    TmfLongLocation nl = (TmfLongLocation) location;
    if (location == null) {
      nl = new TmfLongLocation(0L);
    }
    try {
      seek(nl.getLocationInfo());
    } catch (IOException e) {
    }
    return new TmfContext(nl, nl.getLocationInfo());
  }

  @Override
  public ITmfContext seekEvent(double ratio) {
    long rank = (long) (ratio * fNbEvents);
    try {
      seek(rank);
    } catch (IOException e) {
    }
    return new TmfContext(new TmfLongLocation(rank), rank);
  }

  private void seek(long rank) throws IOException {
    final long position = fOffset + rank * EVENT_SIZE;
    int size = Math.min((int) (fFileChannel.size() - position), CHUNK_SIZE);
    fMappedByteBuffer = fFileChannel.map(MapMode.READ_ONLY, position, size);
  }

  @Override
  protected void processEvent(ITmfEvent event) {
  }

  @Override
  public synchronized long getNbEvents() {
    return fNbEvents;
  }

  @Override
  public synchronized ITmfEvent getNext(ITmfContext context) {
    TmfEvent event = null;
    long ts = -1;
    int type = -1;
    int payload = -1;
    long pos = context.getRank();
    if (pos < getNbEvents()) {
      try {
        // if we are approaching the limit size, move to a new window
        if (fMappedByteBuffer.position() + EVENT_SIZE > fMappedByteBuffer
            .limit()) {
          seek(context.getRank());
        }
        // the trace format, is 32 bits for the time, 6 for the event
        // type,
        // 26 for the data.
        // all the 0x00 stuff are masks.
        ts = 0x00000000ffffffffL & fMappedByteBuffer.getInt();
        long data = 0x00000000ffffffffL & fMappedByteBuffer.getInt();
        type = (int) (data >> 26) & (0x03f);
        payload = (int) (data & 0x003FFFFFFL);
        // the time is in microseconds.
        TmfTimestamp timestamp = new TmfTimestamp(ts, -6);
        final String title = fEventTypes[type];
        // put the value in a field
        final TmfEventField tmfEventField = new TmfEventField(
            "value", payload, null); //$NON-NLS-1$
        // the field must be in an array
        final TmfEventField[] events = new TmfEventField[1];
        events[0] = tmfEventField;
        final TmfEventField content = new TmfEventField(
            ITmfEventField.ROOT_FIELD_ID, null, events);
        // create the event
        event = new TmfEvent(this, pos, timestamp, null, new TmfEventType(
            title, title, null), content, null);
        fCurrent = new TmfLongLocation(pos);
      } catch (IOException e) {
      }
    }
    if (event != null) {
      updateAttributes(context, event.getTimestamp());
      context.setLocation(getCurrentLocation());
      context.increaseRank();
      processEvent(event);
    }
    fCurrentEvent = event;
    return event;
  }

  @Override
  public ITmfEvent parseEvent(ITmfContext context) {
    return fCurrentEvent;
  }

  @Override
  public ITmfLocation restoreLocation(ByteBuffer bufferIn) {
    return new TmfLongLocation(bufferIn.getLong());
  }

  @Override
  public int getCheckpointSize() {
    return Long.SIZE / 8;
  }

}
