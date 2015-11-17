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
package org.eclipse.linuxtools.internal.docker.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.docker.core.Activator;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerListener;

public class DockerContainerRefreshManager implements IDockerContainerListener {

	private static DockerContainerRefreshManager instance;
	private final long DEFAULT_REFRESH_TIME = 15;

	private Map<IDockerConnection, ContainerRefreshThread> refreshThreadMap;

	private DockerContainerRefreshManager() {
		refreshThreadMap = new HashMap<>();
	}

	public static DockerContainerRefreshManager getInstance() {
		if (instance == null)
			instance = new DockerContainerRefreshManager();
		return instance;
	}

	/**
	 * @return an immutable {@link Set} of all the {@link IDockerConnection}
	 *         that are monitored
	 */
	public Set<IDockerConnection> getConnections() {
		return Collections.unmodifiableSet(refreshThreadMap.keySet());
	}

	@Override
	public synchronized void listChanged(
			final IDockerConnection connection,
			final List<IDockerContainer> dclist) {

		if (!refreshThreadMap.containsKey(connection)) {
			long refreshRateInSeconds = Platform.getPreferencesService()
					.getLong("org.eclipse.linuxtools.docker.ui", //$NON-NLS-1$ 
							"containerRefreshTime", DEFAULT_REFRESH_TIME, null); //$NON-NLS-1$
			final ContainerRefreshThread rt = new ContainerRefreshThread(
					connection,
					TimeUnit.SECONDS.toMillis(refreshRateInSeconds));
			rt.start();
			refreshThreadMap.put(connection, rt);
		}
	}

	/**
	 * Stops and remove the {@link ContainerRefreshThread} associated with the
	 * given {@link IDockerConnection}.
	 * 
	 * @param connection
	 *            the connection that was monitored
	 */
	public synchronized void removeContainerRefreshThread(
			final IDockerConnection connection) {

		if (refreshThreadMap.containsKey(connection)) {
			final ContainerRefreshThread containerRefreshThread = refreshThreadMap.get(connection);
			containerRefreshThread.stopMonitoring();
			refreshThreadMap.remove(connection);
		}
	}

	/**
	 * Method to reset the refresh rate for updating container lists
	 * 
	 * @param seconds
	 *            - time to wait between refreshes
	 */
	public void setRefreshTime(long seconds) {
		if (seconds >= 5) {
			long refreshRate = TimeUnit.SECONDS.toMillis(seconds);
			for (ContainerRefreshThread t : refreshThreadMap.values()) {
				t.setSleepTime(refreshRate);
			}
		}
	}

	/**
	 * ContainerRefreshThread class is used to update the container lists for a
	 * particular connection as short as needed to keep the UI updated on a
	 * regular basis.
	 *
	 */
	private class ContainerRefreshThread extends Thread {

		private IDockerConnection connection;
		private long sleepTime;
		private boolean kill;
		private boolean monitor;

		public ContainerRefreshThread(IDockerConnection connection,
				long sleepTime) {
			this.connection = connection;
			this.sleepTime = sleepTime;
			this.monitor = true;
		}

		/**
		 * Stop this {@link ContainerRefreshThread} to monitor changes for the
		 * associated {@link IDockerConnection} (after it was removed, for
		 * example)
		 */
		public void stopMonitoring() {
			monitor = false;
		}

		public IDockerConnection getConnection() {
			return connection;
		}

		public synchronized long getSleepTime() {
			return sleepTime;
		}

		public synchronized void setSleepTime(long newTime) {
			sleepTime = newTime;
			this.interrupt();
		}

		public void kill() {
			kill = true;
			this.interrupt();
		}

		@Override
		public void run() {
			while (monitor) {
				try {
					Thread.sleep(getSleepTime());
					// After sleep period, update the containers list, but make
					// sure the refreshManager isn't notified since that
					// is what triggered this to begin with.
					synchronized (instance) {
						// monitoring may have been switched off while the
						// thread was sleeping
						if (monitor) {
							// connection.removeContainerListener(instance);
							((DockerConnection) connection).getContainers(true);
							// connection.addContainerListener(instance);
						}
					}
				} catch (InterruptedException e) {
					if (kill)
						break;
					// otherwise..continue
				}
			}
			Activator.log(new Status(IStatus.INFO, Activator.PLUGIN_ID,
					"Stopped monitor container changes for connection '"
							+ connection.getName() + "'"));
		}
	}

	/***
	 * Method to kill all threads at shutdown.
	 */
	public void killAllThreads() {
		for (ContainerRefreshThread rt : refreshThreadMap.values()) {
			rt.kill();
			refreshThreadMap.remove(rt.getConnection());
		}
	}
}
