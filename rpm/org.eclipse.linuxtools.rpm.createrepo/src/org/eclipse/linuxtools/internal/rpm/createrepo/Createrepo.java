/*******************************************************************************
 * Copyright (c) 2013 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Neil Guzman - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.createrepo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.rpm.core.utils.Utils;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoPreferenceConstants;
import org.eclipse.linuxtools.rpm.createrepo.CreaterepoProject;
import org.eclipse.linuxtools.rpm.createrepo.ICreaterepoConstants;
import org.osgi.framework.FrameworkUtil;

/**
 * This class will execute the actual createrepo command.
 */
public class Createrepo {

	/*
	 * Default commands that every execution will have.
	 */
	private static final String[] DEFAULT_ARGUMENTS = {
		CreaterepoPreferenceConstants.PREF_VERBOSE, CreaterepoPreferenceConstants.PREF_PROFILE};

	/**
	 * Holds the command line switches.
	 */
	private List<String> commandSwitches;

	/**
	 * Initialize the command switches to be "createrepo" and
	 * default command arguments.
	 */
	public Createrepo() {
		commandSwitches = new ArrayList<String>();
		commandSwitches.add(ICreaterepoConstants.CREATEREPO_COMMAND);
		for (String arg : DEFAULT_ARGUMENTS) {
			commandSwitches.add(ICreaterepoConstants.DASH.concat(arg));
		}
	}

	/**
	 * Execute a createrepo command with custom arguments. The target directory
	 * will always be the current project's content folder and will automatically be
	 * added before execution. A blank list will result in the default createrepo execution.
	 *
	 * @param os Direct execution stream to this.
	 * @param project The project.
	 * @param commands A list of command switches to execute with the createrepo command.
	 * @return The status of the execution.
	 * @throws CoreException Occurs when error trying to execute the command.
	 */
	public IStatus execute(final OutputStream os, CreaterepoProject project, List<String> commands) throws CoreException {
		commandSwitches.addAll(commands);
		commandSwitches.add(project.getContentFolder().getLocation().toOSString());
		/* Display what the execution looks like */
		String commandString = ICreaterepoConstants.EMPTY_STRING;
		for (String arg : commandSwitches) {
			commandString = commandString.concat(arg + " "); //$NON-NLS-1$
		}
		commandString = commandString.concat("\n"); //$NON-NLS-1$
		try {
			os.write(commandString.getBytes());
			return Utils.runCommand(os, project.getProject(), commandSwitches.toArray(new String[commandSwitches.size()]));
		} catch (IOException e) {
			IStatus status = new Status(
					IStatus.ERROR,
					FrameworkUtil.getBundle(CreaterepoProject.class).getSymbolicName(),
					Messages.Createrepo_errorExecuting, null);
			throw new CoreException(status);
		}
	}

}
