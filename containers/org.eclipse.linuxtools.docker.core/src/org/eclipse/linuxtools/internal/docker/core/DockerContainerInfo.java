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
		this.id = info.id();
		this.created = info.created();
		this.path = info.path();
		this.args = info.args();
		this.config = new DockerContainerConfig(info.config());
		this.hostConfig = new DockerHostConfig(info.hostConfig());
		this.state = new DockerContainerState(info.state());
		this.image = info.image();
		this.networkSettings = new DockerNetworkSettings(info.networkSettings());
		this.resolvConfPath = info.resolvConfPath();
		this.hostnamePath = info.hostnamePath();
		this.hostsPath = info.hostsPath();
		this.name = info.name();
		this.driver = info.driver();
		this.execDriver = info.execDriver();
		this.processLabel = info.processLabel();
		this.mountLabel = info.mountLabel();
		this.volumes = info.volumes();
		this.volumesRW = info.volumesRW();
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
