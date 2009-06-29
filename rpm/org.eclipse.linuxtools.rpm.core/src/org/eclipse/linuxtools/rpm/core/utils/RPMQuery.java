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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.linuxtools.rpm.core.IRPMConstants;
import org.eclipse.linuxtools.rpm.core.RPMCorePlugin;
import org.eclipse.linuxtools.rpm.core.internal.utils.ShellScript;

/**
 * Utility class for executing queries on existing binary and 
 * source RPMs
 *
 */
public class RPMQuery {
	
	private RPMQuery() {
	}
	
	public static String getHeaderInfo(IFile rpmFile) throws CoreException {
		return query("-qi -qp", rpmFile); //$NON-NLS-1$
	}
	
	public static String getChangelog(IFile rpmFile) throws CoreException {
		return query("--changelog -qp", rpmFile); //$NON-NLS-1$
	}
	
	public static String getArch(IFile rpmFile) throws CoreException {
		return query("--qf %{ARCH} -qp", rpmFile); //$NON-NLS-1$
	}

	public static String getPlatform(IFile rpmFile) throws CoreException {
		return query("--qf %{PLATFORM} -qp", rpmFile); //$NON-NLS-1$
	}
	
	public static String getOS(IFile rpmFile) throws CoreException {
		return query("--qf %{OS} -qp", rpmFile); //$NON-NLS-1$
	}
	
	public static String getBuildHost(IFile rpmFile) throws CoreException {
		return query("--qf %{BUILDHOST} -qp", rpmFile); //$NON-NLS-1$
	}
	
	public static String getBuildTime(IFile rpmFile) throws CoreException {
		return query("--qf %{BUILDTIME:date} -qp", rpmFile); //$NON-NLS-1$
	}
	
	public static String getPreInstallScript(IFile rpmFile) throws CoreException {
		return query("--qf %{PREIN} -qp", rpmFile); //$NON-NLS-1$
	}
	
	public static String getPostInstallScript(IFile rpmFile) throws CoreException {
		return query("--qf %{POSTIN} -qp", rpmFile); //$NON-NLS-1$
	}
	
	public static String getPreUninstallScript(IFile rpmFile) throws CoreException {
		return query("--qf %{PREUN} -qp", rpmFile); //$NON-NLS-1$
	}
	
	public static String getPostUninstallScript(IFile rpmFile) throws CoreException {
		return query("--qf %{POSTUN} -qp", rpmFile); //$NON-NLS-1$
	}
	
	public static String getProvides(IFile rpmFile) throws CoreException {
		return query("-ql -qp", rpmFile); //$NON-NLS-1$
	}
	
	private static String query(String args, IFile rpmFile) throws CoreException {
		IEclipsePreferences node = new DefaultScope().getNode(RPMCorePlugin.ID);
		String rpmCmd = node.get(IRPMConstants.RPM_CMD, ""); //$NON-NLS-1$
		String command = rpmCmd + " " + args + " " + //$NON-NLS-1$ //$NON-NLS-2$
			rpmFile.getLocation().toOSString();
		ShellScript script = new ShellScript(command, 0);
		return script.execNoLog();
	}
	
}
