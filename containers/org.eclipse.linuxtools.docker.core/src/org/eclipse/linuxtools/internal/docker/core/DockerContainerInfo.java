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
package org.eclipse.linuxtools.internal.docker.core;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerContainerState;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerNetworkSettings;

import com.spotify.docker.client.messages.ContainerInfo;

public class DockerContainerInfo implements IDockerContainerInfo {

	private String id;
	private Date created;
	private String path;
	private List<String> args;
	private IDockerContainerConfig config;
	private IDockerHostConfig hostConfig;
	private IDockerContainerState state;
	private String image;
	private IDockerNetworkSettings networkSettings;
	private String resolvConfPath;
	private String hostnamePath;
	private String hostsPath;
	private String name;
	private String driver;
	private String execDriver;
	private String processLabel;
	private String mountLabel;
	private Map<String, String> volumes;
	private Map<String, Boolean> volumesRW;

	public DockerContainerInfo (final ContainerInfo info) {
		this.id = info != null ? info.id() : null;
		this.created = info != null ? info.created() : null;
		this.path = info != null ? info.path() : null;
		this.args = info != null ? info.args() : null;
		this.config = info != null && info.config() != null
				? new DockerContainerConfig(info.config()) : null;
		this.hostConfig = info != null && info.hostConfig() != null
				? new DockerHostConfig(info.hostConfig()) : null;
		this.state = info != null && info.state() != null
				? new DockerContainerState(info.state()) : null;
		this.image = info != null ? info.image() : null;
		this.networkSettings = info != null && info.networkSettings() != null
				? new DockerNetworkSettings(info.networkSettings()) : null;
		this.resolvConfPath = info != null ? info.resolvConfPath() : null;
		this.hostnamePath = info != null ? info.hostnamePath() : null;
		this.hostsPath = info != null ? info.hostsPath() : null;
		this.name = info != null ? info.name() : null;
		this.driver = info != null ? info.driver() : null;
		this.execDriver = info != null ? info.execDriver() : null;
		this.processLabel = info != null ? info.processLabel() : null;
		this.mountLabel = info != null ? info.mountLabel() : null;
		this.volumes = info != null ? info.volumes() : null;
		this.volumesRW = info != null ? info.volumesRW() : null;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public Date created() {
		return created == null ? null : new Date(created.getTime());
	}

	@Override
	public String path() {
		return path;
	}

	@Override
	public List<String> args() {
		return args;
	}

	@Override
	public IDockerContainerConfig config() {
		return config;
	}

	@Override
	public IDockerHostConfig hostConfig() {
		return hostConfig;
	}

	@Override
	public IDockerContainerState state() {
		return state;
	}

	@Override
	public String image() {
		return image;
	}

	@Override
	public IDockerNetworkSettings networkSettings() {
		return networkSettings;
	}

	@Override
	public String resolvConfPath() {
		return resolvConfPath;
	}

	@Override
	public String hostnamePath() {
		return hostnamePath;
	}

	@Override
	public String hostsPath() {
		return hostsPath;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public String driver() {
		return driver;
	}

	@Override
	public String execDriver() {
		return execDriver;
	}

	@Override
	public String processLabel() {
		return processLabel;
	}

	@Override
	public String mountLabel() {
		return mountLabel;
	}

	@Override
	public Map<String, String> volumes() {
		return volumes;
	}

	@Override
	public Map<String, Boolean> volumesRW() {
		return volumesRW;
	}

}
