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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.Platform;
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

	@Override
	public synchronized void listChanged(IDockerConnection connection,
			List<IDockerContainer> dclist) {

		ContainerRefreshThread rt = refreshThreadMap.get(connection);
		if (rt == null) {
			long refreshRateInSeconds = Platform.getPreferencesService()
					.getLong("org.eclipse.linuxtools.docker.ui", //$NON-NLS-1$ 
							"containerRefreshTime", DEFAULT_REFRESH_TIME, null); //$NON-NLS-1$
			rt = new ContainerRefreshThread(connection,
					TimeUnit.SECONDS.toMillis(refreshRateInSeconds));
			rt.start();
			refreshThreadMap.put(connection, rt);
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

		public ContainerRefreshThread(IDockerConnection connection,
				long sleepTime) {
			this.connection = connection;
			this.sleepTime = sleepTime;
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
			for (;;) {
				try {
					Thread.sleep(getSleepTime());
					// After sleep period, update the containers list, but make
					// sure the refreshManager isn't notified since that
					// is what triggered this to begin with.
					synchronized (instance) {
						connection.removeContainerListener(instance);
						((DockerConnection) connection).getContainers(true);
						connection.addContainerListener(instance);
					}
				} catch (InterruptedException e) {
					if (kill)
						break;
					// otherwise..continue
				}
			}
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
