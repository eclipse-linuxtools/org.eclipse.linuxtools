package org.eclipse.linuxtools.valgrind.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ValgrindMockProcess extends Process {
	protected int exitcode;
	
	public ValgrindMockProcess(int exitcode) {
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
