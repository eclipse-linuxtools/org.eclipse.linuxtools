/*******************************************************************************
 * Copyright (c) 2005-2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat, Inc. - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.core;

import java.io.InputStreamReader;
import java.io.InputStream;

import org.eclipse.osgi.util.NLS;

/**
 * Thread for reading input and output streams
 */
public class StreamReaderThread extends Thread {
	StringBuffer out;
	InputStreamReader in;

	public StreamReaderThread(InputStream in, StringBuffer out) {
		this.out = out;
		this.in = new InputStreamReader(in);
	}

	public void run() {
		int ch;
		try {
			while ((ch = in.read()) != -1) {
				out.append((char) ch);
			}
		} catch (Exception e) {
			out.append("\n" + NLS.bind(Messages.getString("StreamReaderThread.Read_error"), e.getMessage())); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
