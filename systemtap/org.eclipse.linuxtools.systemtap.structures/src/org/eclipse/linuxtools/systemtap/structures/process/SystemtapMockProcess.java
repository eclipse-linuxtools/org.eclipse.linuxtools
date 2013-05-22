/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.structures.process;

import java.io.InputStream;
import java.io.OutputStream;

public class SystemtapMockProcess extends Process {
	protected int exitcode;
	private String expected;
	private String output;
	private boolean block;
	private boolean stopped;

	public SystemtapMockProcess(int exitcode) {
		this.exitcode = exitcode;
	}

	/**
	 * Create a mock process which expects the given string and returns the give output
	 * @param expected
	 * @param out
	 * @param block if true then calls to wait will block until stop() is called.
	 */
	public SystemtapMockProcess(String expected, String output, boolean block) {
		this.exitcode = 0;
		this.expected = expected;
		this.output = output;
		this.block = block;
	}

	/**
	 * Returns true of the given command matches the processes expected command.
	 * @param command
	 * @return
	 */
	public boolean expecting(String command){
		if (this.expected.equals(command)){
			return true;
		}
		System.err.println("SystemtapGuiMockProcess: expecting " + this.expected + " but got " + command);
		return false;
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
			public int read() {
				return -1;
			}
		};
	}

	@Override
	public InputStream getInputStream() {
		return new InputStream() {
			@Override
			public int read() {
				return -1;
			}
		};
	}

	@Override
	public OutputStream getOutputStream() {
		return new OutputStream() {
			@Override
			public void write(int b) {
			}
		};
	}

	public void stop() {
		if (block) {
			synchronized (this) {
				this.notifyAll();
			}
		}
		this.stopped = true;
	}

	@Override
	public int waitFor() {
		if (block && !stopped) {
    		try {
    			synchronized (this) {
    				wait();
    			}
    		} catch (InterruptedException e) {
    			//Ignore
    		}
		}
		return exitcode;
	}

	public boolean expecting(String[] args) {
		StringBuilder command = new StringBuilder();
		for (String arg : args) {
			command.append(arg);
			command.append(" "); //$NON-NLS-1$
		}

		return expecting(command.toString().trim());
	}

}
