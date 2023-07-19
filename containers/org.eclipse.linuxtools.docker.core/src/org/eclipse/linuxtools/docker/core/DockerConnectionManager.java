/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0l
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.docker.core;

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

	private static DockerConnectionManager instance;

	private final static int MAX_TIME = 40000;

	private List<IDockerConnection> connections;
	private ListenerList<IDockerConnectionManagerListener> connectionManagerListeners;

	Thread reloadThread = null;

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
		reloadThread = new Thread(() -> {
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
					if (setting.getType()
							.equals(BindingType.UNIX_SOCKET_CONNECTION)) {
						UnixSocketConnectionSettings usetting = (UnixSocketConnectionSettings) setting;
						conn = new DockerConnection.Builder()
								.name(usetting.getName())
								.unixSocketConnection(usetting);
					} else {
						TCPConnectionSettings tsetting = (TCPConnectionSettings) setting;
						conn = new DockerConnection.Builder()
								.name(tsetting.getName())
								.tcpConnection(tsetting);
					}
					// add the connection but do not notify the listeners to
					// avoid
					// flickering on the Docker Explorer view for each entry
					addConnectionUnchecked(conn, false);
				}
			}
		});
		reloadThread.start();
	}

	public void setConnectionSettingsFinder(
			final IDockerConnectionSettingsFinder connectionSettingsFinder) {
		if (reloadThread != null) {
			try {
				reloadThread.join(MAX_TIME);
			} catch (InterruptedException e) {
				// do nothing
			}
			reloadThread = null;
		}
		this.connectionSettingsFinder = connectionSettingsFinder;
	}

	public void setConnectionStorageManager(
			final IDockerConnectionStorageManager connectionStorageManager) {
		if (reloadThread != null) {
			try {
				reloadThread.join(MAX_TIME);
			} catch (InterruptedException e) {
				// do nothing
			}
			reloadThread = null;
		}
		this.connectionStorageManager = connectionStorageManager;
	}

	public void saveConnections() {
		if (reloadThread != null) {
			try {
				reloadThread.join(MAX_TIME);
			} catch (InterruptedException e) {
				// do nothing
			}
			reloadThread = null;
		}
		saveConnectionsUnchecked();
	}

	private void saveConnectionsUnchecked() {
		this.connectionStorageManager.saveConnections(this.connections);
	}

	/**
	 * @return an unmodifiable and non-null array of {@link IDockerConnection}
	 */
	public IDockerConnection[] getConnections() {
		if (reloadThread != null) {
			try {
				reloadThread.join(MAX_TIME);
			} catch (InterruptedException e) {
				// do nothing
			}
			reloadThread = null;
		}
		if (this.connections == null) {
			return new IDockerConnection[0];
		}
		return connections.toArray(new IDockerConnection[connections.size()]);
	}

	/**
	 * @return an unmodifiable and non-null list of {@link IDockerConnection}
	 */
	public List<IDockerConnection> getAllConnections() {
		if (reloadThread != null) {
			try {
				reloadThread.join(MAX_TIME);
			} catch (InterruptedException e) {
				// do nothing
			}
			reloadThread = null;
		}
		if (this.connections == null) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(this.connections);
	}

	/**
	 * @return the first {@link IDockerConnection} or <code>null</code> if none
	 *         exists yet.
	 */
	public IDockerConnection getFirstConnection() {
		if (reloadThread != null) {
			try {
				reloadThread.join(MAX_TIME);
			} catch (InterruptedException e) {
				// do nothing
			}
			reloadThread = null;
		}
		if (!hasConnections()) {
			return null;
		}
		return this.connections.get(0);
	}

	/**
	 * @return <code>true</code> if there is at least one
	 *         {@link IDockerConnection} in this
	 *         {@link DockerConnectionManager}, <code>false</code> otherwise.
	 */
	public boolean hasConnections() {
		if (reloadThread != null) {
			try {
				reloadThread.join(MAX_TIME);
			} catch (InterruptedException e) {
				// do nothing
			}
			reloadThread = null;
		}
		return connections != null && !connections.isEmpty();
	}

	/**
	 * Finds the {@link IDockerConnection} from the given {@code connectionName}
	 *
	 * @param connectionName
	 *            the name of the connection to find
	 * @return the {@link IDockerConnection} or <code>null</code> if none
	 *         matched.
	 */
	public IDockerConnection getConnectionByName(
			final String connectionName) {
		if (reloadThread != null) {
			try {
				reloadThread.join(MAX_TIME);
			} catch (InterruptedException e) {
				// do nothing
			}
			reloadThread = null;
		}
		return this.connections.stream().filter(
				connection -> connection.getName().equals(connectionName))
				.findFirst().orElse(null);
	}

	/**
	 * Finds the {@link IDockerConnection} from the given {@code connectionUri}
	 *
	 * @param connectionUri
	 *            the URI of the connection to find
	 * @return the {@link IDockerConnection} or <code>null</code> if none
	 *         matched.
	 */
	public IDockerConnection getConnectionByUri(
			String connectionUri) {
		if (reloadThread != null) {
			try {
				reloadThread.join(MAX_TIME);
			} catch (InterruptedException e) {
				// do nothing
			}
			reloadThread = null;
		}
		return DockerConnectionManager.getInstance().getAllConnections()
				.stream()
				.filter(c -> c.getUri().equals(connectionUri)).findFirst()
				.orElse(null);
	}

	/**
	 * @return an immutable {@link List} of the {@link IDockerConnection} names
	 */
	public List<String> getConnectionNames() {
		if (reloadThread != null) {
			try {
				reloadThread.join();
			} catch (InterruptedException e) {
				// do nothing
			}
			reloadThread = null;
		}
		return Collections.unmodifiableList(getAllConnections().stream()
				.map(IDockerConnection::getName)
				// making sure that no 'null' name is returned in the list of
				// connection names.
				.filter(n -> n != null)
				.toList());
	}

	public IDockerConnection findConnection(final String name) {
		if (reloadThread != null) {
			try {
				reloadThread.join();
			} catch (InterruptedException e) {
				// do nothing
			}
			reloadThread = null;
		}
		if (name != null) {
			for (IDockerConnection connection : connections) {
				if (connection.getName() != null
						&& connection.getName().equals(name))
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
	public void addConnection(
			final IDockerConnection dockerConnection) {
		if (reloadThread != null) {
			try {
				reloadThread.join();
			} catch (InterruptedException e) {
				// do nothing
			}
			reloadThread = null;
		}
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
	public void addConnection(
			final IDockerConnection dockerConnection,
			final boolean notifyListeners) {
		if (reloadThread != null) {
			try {
				reloadThread.join();
			} catch (InterruptedException e) {
				// do nothing
			}
			reloadThread = null;
		}
		addConnectionUnchecked(dockerConnection, notifyListeners);
	}

	private void addConnectionUnchecked(
			final IDockerConnection dockerConnection,
			final boolean notifyListeners) {
		if (!connections.contains(dockerConnection)) {
			connections.add(dockerConnection);
			saveConnectionsUnchecked();
			if (notifyListeners) {
				notifyListeners(dockerConnection,
						IDockerConnectionManagerListener.ADD_EVENT);
			}
		}
	}

	public void removeConnection(
			final IDockerConnection connection) {
		if (reloadThread != null) {
			try {
				reloadThread.join();
			} catch (InterruptedException e) {
				// do nothing
			}
			reloadThread = null;
		}
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
	 * Notifies all listeners that a change occurred on the given connection
	 *
	 * @param connection
	 *            the connection that changed
	 * @param type
	 *            the type of change
	 */
	public void notifyListeners(final IDockerConnection connection,
			final int type) {
		if (connectionManagerListeners != null) {
			for (IDockerConnectionManagerListener listener : connectionManagerListeners) {
				listener.changeEvent(connection, type);
			}
		}
	}

	/**
	 * Notifies all listeners that a change occurred on the given connection but
	 * does nothing if DockerConnectionManager isn't instantiated
	 *
	 * @param connection
	 *                       the connection that changed
	 * @param type
	 *                       the type of change
	 *
	 * @since 4.0
	 */
	public static void instanceNotifyListeners(
			final IDockerConnection connection,
			final int type) {
		if (instance != null) {
			instance.notifyListeners(connection, type);
		}
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
