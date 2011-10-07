package org.eclipse.linuxtools.internal.rdt.proxy;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.linuxtools.profiling.launch.IProcess;
import org.eclipse.ptp.remote.core.IRemoteProcess;

public class RDTProcess implements IProcess {

	private IRemoteProcess process;
	
	public RDTProcess(IRemoteProcess process) {
		this.process = process;
	}
	
	@Override
	public OutputStream getOutputStream() {
		return process.getOutputStream();
	}

	@Override
	public InputStream getInputStream() {
		return process.getInputStream();
	}

	@Override
	public InputStream getErrorStream() {
		return process.getErrorStream();
	}

	@Override
	public int exitValue() {
		return process.exitValue();
	}

}
