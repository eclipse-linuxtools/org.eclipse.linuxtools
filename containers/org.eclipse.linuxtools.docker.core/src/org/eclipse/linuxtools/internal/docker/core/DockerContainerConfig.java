/*******************************************************************************
 * Copyright (c) 2014, 2020 Red Hat.
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
package org.eclipse.linuxtools.internal.docker.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.mandas.docker.client.messages.ContainerConfig;
import org.mandas.docker.client.messages.HostConfig;
import org.mandas.docker.client.messages.ImageConfig;

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
	private String rawcmd;
	private final List<String> cmd;
	private final String image;
	@SuppressWarnings("rawtypes")
	private final Map<String, Map> volumes;
	private final String workingDir;
	private final List<String> entrypoint;
	private final boolean networkDisabled;
	private final List<String> onBuild;
	private final Map<String, String> labels;

	public DockerContainerConfig(final ImageConfig containerConfig) {
		this.hostname = containerConfig != null ? containerConfig.hostname()
				: null;
		this.domainname = containerConfig != null ? containerConfig.domainname()
				: null;
		this.user = containerConfig != null ? containerConfig.user() : null;
		final HostConfig hc = containerConfig != null
				? containerConfig.hostConfig() : null;
		this.memory = hc != null ? hc.memory() : null;
		this.memorySwap = hc != null ? hc.memorySwap() : null;
		this.cpuShares = hc != null ? hc.cpuShares() : null;
		this.cpuset = hc != null ? hc.cpusetCpus() : null;
		this.attachStdin = containerConfig != null
				&& containerConfig.attachStdin() != null
				? containerConfig.attachStdin() : false;
		this.attachStdout = containerConfig != null
				&& containerConfig.attachStdout() != null
				? containerConfig.attachStdout() : false;
		this.attachStderr = containerConfig != null
				&& containerConfig.attachStderr() != null
				? containerConfig.attachStderr() : false;
		this.portSpecs = containerConfig != null ? containerConfig.portSpecs()
				: null;
		this.exposedPorts = containerConfig != null
				? containerConfig.exposedPorts() : null;
		this.tty = containerConfig != null && containerConfig.tty() != null
				? containerConfig.tty()
				: false;
		this.openStdin = containerConfig != null
				&& containerConfig.openStdin() != null
				? containerConfig.openStdin() : false;
		this.stdinOnce = containerConfig != null
				&& containerConfig.stdinOnce() != null
				? containerConfig.stdinOnce() : false;
		this.env = containerConfig != null ? containerConfig.env() : null;
		this.cmd = containerConfig != null ? containerConfig.cmd() : null;
		this.image = containerConfig != null ? containerConfig.image() : null;

		@SuppressWarnings("rawtypes")
		Map<String, Map> res = new HashMap<>();
		try {
			if (containerConfig != null && containerConfig.volumes() != null) {
				containerConfig.volumes().forEach(v -> res.put(v, Collections.emptyMap()));
			}
		} catch (NullPointerException e) {
		}
		this.volumes = res;
		this.workingDir = containerConfig != null ? containerConfig.workingDir()
				: null;
		this.entrypoint = containerConfig != null ? containerConfig.entrypoint()
				: null;
		this.networkDisabled = containerConfig != null
				&& containerConfig.networkDisabled() != null
				? containerConfig.networkDisabled() : false;
		this.onBuild = containerConfig != null ? containerConfig.onBuild()
				: null;
		this.labels = containerConfig != null ? containerConfig.labels() : null;
	}

	public DockerContainerConfig(final ContainerConfig containerConfig) {
		this.hostname = containerConfig != null ? containerConfig.hostname()
				: null;
		this.domainname = containerConfig != null ? containerConfig.domainname()
				: null;
		this.user = containerConfig != null ? containerConfig.user() : null;
		final HostConfig hc = containerConfig != null
				? containerConfig.hostConfig()
				: null;
		this.memory = hc != null ? hc.memory() : null;
		this.memorySwap = hc != null ? hc.memorySwap() : null;
		this.cpuShares = hc != null ? hc.cpuShares() : null;
		this.cpuset = hc != null ? hc.cpusetCpus() : null;
		this.attachStdin = containerConfig != null
				&& containerConfig.attachStdin() != null
						? containerConfig.attachStdin()
						: false;
		this.attachStdout = containerConfig != null
				&& containerConfig.attachStdout() != null
						? containerConfig.attachStdout()
						: false;
		this.attachStderr = containerConfig != null
				&& containerConfig.attachStderr() != null
						? containerConfig.attachStderr()
						: false;
		this.portSpecs = containerConfig != null ? containerConfig.portSpecs()
				: null;
		this.exposedPorts = containerConfig != null
				? containerConfig.exposedPorts()
				: null;
		this.tty = containerConfig != null && containerConfig.tty() != null
				? containerConfig.tty()
				: false;
		this.openStdin = containerConfig != null
				&& containerConfig.openStdin() != null
						? containerConfig.openStdin()
						: false;
		this.stdinOnce = containerConfig != null
				&& containerConfig.stdinOnce() != null
						? containerConfig.stdinOnce()
						: false;
		this.env = containerConfig != null ? containerConfig.env() : null;
		this.cmd = containerConfig != null ? containerConfig.cmd() : null;
		this.image = containerConfig != null ? containerConfig.image() : null;

		@SuppressWarnings("rawtypes")
		Map<String, Map> res = new HashMap<>();
		try {
			if (containerConfig != null && containerConfig.volumes() != null) {
				containerConfig.volumes()
						.forEach(v -> res.put(v, Collections.emptyMap()));
			}
		} catch (NullPointerException e) {
		}
		this.volumes = res;
		this.workingDir = containerConfig != null ? containerConfig.workingDir()
				: null;
		this.entrypoint = containerConfig != null ? containerConfig.entrypoint()
				: null;
		this.networkDisabled = containerConfig != null
				&& containerConfig.networkDisabled() != null
						? containerConfig.networkDisabled()
						: false;
		this.onBuild = containerConfig != null ? containerConfig.onBuild()
				: null;
		this.labels = containerConfig != null ? containerConfig.labels() : null;
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
		this.rawcmd = builder.rawcmd;
		this.cmd = builder.cmd;
		this.image = builder.image;
		this.volumes = builder.volumes;
		this.workingDir = builder.workingDir;
		this.entrypoint = builder.entrypoint;
		this.networkDisabled = builder.networkDisabled != null
				? builder.networkDisabled : false;
		this.onBuild = builder.onBuild;
		this.labels = builder.labels;
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

	/**
	 * @see DockerHostConfig#memory()
	 */
	@Deprecated
	@Override
	public Long memory() {
		return memory;
	}

	@Deprecated
	@Override
	public Long memorySwap() {
		return memorySwap;
	}

	/**
	 * @see DockerHostConfig#cpuShares()
	 */
	@Deprecated
	@Override
	public Long cpuShares() {
		return cpuShares;
	}

	@Deprecated
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

	public String rawcmd() {
		return rawcmd;
	}

	@Override
	public String image() {
		return image;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map<String, Map> volumes() {
		if (volumes == null) {
			return Collections.emptyMap();
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

	// @Override
	@Override
	public Map<String, String> labels() {
		if (this.labels == null) {
			return Collections.emptyMap();
		}
		return this.labels;
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
		private String rawcmd;
		private List<String> cmd;
		private String image;
		@SuppressWarnings("rawtypes")
		private Map<String, Map> volumes;
		private String workingDir;
		private List<String> entrypoint;
		private Boolean networkDisabled;
		private List<String> onBuild;
		private Map<String, String> labels;

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
			this.rawcmd = cmd;
			this.cmd = getCmdList(cmd);
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

		public Builder volumes(
				@SuppressWarnings("rawtypes") final Map<String, Map> volumes) {
			this.volumes = volumes;
			return this;
		}

		@SuppressWarnings("rawtypes")
		public Map<String, Map> volumes() {
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

		public Builder labels(final Map<String, String> labels) {
			this.labels = labels;
			return this;
		}

		public DockerContainerConfig build() {
			return new DockerContainerConfig(this);
		}

		/**
		 * Create a proper command list after handling quotation.
		 *
		 * @param command
		 *            the command as a single {@link String}
		 * @return the command splitted in a list of ars or <code>null</code> if
		 *         the input <code>command</code> was <code>null</code>.
		 */
		private List<String> getCmdList(final String command) {
			if (command == null) {
				return null;
			}
			final List<String> list = new ArrayList<>();
			int length = command.length();
			boolean insideQuote1 = false; // single-quote
			boolean insideQuote2 = false; // double-quote
			boolean escaped = false;
			StringBuilder buffer = new StringBuilder();
			// Parse the string and break it up into chunks that are
			// separated by white-space or are quoted. Ignore characters
			// that have been escaped, including the escape character.
			for (int i = 0; i < length; ++i) {
				char c = command.charAt(i);
				if (escaped) {
					buffer.append(c);
					escaped = false;
				}
				switch (c) {
				case '\'':
					if (!insideQuote2)
						insideQuote1 = insideQuote1 ^ true;
					else
						buffer.append(c);
					break;
				case '\"':
					if (!insideQuote1)
						insideQuote2 = insideQuote2 ^ true;
					else
						buffer.append(c);
					break;
				case '\\':
					escaped = true;
					break;
				case ' ':
				case '\t':
				case '\r':
				case '\n':
					if (insideQuote1 || insideQuote2)
						buffer.append(c);
					else {
						String item = buffer.toString();
						buffer.setLength(0);
						if (item.length() > 0)
							list.add(item);
					}
					break;
				default:
					buffer.append(c);
					break;
				}
			}
			// add last item of string that will be in the buffer
			String item = buffer.toString();
			if (item.length() > 0)
				list.add(item);
			return list;
		}

	}
}
