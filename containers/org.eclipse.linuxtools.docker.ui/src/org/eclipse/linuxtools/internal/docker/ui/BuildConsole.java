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

package org.eclipse.linuxtools.internal.docker.ui;

import java.io.IOException;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;

/**
 * The {@link IOConsole} used to display logs during an image build. This
 * console is unique for all builds, whatever the Image and is cleared at the
 * beginning of each new build.
 * 
 */
public class BuildConsole extends IOConsole {

	/** Id of this console. */
	public static final String ID = "imageBuildLog"; //$NON-NLS-1$
	public static final String BUILD_CONSOLE_TITLE = "BuildConsole.title"; //$NON-NLS-1$

	private final IOConsoleOutputStream outputStream;

	/**
	 * Returns a reference to this {@link BuildConsole}. If the console does not
	 * yet exist, it is created.
	 *
	 * @return An existing or newly created instance of {@link BuildConsole}.
	 */
	public static BuildConsole findConsole() {
		for (IConsole console : ConsolePlugin.getDefault().getConsoleManager()
				.getConsoles()) {
			if (console instanceof BuildConsole) {
				return (BuildConsole) console;
			}
		}
		// no existing console, create new one
		final BuildConsole console = new BuildConsole();
		ConsolePlugin.getDefault().getConsoleManager()
				.addConsoles(new IConsole[] { console });
		return console;
	}

	/**
	 * Constructor
	 * 
	 * @see BuildConsole#findConsole()
	 */
	private BuildConsole() {
		super(ConsoleMessages.getString(BUILD_CONSOLE_TITLE), ID, null, true);
		this.outputStream = super.newOutputStream();
	}

	public void write(final byte[] bytes) throws IOException {
		this.outputStream.write(bytes);
	}

	public void close() throws IOException {
		this.outputStream.close();
	}

	/**
	 * Show this console in the Console View.
	 */
	public void showConsole() {
		// Show this console
		ConsolePlugin.getDefault().getConsoleManager().showConsoleView(this);
	}

}
