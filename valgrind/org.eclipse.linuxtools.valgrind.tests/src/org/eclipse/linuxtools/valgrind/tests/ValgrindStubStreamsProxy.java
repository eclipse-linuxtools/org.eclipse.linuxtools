package org.eclipse.linuxtools.valgrind.tests;

import java.io.IOException;

import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;

public class ValgrindStubStreamsProxy implements IStreamsProxy {

	public IStreamMonitor getErrorStreamMonitor() {
		return null;
	}

	public IStreamMonitor getOutputStreamMonitor() {
		return null;
	}

	public void write(String input) throws IOException {
	}

}
