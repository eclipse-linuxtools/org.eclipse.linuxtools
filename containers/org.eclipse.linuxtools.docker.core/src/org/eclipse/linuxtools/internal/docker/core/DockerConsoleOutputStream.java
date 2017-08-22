/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;

// Special Console OutputStream which supports listeners.

public class DockerConsoleOutputStream extends OutputStream {

	private OutputStream stream;
	private Map<String, Object> properties;

	ListenerList<IConsoleListener> consoleListeners;

	public DockerConsoleOutputStream(OutputStream stream) {
		this.stream = stream;
	}

	public DockerConsoleOutputStream setOutputStream(OutputStream stream) {
		this.stream = stream;
		return this;
	}

	public void setTerminalProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public Map<String, Object> getTerminalProperties() {
		return properties;
	}

	@Override
	public void write(byte[] b) throws IOException {
		if (stream != null) {
			stream.write(b);
		}
		notifyConsoleListeners(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (stream != null) {
			stream.write(b, off, len);
		}
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
		if (stream != null) {
			stream.close();
		}
	}

	@Override
	public void flush() throws IOException {
		if (stream != null) {
			stream.flush();
		}
	}

	public void addConsoleListener(IConsoleListener listener) {
		if (consoleListeners == null)
			consoleListeners = new ListenerList<>(ListenerList.IDENTITY);
		consoleListeners.add(listener);
	}

	public void removeConsoleListener(IConsoleListener listener) {
		if (consoleListeners != null)
			consoleListeners.remove(listener);
	}

	public void notifyConsoleListeners(byte[] b, int off, int len) {
		if (consoleListeners != null) {
			String output = new String(b, off, len);
			Object[] listeners = consoleListeners.getListeners();
			for (int i = 0; i < listeners.length; ++i) {
				((IConsoleListener) listeners[i]).newOutput(output);
			}
		}
	}

}
