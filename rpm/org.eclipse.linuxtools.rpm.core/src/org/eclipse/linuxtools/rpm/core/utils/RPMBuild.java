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
package org.eclipse.linuxtools.rpm.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.RPMConfiguration;
import org.eclipse.linuxtools.rpm.core.RPMCorePlugin;

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
	public RPMBuild(RPMConfiguration config) {
		IEclipsePreferences node = new DefaultScope().getNode(RPMCorePlugin.ID);
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
	 * @param specFile
	 *            the spec file
	 * @return The output of the `rpmbuild -bp` command.
	 * @throws CoreException
	 *             if the operation fails
	 */
	public InputStream buildPrep(IFile specFile) throws CoreException {
		List<String> command = new ArrayList<String>();
		command.addAll(Arrays.asList(macroDefines));
		command.add("-bp"); //$NON-NLS-1$
		command.add(specFile.getLocation().toString());
		try {
			return Utils.runCommandToInputStream(command
					.toArray(new String[command.size()]));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, RPMCorePlugin.ID,
					e.getMessage(), e));
		}
	}

	/**
	 * Builds a binary RPM for a given spec file.
	 * 
	 * @param specFile
	 *            the spec file
	 * @param outStream The stream to write the output to.
	 * @throws CoreException
	 *             if the operation fails
	 */
	public void buildBinary(IFile specFile, OutputStream outStream) throws CoreException {
		List<String> command = new ArrayList<String>();
		command.addAll(Arrays.asList(macroDefines));
		command.add("-bb"); //$NON-NLS-1$
		command.add(specFile.getLocation().toString());
		try {
			Utils.runCommand(outStream, command
					.toArray(new String[command.size()]));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, RPMCorePlugin.ID,
					e.getMessage(), e));
		}
	}

	/**
	 * Builds both a binary and source RPM for a given spec file.
	 * 
	 * @param specFile
	 *            the spec file
	 * @param outStream The stream to write the output to.
	 * @throws CoreException
	 *             if the operation fails
	 */
	public void buildAll(IFile specFile, OutputStream outStream) throws CoreException {
		List<String> command = new ArrayList<String>();
		command.addAll(Arrays.asList(macroDefines));
		command.add("-ba"); //$NON-NLS-1$
		command.add(specFile.getLocation().toString());
		try {
			Utils.runCommand(outStream, command
					.toArray(new String[command.size()]));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, RPMCorePlugin.ID,
					e.getMessage(), e));
		}
	}

	/**
	 * Builds a source RPM for a given spec file.
	 * 
	 * @param specFile
	 *            the spec file
	 * @param outStream The stream to write the output to.
	 * @throws CoreException
	 *             if the operation fails
	 */
	public void buildSource(IFile specFile, OutputStream outStream) throws CoreException {
		List<String> command = new ArrayList<String>();
		command.addAll(Arrays.asList(macroDefines));
		command.add("-bs"); //$NON-NLS-1$
		command.add(specFile.getLocation().toString());
		try {
			Utils.runCommand(outStream, command
					.toArray(new String[command.size()]));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, RPMCorePlugin.ID,
					e.getMessage(), e));
		}
	}
}
