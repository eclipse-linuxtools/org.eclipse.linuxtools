/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat Inc. and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.commands;

import static org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils.getCurrentConnection;
import static org.eclipse.linuxtools.internal.docker.ui.commands.CommandUtils.getSelectedPortMappings;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.docker.core.Activator;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerPortMapping;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler to open the selection in the Web Browser.
 */
public class ShowInWebBrowserCommandHandler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final List<IDockerPortMapping> portMappings = getSelectedPortMappings(
				activePart);
		if (portMappings == null || portMappings.isEmpty()) {
			return null;
		}
		final Job job = new Job(
				CommandMessages.getString("command.showIn.webBrowser")) { //$NON-NLS-1$
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				try {
					final IDockerConnection currentConnection = getCurrentConnection(
							activePart);
					final IDockerPortMapping selectedPort = portMappings
							.get(0);
					final URI connectionURI = new URI(
							currentConnection.getUri());
					if ("tcp".equalsIgnoreCase(connectionURI.getScheme()) //$NON-NLS-1$
							|| "unix" //$NON-NLS-1$
									.equalsIgnoreCase(connectionURI.getScheme())
							|| "http" //$NON-NLS-1$
									.equalsIgnoreCase(connectionURI.getScheme())
							|| "https".equalsIgnoreCase( //$NON-NLS-1$
									connectionURI.getScheme())) {
						final String host = "unix" //$NON-NLS-1$
								.equalsIgnoreCase(connectionURI.getScheme())
										? "127.0.0.1" : connectionURI.getHost(); //$NON-NLS-1$
						final URL location = new URL("http", host, //$NON-NLS-1$
								selectedPort.getPublicPort(), "/"); //$NON-NLS-1$
						openLocationInWebBrowser(location);
					}
				} catch (URISyntaxException | MalformedURLException e) {
					Activator.logErrorMessage(
							CommandMessages.getString(
									"command.showIn.webBrowser.failure"), //$NON-NLS-1$
							e);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

	private void openLocationInWebBrowser(final URL location) {
		Display.getDefault().asyncExec(() -> {
			try {
				final IWebBrowser browser = PlatformUI.getWorkbench()
						.getBrowserSupport()
						.createBrowser(IWorkbenchBrowserSupport.AS_EDITOR
								| IWorkbenchBrowserSupport.LOCATION_BAR
								| IWorkbenchBrowserSupport.NAVIGATION_BAR,
								Activator.PLUGIN_ID,
								CommandMessages.getString("ShowInWebBrowserCommandHandler.internal.browser.label"), //$NON-NLS-1$
								CommandMessages.getString("ShowInWebBrowserCommandHandler.internal.browser.tooltip")); //$NON-NLS-1$
				browser.openURL(location);
			} catch (Exception e) {
				Activator.log(Status.error(CommandMessages.getString("command.showIn.webBrowser.failure"), //$NON-NLS-1$
						e));
			}
		});
	}

}
