/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat, Inc. - initial API and implementation
 *    Elliott Baron <ebaron@redhat.com> - modification
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class ConsoleWriterThread extends Thread {
	InputStreamReader in;
	OutputStream out;

	public ConsoleWriterThread(InputStream in, OutputStream out) {
		this.out = out;
		this.in = new InputStreamReader(in);
	}

	public void run() {
		int ch;
		try {
			while ((ch = in.read()) != -1) {
				out.write(ch);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
