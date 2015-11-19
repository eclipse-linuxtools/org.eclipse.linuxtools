/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.commands;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.Util;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerVolume;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.ide.handlers.ShowInSystemExplorerHandler;

/**
 * Command handler to open the selection in the Web Browser.
 * 
 * @see ShowInSystemExplorerHandler
 */
@SuppressWarnings("restriction")
public class ShowInSystemExplorerCommandHandler extends AbstractHandler {

	private static final String VARIABLE_RESOURCE = "${selected_resource_loc}"; //$NON-NLS-1$
	private static final String VARIABLE_RESOURCE_URI = "${selected_resource_uri}"; //$NON-NLS-1$
	private static final String VARIABLE_FOLDER = "${selected_resource_parent_loc}"; //$NON-NLS-1$

	@Override
	public Object execute(final ExecutionEvent event) {
		final IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		final List<DockerContainerVolume> volumes = CommandUtils
				.getSelectedVolumes(activePart);
		if (volumes == null || volumes.isEmpty()) {
			return null;
		}
		final DockerContainerVolume selectedVolume = volumes.get(0);
		final File hostFile = new File(selectedVolume.getHostPath());
		final String launchCmd = getShowInSystemExplorerCommand(hostFile);

		if (launchCmd == null) {
			return null;
		}
		final Job job = new Job(
				CommandMessages.getString("command.showIn.systemExplorer")) {
			// $NON-NLS-1$
			@Override
			protected IStatus run(final IProgressMonitor monitor) {

				try {
					final Process p = getLaunchProcess(launchCmd, hostFile);
					final int retCode = p.waitFor();
					if (retCode != 0 && !Util.isWindows()) {
						Activator.logErrorMessage(
								CommandMessages.getFormattedString(
										"command.showIn.systemExplorer.failure.command.execute", //$NON-NLS-1$
										launchCmd, Integer.toString(retCode)));
					}
				} catch (IOException | InterruptedException e) {
					Activator
							.logErrorMessage(CommandMessages.getFormattedString(
									"command.showIn.systemExplorer.failure", //$NON-NLS-1$
									launchCmd), e);
				} finally {
					monitor.done();
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
		return null;
	}

	/**
	 * @param launchCmd
	 *            the launch command
	 * @return the platform-dependent {@link Process} to run to open the file in
	 *         the System Explorer
	 * @throws IOException
	 */
	private Process getLaunchProcess(final String launchCmd, final File dir)
			throws IOException {
		if (Util.isLinux() || Util.isMac()) {
			return Runtime.getRuntime().exec(
					new String[] { "/bin/sh", "-c", launchCmd }, null, dir); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return Runtime.getRuntime().exec(launchCmd, null, dir);
		}
	}

	/**
	 * Prepare command for launching system explorer to show a path
	 *
	 * @param path
	 *            the path to show
	 * @return the command that shows the path
	 * @see ShowInSystemExplorerHandler#getDefaultCommand()
	 */
	private String getShowInSystemExplorerCommand(final File path) {
		String command = ShowInSystemExplorerHandler.getDefaultCommand();
		if ("".equals(command)) {
			Activator.logErrorMessage(CommandMessages.getString(
					"command.showIn.systemExplorer.failure.command_unavailable"));
			return null;
		}
		try {
			command = Util.replaceAll(command, VARIABLE_RESOURCE,
					quotePath(path.getCanonicalPath()));
			command = Util.replaceAll(command, VARIABLE_RESOURCE_URI,
					path.getCanonicalFile().toURI().toString());
			File parent = path.getParentFile();
			if (parent != null) {
				command = Util.replaceAll(command, VARIABLE_FOLDER,
						quotePath(parent.getCanonicalPath()));
			}
			return command;
		} catch (IOException e) {
			Activator.logErrorMessage(CommandMessages
					.getString("command.showIn.systemExplorer.failure"), e);
			return null;
		}
	}

	private String quotePath(String path) {
		if (Util.isLinux() || Util.isMac()) {
			// Quote for usage inside "", man sh, topic QUOTING:
			path = path.replaceAll("[\"$`]", "\\\\$0"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		// Windows: Can't quote, since explorer.exe has a very special command
		// line parsing strategy.
		return path;
	}

}
