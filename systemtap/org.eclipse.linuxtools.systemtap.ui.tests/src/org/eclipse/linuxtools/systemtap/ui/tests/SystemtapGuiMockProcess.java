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

package org.eclipse.linuxtools.systemtap.ui.tests;

import java.io.InputStream;
import java.io.OutputStream;

public class SystemtapGuiMockProcess extends Process {
	protected int exitcode;
	
	public SystemtapGuiMockProcess(int exitcode) {
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

	@Override
	public int waitFor() {
		return exitcode;
	}

}
