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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;

public class DockerContainerConfig implements IDockerContainerConfig {

	private final String hostname;
	private final String domainname;
	private final String user;
	private final Long memory;
	private final Long memorySwap;
	private final Long cpuShares;
	private final String cpuset;
	private final boolean attachStdin;
	private final boolean attachStdout;
	private final boolean attachStderr;
	private final List<String> portSpecs;
	private final Set<String> exposedPorts;
	private final boolean tty;
	private final boolean openStdin;
	private final boolean stdinOnce;
	private final List<String> env;
	private final List<String> cmd;
	private final String image;
	private final Set<String> volumes;
	private final String workingDir;
	private final List<String> entrypoint;
	private final boolean networkDisabled;
	private final List<String> onBuild;

	public DockerContainerConfig(final ContainerConfig containerConfig) {
		HostConfig hc = containerConfig.hostConfig();
		this.hostname = containerConfig.hostname();
		this.domainname = containerConfig.domainname();
		this.user = containerConfig.user();
		this.memory = hc != null ? hc.memory() : null;
		this.memorySwap = hc != null ? hc.memorySwap() : null;
		this.cpuShares = hc != null ? hc.cpuShares() : null;
		this.cpuset = hc != null ? hc.cpusetCpus() : null;
		this.attachStdin = containerConfig.attachStdin() != null
				? containerConfig.attachStdin() : false;
		this.attachStdout = containerConfig.attachStdout() != null
				? containerConfig.attachStdout() : false;
		this.attachStderr = containerConfig.attachStderr() != null
				? containerConfig.attachStderr() : false;
		this.portSpecs = containerConfig.portSpecs();
		this.exposedPorts = containerConfig.exposedPorts();
		this.tty = containerConfig.tty() != null ? containerConfig.tty()
				: false;
		this.openStdin = containerConfig.openStdin() != null
				? containerConfig.openStdin() : false;
		this.stdinOnce = containerConfig.stdinOnce() != null
				? containerConfig.stdinOnce() : false;
		this.env = containerConfig.env();
		this.cmd = containerConfig.cmd();
		this.image = containerConfig.image();
		this.volumes = containerConfig.volumes();
		this.workingDir = containerConfig.workingDir();
		this.entrypoint = containerConfig.entrypoint();
		this.networkDisabled = containerConfig.networkDisabled() != null
				? containerConfig.networkDisabled() : false;
		this.onBuild = containerConfig.onBuild();
	}

	private DockerContainerConfig(final Builder builder) {
		this.hostname = builder.hostname;
		this.domainname = builder.domainname;
		this.user = builder.user;
		this.memory = builder.memory;
		this.memorySwap = builder.memorySwap;
		this.cpuShares = builder.cpuShares;
		this.cpuset = builder.cpuset;
		this.attachStdin = builder.attachStdin != null ? builder.attachStdin
				: false;
		this.attachStdout = builder.attachStdout != null ? builder.attachStdout
				: false;
		this.attachStderr = builder.attachStderr != null ? builder.attachStderr
				: false;
		this.portSpecs = builder.portSpecs;
		this.exposedPorts = builder.exposedPorts;
		this.tty = builder.tty != null ? builder.tty : false;
		this.openStdin = builder.openStdin != null ? builder.openStdin : false;
		this.stdinOnce = builder.stdinOnce != null ? builder.stdinOnce : false;
		this.env = builder.env;
		this.cmd = builder.cmd;
		this.image = builder.image;
		this.volumes = builder.volumes;
		this.workingDir = builder.workingDir;
		this.entrypoint = builder.entrypoint;
		this.networkDisabled = builder.networkDisabled != null
				? builder.networkDisabled : false;
		this.onBuild = builder.onBuild;
	}

	@Override
	public String hostname() {
		return hostname;
	}

	@Override
	public String domainname() {
		return domainname;
	}

	@Override
	public String user() {
		return user;
	}

	@Override
	public Long memory() {
		return memory;
	}

	@Override
	public Long memorySwap() {
		return memorySwap;
	}

	@Override
	public Long cpuShares() {
		return cpuShares;
	}

	@Override
	public String cpuset() {
		return cpuset;
	}

	@Override
	public boolean attachStdin() {
		return attachStdin;
	}

	@Override
	public boolean attachStdout() {
		return attachStdout;
	}

	@Override
	public boolean attachStderr() {
		return attachStderr;
	}

	@Override
	public List<String> portSpecs() {
		if (portSpecs == null) {
			return Collections.emptyList();
		}
		return portSpecs;
	}

	@Override
	public Set<String> exposedPorts() {
		if (exposedPorts == null) {
			return Collections.emptySet();
		}
		return exposedPorts;
	}

	@Override
	public boolean tty() {
		return tty;
	}

	@Override
	public boolean openStdin() {
		return openStdin;
	}

	@Override
	public boolean stdinOnce() {
		return stdinOnce;
	}

	@Override
	public List<String> env() {
		if (env == null) {
			return Collections.emptyList();
		}
		return env;
	}

	@Override
	public List<String> cmd() {
		if (cmd == null) {
			return Collections.emptyList();
		}
		return cmd;
	}

	@Override
	public String image() {
		return image;
	}

	@Override
	public Set<String> volumes() {
		if (volumes == null) {
			return Collections.emptySet();
		}
		return volumes;
	}

	@Override
	public String workingDir() {
		return workingDir;
	}

	@Override
	public List<String> entrypoint() {
		if (entrypoint == null) {
			return Collections.emptyList();
		}
		return entrypoint;
	}

	@Override
	public boolean networkDisabled() {
		return networkDisabled;
	}

	@Override
	public List<String> onBuild() {
		if (onBuild == null) {
			return Collections.emptyList();
		}
		return onBuild;
	}

	public static class Builder {

		private String hostname;
		private String domainname;
		private String user;
		private Long memory;
		private Long memorySwap;
		private Long cpuShares;
		private String cpuset;
		private Boolean attachStdin;
		private Boolean attachStdout;
		private Boolean attachStderr;
		private List<String> portSpecs;
		private Set<String> exposedPorts;
		private Boolean tty;
		private Boolean openStdin;
		private Boolean stdinOnce;
		private List<String> env;
		private List<String> cmd;
		private String image;
		private Set<String> volumes;
		private String workingDir;
		private List<String> entrypoint;
		private Boolean networkDisabled;
		private List<String> onBuild;

		public Builder hostname(final String hostname) {
			this.hostname = hostname;
			return this;
		}

		public String hostname() {
			return hostname;
		}

		public Builder domainname(final String domainname) {
			this.domainname = domainname;
			return this;
		}

		public String domainname() {
			return domainname;
		}

		public Builder user(final String user) {
			this.user = user;
			return this;
		}

		public String user() {
			return user;
		}

		public Builder memory(final Long memory) {
			this.memory = memory;
			return this;
		}

		public Long memory() {
			return memory;
		}

		public Builder memorySwap(final Long memorySwap) {
			this.memorySwap = memorySwap;
			return this;
		}

		public Long memorySwap() {
			return memorySwap;
		}

		public Builder cpuShares(final Long cpuShares) {
			this.cpuShares = cpuShares;
			return this;
		}

		public Long cpuShares() {
			return cpuShares;
		}

		public Builder cpuset(final String cpuset) {
			this.cpuset = cpuset;
			return this;
		}

		public String cpuset() {
			return cpuset;
		}

		public Builder attachStdin(final Boolean attachStdin) {
			this.attachStdin = attachStdin;
			return this;
		}

		public Boolean attachStdin() {
			return attachStdin;
		}

		public Builder attachStdout(final Boolean attachStdout) {
			this.attachStdout = attachStdout;
			return this;
		}

		public Boolean attachStdout() {
			return attachStdout;
		}

		public Builder attachStderr(final Boolean attachStderr) {
			this.attachStderr = attachStderr;
			return this;
		}

		public Boolean attachStderr() {
			return attachStderr;
		}

		public Builder portSpecs(final List<String> portSpecs) {
			this.portSpecs = new ArrayList<>(portSpecs);
			return this;
		}

		public Builder portSpecs(final String... portSpecs) {
			this.portSpecs = Arrays.asList(portSpecs);
			return this;
		}

		public List<String> portSpecs() {
			return portSpecs;
		}

		public Builder exposedPorts(final Set<String> exposedPorts) {
			this.exposedPorts = new TreeSet<>(exposedPorts);
			return this;
		}

		public Builder exposedPorts(final String... exposedPorts) {
			this.exposedPorts = new TreeSet<>(Arrays.asList(exposedPorts));
			return this;
		}

		public Set<String> exposedPorts() {
			return exposedPorts;
		}

		public Builder tty(final Boolean tty) {
			this.tty = tty;
			return this;
		}

		public Boolean tty() {
			return tty;
		}

		public Builder openStdin(final Boolean openStdin) {
			this.openStdin = openStdin;
			return this;
		}

		public Boolean openStdin() {
			return openStdin;
		}

		public Builder stdinOnce(final Boolean stdinOnce) {
			this.stdinOnce = stdinOnce;
			return this;
		}

		public Boolean stdinOnce() {
			return stdinOnce;
		}

		public Builder env(final List<String> env) {
			this.env = new ArrayList<>(env);
			return this;
		}

		public Builder env(final String... env) {
			this.env = Arrays.asList(env);
			return this;
		}

		public List<String> env() {
			return env;
		}

		public Builder cmd(final List<String> cmd) {
			this.cmd = cmd;
			return this;
		}

		public Builder cmd(final String... cmd) {
			return cmd(Arrays.asList(cmd));
		}

		public Builder cmd(final String cmd) {
			if (cmd != null && !cmd.isEmpty()) {
				return cmd(cmd.split(" "));
			}
			return this;
		}

		public List<String> cmd() {
			return cmd;
		}

		public Builder image(final String image) {
			this.image = image;
			return this;
		}

		public String image() {
			return image;
		}

		public Builder volumes(final Set<String> volumes) {
			this.volumes = new TreeSet<>(volumes);
			return this;
		}

		public Builder volumes(final String... volumes) {
			this.volumes = new TreeSet<>(Arrays.asList(volumes));
			return this;
		}

		public Set<String> volumes() {
			return volumes;
		}

		public Builder workingDir(final String workingDir) {
			this.workingDir = workingDir;
			return this;
		}

		public String workingDir() {
			return workingDir;
		}

		public Builder entryPoint(final List<String> entrypoint) {
			if (entrypoint != null && !entrypoint.isEmpty()) {
				this.entrypoint = new ArrayList<>(entrypoint);
			}
			return this;
		}

		public Builder entryPoint(final String... entrypoint) {
			return entryPoint(Arrays.asList(entrypoint));
		}

		public Builder entryPoint(final String entrypoint) {
			if (entrypoint != null && !entrypoint.isEmpty()) {
				return entryPoint(entrypoint.split(" "));
			} else {
				this.entrypoint = null;
			}
			return this;
		}

		public List<String> entryPoint() {
			return entrypoint;
		}

		public Builder networkDisabled(final Boolean networkDisabled) {
			this.networkDisabled = networkDisabled;
			return this;
		}

		public Boolean networkDisabled() {
			return networkDisabled;
		}

		public Builder onBuild(final List<String> onBuild) {
			this.onBuild = new ArrayList<>(onBuild);
			return this;
		}

		public Builder onBuild(final String... onBuild) {
			this.onBuild = Arrays.asList(onBuild);
			return this;
		}

		public List<String> onBuild() {
			return onBuild;
		}

		public DockerContainerConfig build() {
			return new DockerContainerConfig(this);
		}

	}
}
