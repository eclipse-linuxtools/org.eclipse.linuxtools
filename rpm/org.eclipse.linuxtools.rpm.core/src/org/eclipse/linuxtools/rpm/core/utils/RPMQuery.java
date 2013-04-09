/*******************************************************************************
 * Copyright (c) 2005, 2009 Red Hat, Inc.
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

/**
 * Utility class for executing queries on existing binary and source RPMs
 * 
 */
public class RPMQuery {

	private static final String QF = "--qf"; //$NON-NLS-1$
	private static final String QP = "-qp"; //$NON-NLS-1$

	private RPMQuery() {
	}

	public static String getHeaderInfo(IFile rpmFile) throws CoreException {
		return query(rpmFile, "-qip"); //$NON-NLS-1$
	}

	public static String getChangelog(IFile rpmFile) throws CoreException {
		return query(rpmFile, "--changelog", QP); //$NON-NLS-1$
	}

	public static String getArch(IFile rpmFile) throws CoreException {
		return query(rpmFile, QF, "%{ARCH}", QP); //$NON-NLS-1$
	}

	public static String getPlatform(IFile rpmFile) throws CoreException {
		return query(rpmFile, QF, "%{PLATFORM}", QP); //$NON-NLS-1$
	}

	public static String getOS(IFile rpmFile) throws CoreException {
		return query(rpmFile, QF, "%{OS}", QP); //$NON-NLS-1$
	}

	public static String getBuildHost(IFile rpmFile) throws CoreException {
		return query(rpmFile, QF, "%{BUILDHOST}", QP); //$NON-NLS-1$
	}

	public static String getBuildTime(IFile rpmFile) throws CoreException {
		return query(rpmFile, QF, "%{BUILDTIME:date}", QP); //$NON-NLS-1$
	}

	public static String getPreInstallScript(IFile rpmFile)
			throws CoreException {
		return query(rpmFile, QF, "%{PREIN}", QP); //$NON-NLS-1$
	}

	public static String getPostInstallScript(IFile rpmFile)
			throws CoreException {
		return query(rpmFile, QF, "%{POSTIN}", QP); //$NON-NLS-1$
	}

	public static String getPreUninstallScript(IFile rpmFile)
			throws CoreException {
		return query(rpmFile, QF, "%{PREUN}", QP); //$NON-NLS-1$
	}

	public static String getPostUninstallScript(IFile rpmFile)
			throws CoreException {
		return query(rpmFile, QF, "%{POSTUN}", QP); //$NON-NLS-1$
	}

	public static String getProvides(IFile rpmFile) throws CoreException {
		return query(rpmFile, "-qlp"); //$NON-NLS-1$
	}

	private static String query(IFile rpmFile, String... args)
			throws CoreException {
		IEclipsePreferences node = DefaultScope.INSTANCE.getNode(IRPMConstants.RPM_CORE_ID);
		String rpmCmd = node.get(IRPMConstants.RPM_CMD, ""); //$NON-NLS-1$
		List<String> command = new ArrayList<String>();
		command.add(rpmCmd);
		command.addAll(Arrays.asList(args));
		command.add(rpmFile.getLocation().toOSString());
		try {
			return Utils.runCommandToString(command.toArray(new String[command
					.size()]));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, IRPMConstants.RPM_CORE_ID,
					e.getMessage(), e));
		}
	}
	
	/**
	 * Uses RPM to eval the given string.
	 * @param toEval The string to be evaled.
	 * @return The value of the evaluation.
	 * @throws CoreException If there is IOException when calling.
	 */
	public static String eval(String toEval) throws CoreException {
		IEclipsePreferences node = DefaultScope.INSTANCE
				.getNode(IRPMConstants.RPM_CORE_ID);
		String rpmCmd = node.get(IRPMConstants.RPM_CMD, ""); //$NON-NLS-1$
		List<String> command = new ArrayList<String>();
		command.add(rpmCmd);
		command.add("--eval"); //$NON-NLS-1$
		command.add(toEval);
		try {
			return Utils.runCommandToString(command.toArray(new String[command
					.size()]));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					IRPMConstants.RPM_CORE_ID, e.getMessage(), e));
		}
	}

}
