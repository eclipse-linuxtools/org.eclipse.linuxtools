/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat Inc. and others.
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
package org.eclipse.linuxtools.jdt.docker.launcher;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.StandardVMType;
import org.eclipse.jdt.launching.AbstractVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.LibraryLocation;
import org.eclipse.linuxtools.docker.core.DockerConnectionManager;
import org.eclipse.linuxtools.docker.core.IDockerImage;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;

public class ContainerVMInstall extends AbstractVMInstall {

	private ILaunchConfiguration config;
	private String name;
	private File installLocation;
	private IDockerImage image;
	private int port;
	private String javaVersion;

	public ContainerVMInstall (ILaunchConfiguration cfg, IDockerImage img, int port) {
		// values are ignored (overriden by getVMInstallType() and getId()
		super(new StandardVMType(), img.id());
		this.config = cfg;
		this.image = img;
		this.port = port;
	}

	@Override
	public IVMRunner getVMRunner(String mode) {
		// We hard-coded ContainerVMRunner
		return null;
	}

	@Override
	public String getId() {
		return image.id();
	}

	@Override
	public String getName() {
		if (name == null) {
			DockerConnection conn = getConnection();
			ImageQuery q = new ImageQuery(conn, image.id());
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
			DockerConnection conn = getConnection();
			ImageQuery q = new ImageQuery(conn, image.id());
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

	public int getPort() {
		return port;
	}

	// org.eclipse.jdt.internal.launching.StandardVMType#findJavaExecutable(File)
	public File findJavaExecutable(File vmInstallLocation) {
		final String JRE = "jre"; //$NON-NLS-1$
		final String[] fgCandidateJavaFiles = {"javaw", "javaw.exe", "java", "java.exe", "j9w", "j9w.exe", "j9", "j9.exe"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
		final String[] fgCandidateJavaLocations = {"bin" + UnixFile.separatorChar, JRE + UnixFile.separatorChar + "bin" + UnixFile.separatorChar}; //$NON-NLS-1$ //$NON-NLS-2$

		DockerConnection conn = getConnection();
		ImageQuery q = new ImageQuery(conn, image.id());

		// Try each candidate in order.  The first one found wins.  Thus, the order
		// of fgCandidateJavaLocations and fgCandidateJavaFiles is significant.
		for (int i = 0; i < fgCandidateJavaFiles.length; i++) {
			for (int j = 0; j < fgCandidateJavaLocations.length; j++) {
				File javaFile = new UnixFile(vmInstallLocation, fgCandidateJavaLocations[j] + fgCandidateJavaFiles[i]);
				if (q.isFile(javaFile)) {
					q.destroy();
					return javaFile;
				}
			}
		}
		q.destroy();
		return null;
	}

	public DockerConnection getConnection () {
		String connectionURI;
		try {
			connectionURI = config.getAttribute(JavaLaunchConfigurationConstants.CONNECTION_URI, (String) null);
			return (DockerConnection) DockerConnectionManager.getInstance().getConnectionByUri(connectionURI);
		} catch (CoreException e) {
		}
		return null;
	}

	@Override
	public String getVMArgs() {
		return null;
	}

	@Override
	public void setVMArgs(String vmArgs) {
	}

	@Override
	public String getJavaVersion() {
		if (javaVersion == null) {
			DockerConnection conn = getConnection();
			ImageQuery q = new ImageQuery(conn, image.id());
			javaVersion = String.valueOf(q.getJavaVersion());
			q.destroy();
		}
		return javaVersion;
	}

}
