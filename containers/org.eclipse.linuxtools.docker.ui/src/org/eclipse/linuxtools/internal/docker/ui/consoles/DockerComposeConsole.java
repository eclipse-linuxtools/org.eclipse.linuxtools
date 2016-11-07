/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.consoles;

import java.io.IOException;
import java.util.stream.Stream;

import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * An {@link IOConsole} for the {@code docker-compose} dockerComposeProcess.
 */
public class DockerComposeConsole extends IOConsole {

	public static final String CONSOLE_TYPE = "org.eclipse.linuxtools.internal.docker.ui.consoles.DockerComposeConsole";

	private final IDockerConnection connection;

	private final String workingDir;

	private IProcess dockerComposeProcess;

	public DockerComposeConsole(
			final IDockerConnection connection, final String workingDir) {
		super(ConsoleMessages.getFormattedString("DockerComposeConsole.title", //$NON-NLS-1$
				workingDir), null);
		this.workingDir = workingDir;
		this.connection = connection;
		setType(CONSOLE_TYPE);
	}

	public IDockerConnection getConnection() {
		return this.connection;
	}

	public String getWorkingDir() {
		return this.workingDir;
	}

	public IProcess getDockerComposeProcess() {
		return this.dockerComposeProcess;
	}

	public void setDockerComposeProcess(final IProcess dockerComposeProcess) {
		this.dockerComposeProcess = dockerComposeProcess;
		// activate and clear the console if it has previous content
		activate();
		clearConsole();
		// catch up with the content that was already output by the Java Process
		writeContentInConsole(dockerComposeProcess.getStreamsProxy()
				.getOutputStreamMonitor().getContents());
		// then follow the streams
		dockerComposeProcess.getStreamsProxy().getOutputStreamMonitor()
				.addListener(new IStreamListener() {
					@Override
					public void streamAppended(final String text,
							final IStreamMonitor monitor) {
						writeContentInConsole(text);

					}
				});
		dockerComposeProcess.getStreamsProxy().getErrorStreamMonitor()
				.addListener(new IStreamListener() {
					@Override
					public void streamAppended(final String text,
							final IStreamMonitor monitor) {
						writeContentInConsole(text);

					}
				});
	}

	private void writeContentInConsole(final String text) {
		Display.getDefault().asyncExec(() -> {
			final StyledString styledString = StyledTextBuilder.parse(text);
			final StyleRange[] styleRanges = styledString.getStyleRanges();
			Stream.of(styleRanges).forEach(range -> {
				try (final IOConsoleOutputStream consoleStream = newOutputStream()) {
					consoleStream.setColor(range.foreground);
					consoleStream.write(styledString.getString()
							.substring(range.start, range.start + range.length)
							.getBytes());
				} catch (IOException e) {
					Activator.log(e);
				}
			});
		});
	}

}
