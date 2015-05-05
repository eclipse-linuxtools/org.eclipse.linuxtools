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

import java.util.Date;

import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;

import com.spotify.docker.client.messages.ImageInfo;

public class DockerImageInfo implements IDockerImageInfo {

	private String id;
	private String parent;
	private String comment;
	private Date created;
	private String container;
	private IDockerContainerConfig containerConfig;
	private String dockerVersion;
	private String author;
	private IDockerContainerConfig config;
	private String architecture;
	private String os;
	private Long size;

	public DockerImageInfo(ImageInfo info) {
		this.id = info.id();
		this.parent = info.parent();
		this.comment = info.comment();
		this.created = info.created();
		this.container = info.container();
		this.containerConfig = new DockerContainerConfig(info.containerConfig());
		this.dockerVersion = info.dockerVersion();
		this.author = info.author();
		this.config = new DockerContainerConfig(info.config());
		this.architecture = info.architecture();
		this.os = info.os();
		this.size = info.size();
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String parent() {
		return parent;
	}

	@Override
	public String comment() {
		return comment;
	}

	@Override
	public Date created() {
		return created;
	}

	@Override
	public String container() {
		return container;
	}

	@Override
	public IDockerContainerConfig containerConfig() {
		return containerConfig;
	}

	@Override
	public String dockerVersion() {
		return dockerVersion;
	}

	public void setDockerVersion(String dockerVersion) {
		this.dockerVersion = dockerVersion;
	}

	@Override
	public String author() {
		return author;
	}

	@Override
	public IDockerContainerConfig config() {
		return config;
	}

	@Override
	public String architecture() {
		return architecture;
	}

	@Override
	public String os() {
		return os;
	}

	@Override
	public Long size() {
		return size;
	}

}
