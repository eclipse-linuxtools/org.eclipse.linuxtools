/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.jdt.docker.launcher;

import java.io.File;
import java.net.URL;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;

public class ContainerVMInstall implements IVMInstall {

	private ILaunchConfiguration config;
	private String name;
	private File installLocation;

	public ContainerVMInstall (ILaunchConfiguration cfg) {
		this.config = cfg;
	}

	@Override
	public IVMRunner getVMRunner(String mode) {
		// We hard-coded ContainerVMRunner
		return null;
	}

	@Override
	public String getId() {
		//TODO : Should be something the image name that got passed in
		return Long.toString(System.currentTimeMillis());
	}

	@Override
	public String getName() {
		if (name == null) {
			DockerConnection conn = (DockerConnection) DockerConnectionManager.getInstance().getFirstConnection();
			ImageQuery q = new ImageQuery(conn, "fedora-java");
			name = q.getDefaultJVMName();
			q.destroy();
		}
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public File getInstallLocation() {
		if (installLocation == null) {
			DockerConnection conn = (DockerConnection) DockerConnectionManager.getInstance().getFirstConnection();
			ImageQuery q = new ImageQuery(conn, "fedora-java");
			installLocation = q.getDefaultJVMInstallLocation();
			q.destroy();
		}
		return installLocation;
	}

	@Override
	public void setInstallLocation(File installLocation) {
		this.installLocation = installLocation;
	}

	@Override
	public IVMInstallType getVMInstallType() {
		return null;
	}

	@Override
	public LibraryLocation[] getLibraryLocations() {
		return null;
	}

	@Override
	public void setLibraryLocations(LibraryLocation[] locations) {
	}

	@Override
	public void setJavadocLocation(URL url) {
	}

	@Override
	public URL getJavadocLocation() {
		return null;
	}

	@Override
	public String[] getVMArguments() {
		return null;
	}

	@Override
	public void setVMArguments(String[] vmArgs) {
	}

	// org.eclipse.jdt.internal.launching.StandardVM#getJavaExecutable()
	public File getJavaExecutable() {
		File installLocation = getInstallLocation();
        if (installLocation != null) {
            return findJavaExecutable(installLocation);
        }
        return null;
	}

	// org.eclipse.jdt.internal.launching.StandardVMType#findJavaExecutable(File)
	public static File findJavaExecutable(File vmInstallLocation) {
		final String JRE = "jre"; //$NON-NLS-1$
		final String[] fgCandidateJavaFiles = {"javaw", "javaw.exe", "java", "java.exe", "j9w", "j9w.exe", "j9", "j9.exe"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
		final String[] fgCandidateJavaLocations = {"bin" + File.separatorChar, JRE + File.separatorChar + "bin" + File.separatorChar}; //$NON-NLS-1$ //$NON-NLS-2$

		DockerConnection conn = (DockerConnection) DockerConnectionManager.getInstance().getFirstConnection();
		ImageQuery q = new ImageQuery(conn, "fedora-java");

		// Try each candidate in order.  The first one found wins.  Thus, the order
		// of fgCandidateJavaLocations and fgCandidateJavaFiles is significant.
		for (int i = 0; i < fgCandidateJavaFiles.length; i++) {
			for (int j = 0; j < fgCandidateJavaLocations.length; j++) {
				File javaFile = new File(vmInstallLocation, fgCandidateJavaLocations[j] + fgCandidateJavaFiles[i]);
				if (q.isFile(javaFile)) {
					q.destroy();
					return javaFile;
				}
			}
		}
		q.destroy();
		return null;
	}

}
