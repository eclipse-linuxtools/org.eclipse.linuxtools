/*******************************************************************************
 * Copyright (c) 2010, 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *     Red Hat Inc - modified to use in Docker UI
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.consoles;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerState;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.core.DockerConsoleOutputStream;
import org.eclipse.linuxtools.internal.docker.ui.views.DVMessages;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;

/**
 * RpmConsole is used to output rpm/rpmbuild output.
 *
 */
public class RunConsole extends IOConsole {

	/** Id of this console. */
	public static final String ID = "containerLog"; //$NON-NLS-1$
	public static final String CONTAINER_LOG_TITLE = "ContainerLog.title"; //$NON-NLS-1$
	public static final String DEFAULT_ID = "__DEFAULT_ID__"; //$NON-NLS-1$

	private String containerId;
	private String id;

	private OutputStream outputStream;
	private boolean attached = false;

	/**
	 * Returns a reference to the console that is for the given container id. If
	 * such a console does not yet exist, it will be created.
	 *
	 * @param containerId
	 *            The container id this console will be for. Must not be
	 *            <code>null</code>.
	 * @return A console instance or <code>null</code> if the given containerId
	 *         was <code>null</code>.
	 */
	public static RunConsole findConsole(String containerId) {
		if (containerId == null)
			return null;
		return findConsole(containerId, DEFAULT_ID);
	}

	/**
	 * Returns a reference to the console that is for the given container id. If
	 * such a console does not yet exist, it will be created.
	 *
	 * @param container
	 *            The container this console will be for. Must not be
	 *            <code>null</code>.
	 * @return A console instance or <code>null</code> if the given containerId
	 *         was <code>null</code>.
	 */
	public static RunConsole findConsole(IDockerContainer container) {
		if (container == null)
			return null;
		return findConsole(container.id(), DEFAULT_ID, container.name());
	}

	/**
	 * Returns a reference to the console that is for the given container id. If
	 * such a console does not yet exist, it will be created.
	 *
	 * @param containerId
	 *            The container this console will be for. Must not be
	 *            <code>null</code>.
	 * @param id
	 *            The secondary id used to identify consoles belonging to
	 *            various owners.
	 * @return A console instance.
	 */
	public static RunConsole findConsole(String containerId, String id) {
		return findConsole(containerId, id, containerId.substring(0, 8));
	}

	/**
	 * Returns a reference to the console that is for the given container id. If
	 * such a console does not yet exist, it will be created.
	 *
	 * @param containerId
	 *            The container this console will be for. Must not be
	 *            <code>null</code>.
	 * @param id
	 *            The secondary id used to identify consoles belonging to
	 *            various owners.
	 * @param name
	 *            the name of the console to create if it did not already exist
	 * @return A console instance.
	 */
	public static RunConsole findConsole(String containerId, String id,
			String name) {
		RunConsole ret = null;
		for (IConsole cons : ConsolePlugin.getDefault().getConsoleManager()
				.getConsoles()) {
			if (cons instanceof RunConsole
					&& ((RunConsole) cons).containerId.equals(containerId)
					&& ((RunConsole) cons).id.equals(id)) {
				ret = (RunConsole) cons;
			}
		}
		// no existing console, create new one
		if (ret == null) {
			ret = new RunConsole(containerId, id, name);
			ConsolePlugin.getDefault().getConsoleManager()
					.addConsoles(new IConsole[] { ret });
		}

		return ret;
	}

	/**
	 * Set the title of the RunConsole
	 * 
	 * @param name
	 *            - new title
	 */
	public void setTitle(String name) {
		setName(name);
	}

	/**
	 * The console will be attached to the underlying container.
	 *
	 * @param connection
	 *            The connection associated with this console.
	 */
	public void attachToConsole(final IDockerConnection connection) {
		final InputStream in = getInputStream();
		Thread t = new Thread(() -> {
			try {
				DockerConnection conn = (DockerConnection) connection;
				if (conn.getContainerInfo(containerId).config().openStdin()) {
					IDockerContainerState state = conn
							.getContainerInfo(containerId).state();
					do {
						if (!state.running() && (state.finishDate() == null
								|| state.finishDate()
										.before(state.startDate()))) {
							Thread.sleep(300);
						}
						state = conn.getContainerInfo(containerId).state();
					} while (!state.running() && (state.finishDate() == null
							|| state.finishDate().before(state.startDate())));
					Thread.sleep(300);
					state = conn.getContainerInfo(containerId).state();
					if (state.running()) {
						conn.attachCommand(containerId, in, null);
					}
				}
			} catch (Exception e) {
				Activator.log(e);
			}
		});
		t.start();
		attached = true;
	}

	public static void attachToTerminal(final IDockerConnection connection,
			final String containerId, final DockerConsoleOutputStream out) {
		Thread t = new Thread(() -> {
			try {
				DockerConnection conn = (DockerConnection) connection;
				IDockerContainerState state = conn.getContainerInfo(containerId)
						.state();
				do {
					if (!state.running() && (state.finishDate() == null
							|| state.finishDate().before(state.startDate()))) {
						Thread.sleep(300);
					}
					state = conn.getContainerInfo(containerId).state();
				} while (!state.running() && (state.finishDate() == null
						|| state.finishDate().before(state.startDate())));
				// Pause as there appears to be some timing issue with regards
				// to the Container saying it is running, but an exception
				// thrown when we try and attach.
				Thread.sleep(300);
				state = conn.getContainerInfo(containerId).state();
				if (state.running()) {
					conn.attachCommand(containerId, null, out);
				} else {
					// notify any console listener that there is no more output
					// going to follow
					out.notifyConsoleListeners(new byte[] { 0 }, 0, 0);
				}
			} catch (Exception e) {
				Activator.log(e);
				// notify any console listener that there is no more output
				// going to follow
				out.notifyConsoleListeners(new byte[] { 0 }, 0, 0);
			}
		});
		t.start();
	}

	public void attachToConsole(final IDockerConnection connection,
			String containerId) {
		this.containerId = containerId;
		attachToConsole(connection);
	}

	/**
	 * Indicates whether the console is attached to the container's terminal.
	 * 
	 * @return true if the console is attached and false otherwise.
	 */
	public boolean isAttached() {
		/*
		 * TODO: This will still return true when the corresponding container is
		 * stopped but not currently a problem as we always attach to a
		 * container about to be started.
		 */
		return attached;
	}

	/**
	 * Remove a given console from the Console view
	 * 
	 * @param console
	 *            the console to remove
	 */
	public static void removeConsole(RunConsole console) {
		console.closeOutputStream();
		ConsolePlugin.getDefault().getConsoleManager()
				.removeConsoles(new IConsole[] { console });
	}

	/**
	 * Show this console in the Console View.
	 */
	public void showConsole() {
		// Show this console
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(this);
	}

	/**
	 * Close the last output stream opened for this console.
	 */
	public void closeOutputStream() {
		try {
			if (outputStream != null)
				outputStream.close();
		} catch (IOException e) {
			// do nothing...we tried
		}
	}

	/**
	 * Get a new output stream for this console
	 * 
	 * @return An output stream for writing to the console
	 */
	public OutputStream getOutputStream() {
		outputStream = new ConsoleOutputStream(newOutputStream());
		// outputStream = newOutputStream();
		return outputStream;
	}

	/**
	 * Creates the console.
	 *
	 * @param containerId
	 *            The container id this console is for.
	 * @param id
	 *            The id to associate with this console.
	 * @param name
	 *            The name to use for the console.
	 */
	private RunConsole(String containerId, String id, String name) {
		super(DVMessages.getFormattedString(CONTAINER_LOG_TITLE, name), ID,
				null, true);
		this.containerId = containerId;
		this.id = id;
	}
}
