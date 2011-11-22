package org.eclipse.linuxtools.internal.rdt.proxy;

import java.net.URI;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;

public class RDTFileProxy implements IRemoteFileProxy {

	private IRemoteFileManager manager;

	private void initialize(URI uri) {
		IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(uri);
		services.initialize();
		IRemoteConnection connection = services.getConnectionManager().getConnection(uri);
		manager = services.getFileManager(connection);
	}

	public RDTFileProxy(URI uri) {
		initialize(uri);
	}

	public RDTFileProxy(IProject project) {
		URI uri = project.getLocationURI();
		initialize(uri);
	}

	@Override
	public URI toURI(IPath path) {
		return manager.toURI(path);
	}

	@Override
	public URI toURI(String path) {
		return manager.toURI(path);
	}

	@Override
	public String toPath(URI uri) {
		return manager.toPath(uri);
	}

	@Override
	public String getDirectorySeparator() {
		return manager.getDirectorySeparator();
	}

	@Override
	public IFileStore getResource(String path) {
		return manager.getResource(path);
	}

}
