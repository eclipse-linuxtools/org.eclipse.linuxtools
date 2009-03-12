/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ValgrindStubProcess extends Process {
	protected int exitcode;
	
	public ValgrindStubProcess(int exitcode) {
		this.exitcode = exitcode;
	}

	@Override
	public void destroy() {
	}

	@Override
	public int exitValue() {
		return exitcode;
	}

	@Override
	public InputStream getErrorStream() {
		return new InputStream() {
			@Override
			public int read() throws IOException {
				return -1;
			}			
		};
	}

	@Override
	public InputStream getInputStream() {
		return new InputStream() {
			@Override
			public int read() throws IOException {
				return -1;
			}			
		};
	}

	@Override
	public OutputStream getOutputStream() {
		return new OutputStream() {
			public void write(int b) throws IOException {
			}			
		};
	}

	@Override
	public int waitFor() throws InterruptedException {
		return exitcode;
	}

}
