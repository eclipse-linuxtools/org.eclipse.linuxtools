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
