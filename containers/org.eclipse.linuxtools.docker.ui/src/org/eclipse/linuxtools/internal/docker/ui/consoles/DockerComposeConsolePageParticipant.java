/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc - modified to use in Docker UI
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.consoles;

import java.util.stream.Stream;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * an {@link IConsolePageParticipant} for the {@code docker-compose} console.
 */
public class DockerComposeConsolePageParticipant
		implements IConsolePageParticipant, IDebugEventSetListener {

	private DockerComposeConsole dockerComposeConsole;
	private IPageBookViewPage dockerComposeConsolePage;
	private DockerComposeStopAction stopAction;

	private boolean dockerComposeConsoleTerminated = false;

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public void init(final IPageBookViewPage dockerComposeConsolePage,
			final IConsole console) {
		this.dockerComposeConsolePage = dockerComposeConsolePage;
		this.dockerComposeConsole = (DockerComposeConsole) console;
		this.stopAction = new DockerComposeStopAction(
				dockerComposeConsole.getConnection(),
				dockerComposeConsole.getWorkingDir());
		// contribute to toolbar
		configureToolBar(dockerComposeConsolePage.getSite().getActionBars()
				.getToolBarManager());
		if (this.dockerComposeConsole.getDockerComposeProcess() == null
				|| this.dockerComposeConsoleTerminated) {
			this.stopAction.setEnabled(false);
		}
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	/**
	 * Contribute actions to the toolbar
	 */
	protected void configureToolBar(final IToolBarManager toolBarManager) {
		toolBarManager.appendToGroup(IConsoleConstants.LAUNCH_GROUP,
				stopAction);
	}

	@Override
	public void dispose() {
	}

	@Override
	public void activated() {
	}

	@Override
	public void deactivated() {
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		Stream.of(events)
				.filter(event -> event.getSource()
								.equals(this.dockerComposeConsole
										.getDockerComposeProcess()))
				.forEach(event -> {
					processEvent(event);
				});

	}

	private void processEvent(DebugEvent event) {
		switch (event.getKind()) {
		case DebugEvent.CREATE:
			this.dockerComposeConsoleTerminated = false;
			stopAction.setEnabled(true);
			break;
		case DebugEvent.TERMINATE:
			this.dockerComposeConsoleTerminated = true;
			stopAction.setEnabled(false);
			break;
		}
	}
}
