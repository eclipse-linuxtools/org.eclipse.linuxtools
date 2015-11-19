/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerPortMapping;

import com.spotify.docker.client.messages.Container;

public class DockerContainer implements IDockerContainer {

	private IDockerConnection parent;
	private String id;
	private List<String> names;
	private String image;
	private String command;
	private Long created;
	private String status;
	private List<IDockerPortMapping> ports;
	private Long sizeRw;
	private Long sizeRootFs;
	private IDockerContainerInfo containerInfo;

	public DockerContainer(final IDockerConnection connection,
			Container container) {
		this.parent = connection;
		this.id = container.id();
		this.image = container.image();
		this.command = container.command();
		this.created = container.created();
		this.status = container.status();
		this.names = new ArrayList<>();
		for (String name : container.names()) {
			if (name.startsWith("/")) {
				this.names.add(name.substring(1));
			} else {
				this.names.add(name);
			}
		}
		this.sizeRw = container.sizeRw();
		this.sizeRootFs = container.sizeRootFs();
		this.ports = new ArrayList<>();
		for (Container.PortMapping port : container.ports()) {
			final DockerPortMapping portMapping = new DockerPortMapping(
					port.getPrivatePort(), port.getPublicPort(), port.getType(),
					port.getIp());
			ports.add(portMapping);
		}
		// TODO: include volumes
	}

	@Override
	public IDockerConnection getConnection() {
		return parent;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String image() {
		return image;
	}

	@Override
	public String command() {
		return command;
	}

	@Override
	public Long created() {
		return created;
	}

	@Override
	public String status() {
		return status;
	}

	@Override
	public Long sizeRw() {
		return sizeRw;
	}

	@Override
	public Long sizeRootFs() {
		return sizeRootFs;
	}

	@Override
	public List<IDockerPortMapping> ports() {
		return ports;
	}

	@Override
	public String name() {
		return names.get(0);
	}

	@Override
	public List<String> names() {
		return names;
	}

	
	@Override
	public IDockerContainerInfo info() {
		return info(false);
	}

	@Override
	public IDockerContainerInfo info(final boolean force) {
		if (force || isInfoLoaded()) {
			this.containerInfo = this.parent.getContainerInfo(id);
		}
		return this.containerInfo;
	}

	@Override
	public boolean isInfoLoaded() {
		return this.containerInfo != null;
	}

	@Override
	public String toString() {
		return "Container: id=" + id() + "\n" + "  image=" + image() + "\n"
				+ "  created=" + created() + "\n" + "  command=" + command()
				+ "\n" + "  status=<" + status() + ">\n" + "  name="
				+ name() + "\n";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DockerContainer other = (DockerContainer) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}
