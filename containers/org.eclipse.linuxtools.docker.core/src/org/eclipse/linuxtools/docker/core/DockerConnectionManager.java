/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.docker.core;

import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.docker.core.DefaultDockerConnectionSettingsFinder;
import org.eclipse.linuxtools.internal.docker.core.DefaultDockerConnectionStorageManager;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerRefreshManager;

public class DockerConnectionManager {

	private static DockerConnectionManager instance;

	private List<IDockerConnection> connections;
	private ListenerList connectionManagerListeners;

	private IDockerConnectionSettingsFinder connectionSettingsFinder = new DefaultDockerConnectionSettingsFinder();
	private IDockerConnectionStorageManager connectionStorageManager = new DefaultDockerConnectionStorageManager();

	public static DockerConnectionManager getInstance() {
		if (instance == null) {
			instance = new DockerConnectionManager();
		}
		return instance;
	}

	private DockerConnectionManager() {
		reloadConnections();
	}

	public void reloadConnections() {
		this.connections = connectionStorageManager.loadConnections();
		for (IDockerConnection connection : connections) {
			try {
				connection.open(true);
				notifyListeners(connection,
						IDockerConnectionManagerListener.ADD_EVENT);
			} catch (DockerException e) {
				Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
						e.getMessage()));
			}
		}
	}

	public void setConnectionSettingsFinder(
			final IDockerConnectionSettingsFinder connectionSettingsFinder) {
		this.connectionSettingsFinder = connectionSettingsFinder;
	}

	public void setConnectionStorageManager(
			final IDockerConnectionStorageManager connectionStorageManager) {
		this.connectionStorageManager = connectionStorageManager;
	}

	public void saveConnections() {
		this.connectionStorageManager.saveConnections(this.connections);
	}

	public IDockerConnection[] getConnections() {
		return connections.toArray(new IDockerConnection[connections.size()]);
	}

	public IDockerConnection findConnection(final String name) {
		if (name != null) {
			for (IDockerConnection connection : connections) {
				if (connection.getName().equals(name))
					return connection;
			}
		}
		return null;
	}

	public void addConnection(final IDockerConnection dockerConnection) throws DockerException {
		if(!dockerConnection.isOpen()) {
			dockerConnection.open(true);
		}
		connections.add(dockerConnection);
		saveConnections();
		notifyListeners(dockerConnection,
				IDockerConnectionManagerListener.ADD_EVENT);
	}

	public void removeConnection(final IDockerConnection connection) {
		connections.remove(connection);
		saveConnections();
		notifyListeners(connection,
				IDockerConnectionManagerListener.REMOVE_EVENT);
		DockerContainerRefreshManager.getInstance()
				.removeContainerRefreshThread(connection);
	}

	public void addConnectionManagerListener(
			IDockerConnectionManagerListener listener) {
		if (connectionManagerListeners == null)
			connectionManagerListeners = new ListenerList(ListenerList.IDENTITY);
		connectionManagerListeners.add(listener);
	}

	public void removeConnectionManagerListener(
			IDockerConnectionManagerListener listener) {
		if (connectionManagerListeners != null)
			connectionManagerListeners.remove(listener);
	}

	@SuppressWarnings("deprecation")
	public void notifyListeners(final IDockerConnection connection,
			final int type) {
		if (connectionManagerListeners != null) {
			Object[] listeners = connectionManagerListeners.getListeners();
			for (int i = 0; i < listeners.length; ++i) {
				if (listeners[i] instanceof IDockerConnectionManagerListener2) {
					((IDockerConnectionManagerListener2) listeners[i])
							.changeEvent(connection, type);
				} else {
					// keeping the call to the old method for the listeners that
					// are
					// interested
					((IDockerConnectionManagerListener) listeners[i])
							.changeEvent(type);
				}
			}
		}
	}

	public List<IDockerConnectionSettings> findConnectionSettings() {
		// delegate the call to a utility class.
		return connectionSettingsFinder.findConnectionSettings();
	}

}
