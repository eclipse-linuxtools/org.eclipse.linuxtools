/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.linuxtools.docker.ui.launch.IRunConsoleListener;

// Special Console OutputStream which supports listeners.

public class ConsoleOutputStream extends OutputStream {

	private OutputStream stream;

	ListenerList consoleListeners;

	public ConsoleOutputStream(OutputStream stream) {
		this.stream = stream;
	}

	@Override
	public void write(byte[] b) throws IOException {
		stream.write(b);
		notifyConsoleListeners(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		stream.write(b, off, len);
		notifyConsoleListeners(b, off, len);
	}

	@Override
	public void write(int arg0) throws IOException {
		byte[] b = new byte[1];
		b[0] = (byte) arg0;
		write(b);
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	@Override
	public void flush() throws IOException {
		stream.flush();
	}

	public void addConsoleListener(IRunConsoleListener listener) {
		if (consoleListeners == null)
			consoleListeners = new ListenerList(ListenerList.IDENTITY);
		consoleListeners.add(listener);
	}

	public void removeConsoleListener(IRunConsoleListener listener) {
		if (consoleListeners != null)
			consoleListeners.remove(listener);
	}

	public void notifyConsoleListeners(byte[] b, int off, int len) {
		if (consoleListeners != null) {
			String output = new String(b, off, len);
			Object[] listeners = consoleListeners.getListeners();
			for (int i = 0; i < listeners.length; ++i) {
				((IRunConsoleListener) listeners[i]).newOutput(output);
			}
		}
	}

}
