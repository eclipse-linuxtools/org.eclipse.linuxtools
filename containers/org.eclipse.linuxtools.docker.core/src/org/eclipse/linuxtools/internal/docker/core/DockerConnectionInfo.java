/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.core;

import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionInfo;

import com.spotify.docker.client.messages.Info;
import com.spotify.docker.client.messages.Version;

/**
 * Info about a given {@link IDockerConnection}
 * @author xcoulon
 *
 */
public class DockerConnectionInfo implements IDockerConnectionInfo {

	private final int containers;
	private final boolean debug;
	private final String executionDriver;
	private final int fileDescriptors;
	private final int goroutines;
	private final int images;
	private final String initPath;
	private final String kernelVersion;
	private final boolean memoryLimit;
	private final String storageDriver;
	private final boolean swapLimit;
	private final String apiVersion;
	private final String gitCommit;
	private final String os;
	private final String version;
	private final List<List<String>> driverStatus;
	private final int cpuNumber;
	private final long totalMemory;
	private final String name;
	private final String id;
	private final String initSha1;
	private final String indexServerAddress;
	private final boolean ipv4Forwarding;
	private final List<String> labels;
	private final String dockerRootDir;
	
	public DockerConnectionInfo(final Info info, final Version version) {
		this.containers = info != null ? info.containers() : -1;
		this.debug = info != null ? info.debug() : false;
		this.executionDriver = info != null ? info.executionDriver() : null;
		this.fileDescriptors = info != null ? info.fileDescriptors() : -1;
		this.goroutines = info != null ? info.goroutines() : -1;
		this.images = info != null ? info.images() : -1;
		this.initPath = info != null ? info.initPath() : null;
		this.kernelVersion = info != null ? info.kernelVersion() : null;
		this.memoryLimit = info != null ? info.memoryLimit() : false;
		this.storageDriver = info != null ? info.storageDriver() : null;
		this.swapLimit = info != null ? info.swapLimit() : false;
		this.apiVersion = version != null ? version.apiVersion() : null;
		this.gitCommit = version != null ? version.gitCommit() : null;
		this.os = version != null ? version.os() : "";
		this.version = version != null ? version.version() : null;
		this.driverStatus = info != null ? info.driverStatus() : null;
		this.cpuNumber = info != null ? info.cpus() : -1;
		this.totalMemory = info != null ? info.memTotal() : -1;
		this.name = info != null ? info.name() : null;
		this.id = info != null ? info.id() : null;
		this.initSha1 = info != null ? info.initSha1() : null;
		this.ipv4Forwarding = info != null ? info.ipv4Forwarding() : false;
		this.indexServerAddress = info != null ? info.indexServerAddress()
				: null;
		this.labels = info != null ? info.labels() : null;
		this.dockerRootDir = info != null ? info.dockerRootDir() : null;
		
	}

	@Override
	public boolean isMemoryLimit() {
		return memoryLimit;
	}

	@Override
	public int getContainers() {
		return containers;
	}

	@Override
	public boolean isDebug() {
		return debug;
	}

	@Override
	public String getExecutionDriver() {
		return executionDriver;
	}

	@Override
	public int getFileDescriptors() {
		return fileDescriptors;
	}

	@Override
	public int getGoroutines() {
		return goroutines;
	}

	@Override
	public int getImages() {
		return images;
	}

	@Override
	public String getInitPath() {
		return initPath;
	}

	@Override
	public String getKernelVersion() {
		return kernelVersion;
	}

	@Override
	public String getStorageDriver() {
		return storageDriver;
	}

	@Override
	public boolean isSwapLimit() {
		return swapLimit;
	}

	@Override
	public String getApiVersion() {
		return apiVersion;
	}

	@Override
	public String getGitCommit() {
		return gitCommit;
	}

	@Override
	public String getOs() {
		return os;
	}

	@Override
	public String getVersion() {
		return version;
	}

	/**
	 * @return the driverStatus
	 */
	@Override
	public List<List<String>> getDriverStatus() {
		return driverStatus;
	}

	@Override
	public int getCPUNumber() {
		return cpuNumber;
	}

	@Override
	public long getTotalMemory() {
		return totalMemory;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getInitSha1() {
		return initSha1;
	}

	@Override
	public String getIndexServerAddress() {
		return indexServerAddress;
	}

	@Override
	public boolean isIPv4Forwarding() {
		return ipv4Forwarding;
	}

	@Override
	public List<String> getLabels() {
		return labels;
	}

	@Override
	public String getDockerRootDir() {
		return dockerRootDir;
	}

}
