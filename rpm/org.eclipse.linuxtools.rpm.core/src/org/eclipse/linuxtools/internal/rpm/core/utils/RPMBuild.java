/*******************************************************************************
 * Copyright (c) 2005-2010 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.core.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.linuxtools.rpm.core.IProjectConfiguration;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.utils.Utils;

/**
 * A utility class for executing rpmbuild commands.
 * 
 */
public class RPMBuild {

	private static final String DEFINE = "--define"; //$NON-NLS-1$

	private String[] macroDefines;

	private String rpmBuildCmd;

	/**
	 * Constructs a new object.
	 * 
	 * @param config
	 *            the RPM configuration to use
	 */
	public RPMBuild(IProjectConfiguration config) {
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(IRPMConstants.RPM_CORE_ID);
		rpmBuildCmd = node.get(IRPMConstants.RPMBUILD_CMD, ""); //$NON-NLS-1$
		String[] tmpMacroDefines = {
				rpmBuildCmd,
				"-v", //$NON-NLS-1$
				DEFINE, "_sourcedir " //$NON-NLS-1$
						+ config.getSourcesFolder().getLocation().toOSString(),
				DEFINE, "_srcrpmdir " + //$NON-NLS-1$
						config.getSrpmsFolder().getLocation().toOSString(),
				DEFINE, "_builddir " + //$NON-NLS-1$
						config.getBuildFolder().getLocation().toOSString(),
				DEFINE, "_rpmdir " + //$NON-NLS-1$
						config.getRpmsFolder().getLocation().toOSString(),
				DEFINE, "_specdir " + //$NON-NLS-1$
						config.getSpecsFolder().getLocation().toOSString() };
		this.macroDefines = tmpMacroDefines;
	}

	/**
	 * Prepares the sources for a given spec file.
	 * 
	 * @param specFile the spec file
	 * @param outStream The stream to write the output to.
	 * @throws CoreException If the operation fails.
	 */
	public void buildPrep(IResource specFile, OutputStream outStream)
			throws CoreException {
		List<String> command = new ArrayList<String>();
		command.addAll(Arrays.asList(macroDefines));
		command.add("-bp"); //$NON-NLS-1$
		command.add(specFile.getLocation().toString());
		try {
			Utils.runCommand(outStream,
					command.toArray(new String[command.size()]));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, IRPMConstants.RPM_CORE_ID,
					e.getMessage(), e));
		}
	}

	/**
	 * Builds a binary RPM for a given spec file.
	 * 
	 * @param specFile
	 *            the spec file
	 * @param outStream
	 *            The stream to write the output to.
	 * @return The return code of the build job.
	 * @throws CoreException
	 *             if the operation fails
	 */
	public int buildBinary(IResource specFile, OutputStream outStream) throws CoreException {
		List<String> command = new ArrayList<String>();
		command.addAll(Arrays.asList(macroDefines));
		command.add("-bb"); //$NON-NLS-1$
		command.add(specFile.getLocation().toString());
		try {
			return Utils.runCommand(outStream,
					command.toArray(new String[command.size()]));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, IRPMConstants.RPM_CORE_ID,
					e.getMessage(), e));
		}
	}

	/**
	 * Builds both a binary and source RPM for a given spec file.
	 * 
	 * @param specFile the spec file
	 * @param outStream The stream to write the output to.
	 * @return The return code of the build job.
	 * @throws CoreException
	 *             if the operation fails
	 */
	public int buildAll(IResource specFile, OutputStream outStream) throws CoreException {
		List<String> command = new ArrayList<String>();
		command.addAll(Arrays.asList(macroDefines));
		command.add("-ba"); //$NON-NLS-1$
		command.add(specFile.getLocation().toString());
		try {
			return Utils.runCommand(outStream, command
									.toArray(new String[command.size()]));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, IRPMConstants.RPM_CORE_ID,
					e.getMessage(), e));
		}
	}

	/**
	 * Builds a source RPM for a given spec file.
	 * 
	 * @param specFile the spec file
	 * @param outStream The stream to write the output to.
	 * @return The return code of the build job.
	 * @throws CoreException
	 *             if the operation fails
	 */
	public int buildSource(IResource specFile, OutputStream outStream) throws CoreException {
		List<String> command = new ArrayList<String>();
		command.addAll(Arrays.asList(macroDefines));
		command.add("-bs"); //$NON-NLS-1$
		command.add(specFile.getLocation().toString());
		try {
			return Utils.runCommand(outStream, command
									.toArray(new String[command.size()]));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, IRPMConstants.RPM_CORE_ID,
					e.getMessage(), e));
		}
	}
}
