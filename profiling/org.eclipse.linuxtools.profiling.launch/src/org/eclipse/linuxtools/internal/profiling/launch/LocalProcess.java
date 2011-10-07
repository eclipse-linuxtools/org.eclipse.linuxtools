/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.profiling.launch;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.linuxtools.profiling.launch.IProcess;

public class LocalProcess implements IProcess {

	private Process process;
	
	public LocalProcess(Process process) {
		this.process = process;
	}
	public OutputStream getOutputStream() {
		return process.getOutputStream();
	}

	public InputStream getInputStream() {
		return process.getInputStream();
	}

	public InputStream getErrorStream() {
		return process.getErrorStream();
	}
	
	public int exitValue() {
		return process.exitValue();
	}

}
