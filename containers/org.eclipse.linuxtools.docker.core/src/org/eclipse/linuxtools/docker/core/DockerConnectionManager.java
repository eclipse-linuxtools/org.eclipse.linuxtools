/*******************************************************************************
 * Copyright (c) 2014, 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.docker.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings.BindingType;
import org.eclipse.linuxtools.internal.docker.core.DefaultDockerConnectionSettingsFinder;
import org.eclipse.linuxtools.internal.docker.core.DefaultDockerConnectionStorageManager;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerRefreshManager;
import org.eclipse.linuxtools.internal.docker.core.TCPConnectionSettings;
import org.eclipse.linuxtools.internal.docker.core.UnixSocketConnectionSettings;

public class DockerConnectionManager {

	/**
	 * @deprecated see the {@link IDockerConnectionStorageManager} implementation instead.
	 */
	@Deprecated
	public final static String CONNECTIONS_FILE_NAME = "dockerconnections.xml"; //$NON-NLS-1$

	private static DockerConnectionManager instance;

	private List<IDockerConnection> connections;
	private ListenerList<IDockerConnectionManagerListener> connectionManagerListeners;

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
				notifyListeners(connection,
						IDockerConnectionManagerListener.ADD_EVENT);
		}
		List<IDockerConnectionSettings> settings = connectionSettingsFinder
				.getKnownConnectionSettings();
		for (IDockerConnectionSettings setting : settings) {
			if (setting != null) {
				IDockerConnection conn;
				if (setting.getType().equals(BindingType.UNIX_SOCKET_CONNECTION)) {
					UnixSocketConnectionSettings usetting = (UnixSocketConnectionSettings) setting;
					conn = new DockerConnection.Builder().name(usetting.getName())
							.unixSocketConnection(usetting);
				} else {
					TCPConnectionSettings tsetting = (TCPConnectionSettings) setting;
					conn = new DockerConnection.Builder().name(tsetting.getName())
							.tcpConnection(tsetting);
				}
				// add the connection but do not notify the listeners to avoid
				// flickering on the Docker Explorer view for each entry
				addConnection(conn, false);
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

	/**
	 * @return an immutable {@link List} of the {@link IDockerConnection} names
	 */
	public List<String> getConnectionNames() {
		final List<String> connectionNames = new ArrayList<>();
		for (IDockerConnection connection : this.connections) {
			connectionNames.add(connection.getName());
		}
		return Collections.unmodifiableList(connectionNames);
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

	/**
	 * Adds the given connection and notifies all registered
	 * {@link IDockerConnectionManagerListener}
	 * 
	 * @param dockerConnection
	 *            the connection to add
	 */
	public void addConnection(final IDockerConnection dockerConnection) {
		addConnection(dockerConnection, true);
	}

	/**
	 * Adds the given connection and notifies optionally all registered
	 * {@link IDockerConnectionManagerListener}
	 * 
	 * @param dockerConnection
	 *            the connection to add
	 * @param notifyListeners
	 *            flag to indicate if registered
	 *            {@link IDockerConnectionManagerListener} should be notified
	 *            about the {@link IDockerConnection} addition.
	 */
	public void addConnection(final IDockerConnection dockerConnection,
			final boolean notifyListeners) {
		if (!connections.contains(dockerConnection)) {
			connections.add(dockerConnection);
			saveConnections();
			if (notifyListeners) {
				notifyListeners(dockerConnection,
						IDockerConnectionManagerListener.ADD_EVENT);
			}
		}
	}

	public void removeConnection(final IDockerConnection connection) {
		connections.remove(connection);
		saveConnections();
		notifyListeners(connection,
				IDockerConnectionManagerListener.REMOVE_EVENT);
		DockerContainerRefreshManager.getInstance()
				.removeContainerRefreshThread(connection);
	}

	/**
	 * Notifies that a connection was renamed.
	 */
	@Deprecated
	public void notifyConnectionRename() {
		saveConnections();
		notifyListeners(IDockerConnectionManagerListener.RENAME_EVENT);
	}

	public void addConnectionManagerListener(
			IDockerConnectionManagerListener listener) {
		if (connectionManagerListeners == null)
			connectionManagerListeners = new ListenerList<>(
					ListenerList.IDENTITY);
		connectionManagerListeners.add(listener);
	}

	public void removeConnectionManagerListener(
			IDockerConnectionManagerListener listener) {
		if (connectionManagerListeners != null)
			connectionManagerListeners.remove(listener);
	}

	/**
	 * Notifies all listeners that a change occurred on a connection
	 * 
	 * @param type
	 *            the type of change
	 * @deprecated use
	 *             {@link DockerConnectionManager#notifyListeners(IDockerConnection, int)}
	 *             instead
	 */
	@Deprecated
	public void notifyListeners(int type) {
		if (connectionManagerListeners != null) {
			for (IDockerConnectionManagerListener listener : connectionManagerListeners) {
				listener.changeEvent(type);
			}
		}
	}

	/**
	 * Notifies all listeners that a change occurred on the given connection
	 * 
	 * @param connection
	 *            the connection that changed
	 * @param type
	 *            the type of change
	 */
	@SuppressWarnings("deprecation")
	public void notifyListeners(final IDockerConnection connection,
			final int type) {
		if (connectionManagerListeners != null) {
			for (IDockerConnectionManagerListener listener : connectionManagerListeners) {
				if (listener instanceof IDockerConnectionManagerListener2) {
					((IDockerConnectionManagerListener2) listener)
							.changeEvent(connection, type);
				} else {
					// keeping the call to the old method for the listeners that
					// are
					// interested
					listener.changeEvent(type);
				}
			}
		}
	}

	@Deprecated
	public List<IDockerConnectionSettings> findConnectionSettings() {
		// delegate the call to a utility class.
		return connectionSettingsFinder.findConnectionSettings();
	}

	/**
	 * Finds the default {@link IDockerConnectionSettings}
	 * 
	 * @return the default {@link IDockerConnectionSettings} or
	 *         <code>null</code> if nothing was found
	 */
	public IDockerConnectionSettings findDefaultConnectionSettings() {
		// delegate the call to a utility class.
		return connectionSettingsFinder.findDefaultConnectionSettings();
	}

	/**
	 * Resolves the name of the Docker instance, given the
	 * {@link IDockerConnectionSettings}
	 * 
	 * @param connectionSettings
	 *            the settings to use to connect
	 * @return the name retrieved from the Docker instance or <code>null</code>
	 *         if something went wrong.
	 */
	public String resolveConnectionName(
			IDockerConnectionSettings connectionSettings) {
		return connectionSettingsFinder
				.resolveConnectionName(connectionSettings);
	}

	/**
	 * Updates the given {@link IDockerConnection} with the given {@code name}
	 * and {@code connectionSettings}
	 * 
	 * @param connection
	 *            the {@link IDockerConnection} to update
	 * @param name
	 *            the (new) connection name
	 * @param connectionSettings
	 *            the (new) connection settings
	 * @return <code>true</code> if the connection name or settings changed,
	 *         <code>false</code> otherwise.
	 */
	public boolean updateConnection(final IDockerConnection connection,
			final String name,
			final IDockerConnectionSettings connectionSettings) {
		final boolean nameChanged = connection.setName(name);
		final boolean settingsChanged = connection
				.setSettings(connectionSettings);
		if (nameChanged) {
			notifyListeners(connection,
					IDockerConnectionManagerListener.RENAME_EVENT);
		}
		if (settingsChanged) {
			notifyListeners(connection,
					IDockerConnectionManagerListener.UPDATE_SETTINGS_EVENT);
		}
		if (nameChanged || settingsChanged) {
			saveConnections();
			return true;
		}
		return false;
	}


}
