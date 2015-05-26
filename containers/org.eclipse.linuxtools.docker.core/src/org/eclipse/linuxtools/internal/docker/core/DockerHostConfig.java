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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.linuxtools.docker.core.IDockerConfParameter;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;

import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.HostConfig.LxcConfParameter;
import com.spotify.docker.client.messages.PortBinding;

public class DockerHostConfig implements IDockerHostConfig {

	private final List<String> binds;
	private final String containerIDFile;
	private final List<IDockerConfParameter> lxcConf;
	private final boolean privileged;
	private final Map<String, List<IDockerPortBinding>> portBindings;
	private final List<String> links;
	private final boolean publishAllPorts;
	private final List<String> dns;
	private final List<String> dnsSearch;
	private final List<String> volumesFrom;
	private final String networkMode;

	public DockerHostConfig(final HostConfig hostConfig) {
		this.binds = hostConfig.binds();
		this.containerIDFile = hostConfig.containerIDFile();
		this.lxcConf = new ArrayList<>();
		if(hostConfig.lxcConf() != null) {
			for (LxcConfParameter lxcConfParameter : hostConfig.lxcConf()) {
				this.lxcConf.add(new DockerConfParameter(lxcConfParameter));
			}
		}
		this.privileged = hostConfig.privileged() != null
				? hostConfig.privileged() : false;
		this.portBindings = new HashMap<>();
		if(hostConfig != null && hostConfig.portBindings() != null) {
			for(Entry<String, List<PortBinding>> entry : hostConfig.portBindings().entrySet()) {
				final List<IDockerPortBinding> portBindings = new ArrayList<>();
				for (PortBinding portBinding : entry.getValue()) {
					portBindings.add(new DockerPortBinding(portBinding));
				}
				this.portBindings.put(entry.getKey(), portBindings);
			}
		}
		this.links = hostConfig.links();
		this.publishAllPorts = hostConfig.publishAllPorts() != null
				? hostConfig.publishAllPorts() : false;
		this.dns = hostConfig.dns();
		this.dnsSearch = hostConfig.dnsSearch();
		this.volumesFrom = hostConfig.volumesFrom();
		this.networkMode = hostConfig.networkMode();
	}

	private DockerHostConfig(final Builder builder) {
		this.binds = builder.binds;
		this.containerIDFile = builder.containerIDFile;
		this.lxcConf = builder.lxcConf;
		this.privileged = builder.privileged != null ? builder.privileged
				: false;
		this.portBindings = builder.portBindings;
		this.links = builder.links;
		this.publishAllPorts = builder.publishAllPorts != null
				? builder.publishAllPorts : false;
		this.dns = builder.dns;
		this.dnsSearch = builder.dnsSearch;
		this.volumesFrom = builder.volumesFrom;
		this.networkMode = builder.networkMode;
	}

	@Override
	public List<String> binds() {
		return binds;
	}

	@Override
	public String containerIDFile() {
		return containerIDFile;
	}

	@Override
	public List<IDockerConfParameter> lxcConf() {
		return lxcConf;
	}

	@Override
	public boolean privileged() {
		return privileged;
	}

	@Override
	public Map<String, List<IDockerPortBinding>> portBindings() {
		return portBindings;
	}

	@Override
	public List<String> links() {
		return links;
	}

	@Override
	public boolean publishAllPorts() {
		return publishAllPorts;
	}

	@Override
	public List<String> dns() {
		return dns;
	}

	@Override
	public List<String> dnsSearch() {
		return dnsSearch;
	}

	@Override
	public List<String> volumesFrom() {
		return volumesFrom;
	}

	@Override
	public String networkMode() {
		return networkMode;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private List<String> binds;
		private String containerIDFile;
		private List<IDockerConfParameter> lxcConf;
		private Boolean privileged;
		private Map<String, List<IDockerPortBinding>> portBindings;
		private List<String> links;
		private Boolean publishAllPorts;
		private List<String> dns;
		private List<String> dnsSearch;
		private List<String> volumesFrom;
		private String networkMode;


		public Builder binds(final List<String> binds) {
			this.binds = new ArrayList<>(binds);
			return this;
		}

		public Builder binds(final String... binds) {
			this.binds = Arrays.asList(binds);
			return this;
		}

		public List<String> binds() {
			return binds;
		}

		public Builder containerIDFile(final String containerIDFile) {
			this.containerIDFile = containerIDFile;
			return this;
		}

		public String containerIDFile() {
			return containerIDFile;
		}

		public Builder lxcConf(final List<IDockerConfParameter> lxcConf) {
			this.lxcConf = new ArrayList<>(lxcConf);
			return this;
		}

		public Builder lxcConf(final IDockerConfParameter... lxcConf) {
			this.lxcConf = Arrays.asList(lxcConf);
			return this;
		}

		public List<IDockerConfParameter> lxcConf() {
			return lxcConf;
		}

		public Builder privileged(final Boolean privileged) {
			this.privileged = privileged;
			return this;
		}

		public Boolean privileged() {
			return privileged;
		}

		public Builder portBindings(
				final Map<String, List<IDockerPortBinding>> portBindings) {
			this.portBindings = portBindings;
			return this;
		}

		public Map<String, List<IDockerPortBinding>> portBindings() {
			return portBindings;
		}

		public Builder links(final List<String> links) {
			this.links = new ArrayList<>(links);
			return this;
		}

		public Builder links(final String... links) {
			this.links = Arrays.asList(links);
			return this;
		}

		public List<String> links() {
			return links;
		}

		public Builder publishAllPorts(final Boolean publishAllPorts) {
			this.publishAllPorts = publishAllPorts;
			return this;
		}

		public Boolean publishAllPorts() {
			return publishAllPorts;
		}

		public Builder dns(final List<String> dns) {
			this.dns = new ArrayList<>(dns);
			return this;
		}

		public Builder dns(final String... dns) {
			this.dns = Arrays.asList(dns);
			return this;
		}

		public List<String> dns() {
			return dns;
		}

		public Builder dnsSearch(final List<String> dnsSearch) {
			this.dnsSearch = new ArrayList<>(dnsSearch);
			return this;
		}

		public Builder dnsSearch(final String... dnsSearch) {
			this.dnsSearch = Arrays.asList(dnsSearch);
			return this;
		}

		public List<String> dnsSearch() {
			return dnsSearch;
		}

		public Builder volumesFrom(final List<String> volumesFrom) {
			this.volumesFrom = new ArrayList<>(volumesFrom);
			return this;
		}

		public Builder volumesFrom(final String... volumesFrom) {
			this.volumesFrom = Arrays.asList(volumesFrom);
			return this;
		}

		public List<String> volumesFrom() {
			return volumesFrom;
		}

		public Builder networkMode(final String networkMode) {
			this.networkMode = networkMode;
			return this;
		}

		public String networkMode() {
			return networkMode;
		}

		public IDockerHostConfig build() {
			return new DockerHostConfig(this);
		}

	}
}
