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
package org.eclipse.linuxtools.internal.docker.core;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerVersion;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.Version;

public class DockerVersion implements IDockerVersion {

	private IDockerConnection parent;
	private String apiVersion;
	private String arch;
	private String gitCommit;
	private String goVersion;
	private String kernelVersion;
	private String os;
	private String version;

	/**
	 * Constructor.
	 * 
	 * @param connection
	 *            the Docker connection
	 * @param version
	 *            the underlying {@link Version} data returned by the
	 *            {@link DockerClient}
	 */
	public DockerVersion(final IDockerConnection connection,
			final Version version) {
		this.parent = connection;
		this.apiVersion = version.apiVersion();
		this.arch = version.arch();
		this.gitCommit = version.gitCommit();
		this.goVersion = version.goVersion();
		this.kernelVersion = version.kernelVersion();
		this.os = version.os();
		this.version = version.version();
	}

	@Override
	public IDockerConnection getConnection() {
		return parent;
	}

	@Override
	public String apiVersion() {
		return apiVersion;
	}

	@Override
	public String arch() {
		return arch;
	}

	@Override
	public String gitCommit() {
		return gitCommit;
	}

	@Override
	public String goVersion() {
		return goVersion;
	}

	@Override
	public String kernelVersion() {
		return kernelVersion;
	}

	@Override
	public String os() {
		return os;
	}

	@Override
	public String version() {
		return version;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final DockerVersion version1 = (DockerVersion) o;

		if (apiVersion != null ? !apiVersion.equals(version1.apiVersion)
				: version1.apiVersion != null) {
			return false;
		}
		if (arch != null ? !arch.equals(version1.arch)
				: version1.arch != null) {
			return false;
		}
		if (gitCommit != null ? !gitCommit.equals(version1.gitCommit)
				: version1.gitCommit != null) {
			return false;
		}
		if (goVersion != null ? !goVersion.equals(version1.goVersion)
				: version1.goVersion != null) {
			return false;
		}
		if (kernelVersion != null
				? !kernelVersion.equals(version1.kernelVersion)
				: version1.kernelVersion != null) {
			return false;
		}
		if (os != null ? !os.equals(version1.os) : version1.os != null) {
			return false;
		}
		if (version != null ? !version.equals(version1.version)
				: version1.version != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = apiVersion != null ? apiVersion.hashCode() : 0;
		result = 31 * result + (arch != null ? arch.hashCode() : 0);
		result = 31 * result + (gitCommit != null ? gitCommit.hashCode() : 0);
		result = 31 * result + (goVersion != null ? goVersion.hashCode() : 0);
		result = 31 * result
				+ (kernelVersion != null ? kernelVersion.hashCode() : 0);
		result = 31 * result + (os != null ? os.hashCode() : 0);
		result = 31 * result + (version != null ? version.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Version: apiVersion=" + apiVersion + "\n" + "arch" + arch + "\n"
				+ "gitCommit" + gitCommit + "\n" + "goVersion" + goVersion
				+ "\n" + "kernelVersion" + kernelVersion + "\n" + "os" + os
				+ "\n" + "version" + version + "\n";
	}

}
