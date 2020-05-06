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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.linuxtools.docker.core.IDockerBlkioDeviceRate;
import org.eclipse.linuxtools.docker.core.IDockerBlkioWeightDevice;
import org.eclipse.linuxtools.docker.core.IDockerConfParameter;
import org.eclipse.linuxtools.docker.core.IDockerDevice;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerLogConfig;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.docker.core.IDockerRestartPolicy;
import org.eclipse.linuxtools.docker.core.IDockerUlimit;

import org.mandas.docker.client.messages.Device;
import org.mandas.docker.client.messages.HostConfig;
import org.mandas.docker.client.messages.HostConfig.BlkioDeviceRate;
import org.mandas.docker.client.messages.HostConfig.BlkioWeightDevice;
import org.mandas.docker.client.messages.HostConfig.LxcConfParameter;
import org.mandas.docker.client.messages.HostConfig.Ulimit;
import org.mandas.docker.client.messages.PortBinding;

public class DockerHostConfig implements IDockerHostConfig {

	private final List<String> binds;
	private final String containerIDFile;
	private final List<IDockerConfParameter> lxcConf;
	private final boolean privileged;
	private final boolean readonlyRootfs;
	private final Map<String, String> tmpfs;
	private final Map<String, List<IDockerPortBinding>> portBindings;
	private final List<String> links;
	private final boolean publishAllPorts;
	private final List<String> dns;
	private final List<String> dnsSearch;
	private final List<String> dnsOptions;
	private final List<String> extraHosts;
	private final List<IDockerDevice> devices;
	private final List<String> volumesFrom;
	private final List<String> securityOpt;
	private final List<String> capAdd;
	private final List<String> capDrop;
	private final String networkMode;
	private final String cgroupParent;
	private final Long memory;
	private final Long cpuShares;
	private final Long memorySwap;
	private final Integer memorySwappiness;
	private final Long memoryReservation;
	private final Long nanoCpus;
	private final Long cpuPeriod;
	private final String cpusetCpus;
	private final String cpusetMems;
	private final Long cpuQuota;
	private final String ipcMode;
	private final String pidMode;
	private final Long shmSize;
	private final boolean oomKillDisable;
	private final Integer oomScoreAdj;
	private final boolean autoRemove;
	private final Integer pidsLimit;
	private final IDockerRestartPolicy restartPolicy;
	private final List<IDockerUlimit> ulimits;
	private final IDockerLogConfig logConfig;
	private final Map<String, String> storageOpt;
	private final Integer blkioWeight;
	private final List<IDockerBlkioWeightDevice> blkioWeightDevice;
	private final List<IDockerBlkioDeviceRate> blkioDeviceReadBps;
	private final List<IDockerBlkioDeviceRate> blkioDeviceWriteBps;
	private final List<IDockerBlkioDeviceRate> blkioDeviceReadIOps;
	private final List<IDockerBlkioDeviceRate> blkioDeviceWriteIOps;

	public DockerHostConfig(final HostConfig hostConfig) {
		this.binds = hostConfig.binds();
		this.containerIDFile = hostConfig.containerIdFile();
		this.lxcConf = new ArrayList<>();
		if(hostConfig.lxcConf() != null) {
			for (LxcConfParameter lxcConfParameter : hostConfig.lxcConf()) {
				this.lxcConf.add(new DockerConfParameter(lxcConfParameter));
			}
		}
		this.privileged = hostConfig.privileged() != null
				? hostConfig.privileged() : false;
		this.readonlyRootfs = hostConfig.readonlyRootfs() != null
				? hostConfig.readonlyRootfs()
				: false;
		this.tmpfs = hostConfig.tmpfs();
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
		this.memory = hostConfig.memory();
		this.cpuShares = hostConfig.cpuShares();
		this.securityOpt = hostConfig.securityOpt();
		this.capAdd = hostConfig.capAdd();
		this.capDrop = hostConfig.capDrop();
		this.dnsOptions = hostConfig.dnsOptions();
		this.extraHosts = hostConfig.extraHosts();
		this.devices = new ArrayList<>();
		if (hostConfig != null && hostConfig.devices() != null) {
			for (Device device : hostConfig.devices()) {
				this.devices.add(new DockerDevice(device));
			}
		}
		this.cgroupParent = hostConfig.cgroupParent();
		this.memorySwap = hostConfig.memorySwap();
		this.memorySwappiness = hostConfig.memorySwappiness();
		this.memoryReservation = hostConfig.memoryReservation();
		this.nanoCpus = hostConfig.nanoCpus();
		this.cpuPeriod = hostConfig.cpuPeriod();
		this.cpusetCpus = hostConfig.cpusetCpus();
		this.cpusetMems = hostConfig.cpusetMems();
		this.cpuQuota = hostConfig.cpuQuota();
		this.ipcMode = hostConfig.ipcMode();
		this.pidMode = hostConfig.pidMode();
		this.shmSize = hostConfig.shmSize();
		this.oomKillDisable = hostConfig.oomKillDisable() != null
				? hostConfig.oomKillDisable()
				: false;
		this.oomScoreAdj = hostConfig.oomScoreAdj();
		this.autoRemove = hostConfig.autoRemove() != null
				? hostConfig.autoRemove()
				: false;
		this.pidsLimit = hostConfig.pidsLimit();
		this.restartPolicy = hostConfig.restartPolicy() != null
				? new DockerRestartPolicy(hostConfig.restartPolicy())
				: null;
		this.ulimits = new ArrayList<>();
		if (hostConfig.ulimits() != null) {
			for (Ulimit ulimit : hostConfig.ulimits()) {
				this.ulimits.add(new DockerUlimit(ulimit));
			}
		}
		this.logConfig = hostConfig.logConfig() != null
				? new DockerLogConfig(hostConfig.logConfig())
				: null;
		this.storageOpt = hostConfig.storageOpt();
		this.blkioWeight = hostConfig.blkioWeight();
		this.blkioWeightDevice = new ArrayList<>();
		if (hostConfig.blkioWeightDevice() != null) {
			for (BlkioWeightDevice device : hostConfig.blkioWeightDevice()) {
				blkioWeightDevice.add(new DockerBlkioWeightDevice(device));
			}
		}
		this.blkioDeviceReadBps = new ArrayList<>();
		if (hostConfig.blkioDeviceReadBps() != null) {
			for (BlkioDeviceRate rate : hostConfig.blkioDeviceReadBps()) {
				blkioDeviceReadBps.add(new DockerBlkioDeviceRate(rate));
			}
		}
		this.blkioDeviceWriteBps = new ArrayList<>();
		if (hostConfig.blkioDeviceReadBps() != null) {
			for (BlkioDeviceRate rate : hostConfig.blkioDeviceWriteBps()) {
				blkioDeviceWriteBps.add(new DockerBlkioDeviceRate(rate));
			}
		}
		this.blkioDeviceReadIOps = new ArrayList<>();
		if (hostConfig.blkioDeviceReadBps() != null) {
			for (BlkioDeviceRate rate : hostConfig.blkioDeviceReadIOps()) {
				blkioDeviceReadIOps.add(new DockerBlkioDeviceRate(rate));
			}
		}
		this.blkioDeviceWriteIOps = new ArrayList<>();
		if (hostConfig.blkioDeviceReadBps() != null) {
			for (BlkioDeviceRate rate : hostConfig.blkioDeviceWriteIOps()) {
				blkioDeviceWriteIOps.add(new DockerBlkioDeviceRate(rate));
			}
		}
	}

	private DockerHostConfig(final Builder builder) {
		this.binds = builder.binds;
		this.containerIDFile = builder.containerIDFile;
		this.lxcConf = builder.lxcConf;
		this.privileged = builder.privileged != null ? builder.privileged
				: false;
		this.readonlyRootfs = builder.readonlyRootfs != null
				? builder.readonlyRootfs
				: false;
		this.tmpfs = builder.tmpfs;
		this.portBindings = builder.portBindings;
		this.links = builder.links;
		this.publishAllPorts = builder.publishAllPorts != null
				? builder.publishAllPorts : false;
		this.dns = builder.dns;
		this.dnsSearch = builder.dnsSearch;
		this.volumesFrom = builder.volumesFrom;
		this.networkMode = builder.networkMode;
		this.memory = builder.memory;
		this.cpuShares = builder.cpuShares;
		this.securityOpt = builder.securityOpt;
		this.capAdd = builder.capAdd;
		this.capDrop = builder.capDrop;
		this.dnsOptions = builder.dnsOptions;
		this.extraHosts = builder.extraHosts;
		this.devices = builder.devices;
		this.cgroupParent = builder.cgroupParent;
		this.memorySwap = builder.memorySwap;
		this.memorySwappiness = builder.memorySwappiness;
		this.memoryReservation = builder.memoryReservation;
		this.nanoCpus = builder.nanoCpus;
		this.cpuPeriod = builder.cpuPeriod;
		this.cpusetCpus = builder.cpusetCpus;
		this.cpusetMems = builder.cpusetMems;
		this.cpuQuota = builder.cpuQuota;
		this.ipcMode = builder.ipcMode;
		this.pidMode = builder.pidMode;
		this.shmSize = builder.shmSize;
		this.oomKillDisable = builder.oomKillDisable != null
				? builder.oomKillDisable
				: false;
		this.oomScoreAdj = builder.oomScoreAdj;
		this.autoRemove = builder.autoRemove != null ? builder.autoRemove
				: false;
		this.pidsLimit = builder.pidsLimit;
		this.restartPolicy = builder.restartPolicy;
		this.ulimits = builder.ulimits;
		this.logConfig = builder.logConfig;
		this.storageOpt = builder.storageOpt;
		this.blkioWeight = builder.blkioWeight;
		this.blkioWeightDevice = builder.blkioWeightDevice;
		this.blkioDeviceReadBps = builder.blkioDeviceReadBps;
		this.blkioDeviceWriteBps = builder.blkioDeviceWriteBps;
		this.blkioDeviceReadIOps = builder.blkioDeviceReadIOps;
		this.blkioDeviceWriteIOps = builder.blkioDeviceWriteIOps;
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

	public boolean readonlyRootfs() {
		return readonlyRootfs;
	}

	public Map<String, String> tmpfs() {
		return tmpfs;
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

	public List<String> dnsOptions() {
		return dnsOptions;
	}

	public List<IDockerDevice> devices() {
		return devices;
	}

	@Override
	public List<String> volumesFrom() {
		return volumesFrom;
	}

	@Override
	public List<String> securityOpt() {
		return securityOpt;
	}

	public List<String> capAdd() {
		return capAdd;
	}

	public List<String> capDrop() {
		return capDrop;
	}

	public List<String> extraHosts() {
		return extraHosts;
	}

	public String cgroupParent() {
		return cgroupParent;
	}

	@Override
	public String networkMode() {
		return networkMode;
	}

	public Long memory() {
		return memory;
	}

	public Long cpuShares() {
		return cpuShares;
	}

	public Long memorySwap() {
		return memorySwap;
	}

	public Integer memorySwappiness() {
		return memorySwappiness;
	}

	public Long memoryReservation() {
		return memoryReservation;
	}

	public Long nanoCpus() {
		return nanoCpus;
	}

	public Long cpuPeriod() {
		return cpuPeriod;
	}

	public String cpusetCpus() {
		return cpusetCpus;
	}

	public String cpusetMems() {
		return cpusetMems;
	}

	public Long cpuQuota() {
		return cpuQuota;
	}

	public String ipcMode() {
		return ipcMode;
	}

	public String pidMode() {
		return pidMode;
	}

	public Long shmSize() {
		return shmSize;
	}

	public boolean oomKillDisable() {
		return oomKillDisable;
	}

	public Integer oomScoreAdj() {
		return oomScoreAdj;
	}

	public boolean autoRemove() {
		return autoRemove;
	}

	public Integer pidsLimit() {
		return pidsLimit;
	}

	public IDockerRestartPolicy restartPolicy() {
		return restartPolicy;
	}

	public List<IDockerUlimit> ulimits() {
		return ulimits;
	}

	public IDockerLogConfig logConfig() {
		return logConfig;
	}

	public Map<String, String> storageOpt() {
		return storageOpt;
	}

	public Integer blkioWeight() {
		return blkioWeight;
	}

	public List<IDockerBlkioWeightDevice> blkioWeightDevice() {
		return blkioWeightDevice;
	}

	public List<IDockerBlkioDeviceRate> blkioDeviceReadBps() {
		return blkioDeviceReadBps;
	}

	public List<IDockerBlkioDeviceRate> blkioDeviceWriteBps() {
		return blkioDeviceWriteBps;
	}

	public List<IDockerBlkioDeviceRate> blkioDeviceReadIOps() {
		return blkioDeviceReadIOps;
	}

	public List<IDockerBlkioDeviceRate> blkioDeviceWriteIOps() {
		return blkioDeviceWriteIOps;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private List<String> binds;
		private String containerIDFile;
		private List<IDockerConfParameter> lxcConf;
		private Boolean privileged;
		private Boolean readonlyRootfs;
		private Map<String, String> tmpfs;
		private Map<String, List<IDockerPortBinding>> portBindings;
		private List<String> links;
		private Boolean publishAllPorts;
		private List<String> dns = new ArrayList<>();
		private List<String> dnsSearch = new ArrayList<>();
		private List<String> volumesFrom;
		private List<String> securityOpt;
		private List<String> capAdd;
		private List<String> capDrop;
		private List<String> dnsOptions = new ArrayList<>();
		private List<String> extraHosts;
		private List<IDockerDevice> devices = new ArrayList<>();
		private String cgroupParent = "";
		private String networkMode;
		private Long memory;
		private Long cpuShares;
		private Long memorySwap;
		private Integer memorySwappiness;
		private Long memoryReservation;
		private Long nanoCpus;
		private Long cpuPeriod;
		private String cpusetCpus = "";
		private String cpusetMems = "";
		private Long cpuQuota;
		private String ipcMode = "";
		private String pidMode = "";
		private Long shmSize;
		private Boolean oomKillDisable;
		private Integer oomScoreAdj;
		private Boolean autoRemove;
		private Integer pidsLimit;
		private IDockerRestartPolicy restartPolicy;
		private List<IDockerUlimit> ulimits;
		private IDockerLogConfig logConfig;
		private Map<String, String> storageOpt;
		private Integer blkioWeight;
		private List<IDockerBlkioWeightDevice> blkioWeightDevice;
		private List<IDockerBlkioDeviceRate> blkioDeviceReadBps;
		private List<IDockerBlkioDeviceRate> blkioDeviceWriteBps;
		private List<IDockerBlkioDeviceRate> blkioDeviceReadIOps;
		private List<IDockerBlkioDeviceRate> blkioDeviceWriteIOps;

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

		public Builder lxcConf(final List<IDockerConfParameter> lxcConf) {
			this.lxcConf = new ArrayList<>(lxcConf);
			return this;
		}

		public Builder lxcConf(final IDockerConfParameter... lxcConf) {
			this.lxcConf = Arrays.asList(lxcConf);
			return this;
		}

		public Builder privileged(final Boolean privileged) {
			this.privileged = privileged;
			return this;
		}

		public Builder readonlyRootfs(final Boolean readonlyRootfs) {
			this.readonlyRootfs = readonlyRootfs;
			return this;
		}

		public Builder tmpfs(Map<String, String> tmpfs) {
			this.tmpfs = tmpfs;
			return this;
		}

		public Map<String, String> tmpfs() {
			return tmpfs;
		}

		public Builder portBindings(
				final Map<String, List<IDockerPortBinding>> portBindings) {
			this.portBindings = portBindings;
			return this;
		}

		public Builder links(final List<String> links) {
			this.links = new ArrayList<>(links);
			return this;
		}

		public Builder links(final String... links) {
			this.links = Arrays.asList(links);
			return this;
		}

		public Builder publishAllPorts(final Boolean publishAllPorts) {
			this.publishAllPorts = publishAllPorts;
			return this;
		}

		public Builder volumesFrom(final List<String> volumesFrom) {
			this.volumesFrom = new ArrayList<>(volumesFrom);
			return this;
		}

		public Builder volumesFrom(final String... volumesFrom) {
			this.volumesFrom = Arrays.asList(volumesFrom);
			return this;
		}

		public Builder securityOpt(final List<String> securityOpt) {
			this.securityOpt = new ArrayList<>(securityOpt);
			return this;
		}

		public Builder securityOpt(final String... securityOpt) {
			this.securityOpt = Arrays.asList(securityOpt);
			return this;
		}

		public Builder capAdd(final List<String> capAdd) {
			this.capAdd = new ArrayList<>(capAdd);
			return this;
		}

		public Builder capAdd(final String... capAdd) {
			this.capAdd = Arrays.asList(capAdd);
			return this;
		}

		public Builder capDrop(final List<String> capDrop) {
			this.capDrop = new ArrayList<>(capDrop);
			return this;
		}

		public Builder capDrop(final String... capDrop) {
			this.capDrop = Arrays.asList(capDrop);
			return this;
		}

		public Builder dns(final List<String> dns) {
			this.dns = new ArrayList<>(dns);
			return this;
		}

		public Builder dns(final String... dns) {
			this.dns = Arrays.asList(dns);
			return this;
		}

		public Builder dnsOptions(final List<String> dnsOptions) {
			this.dnsOptions = new ArrayList<>(dnsOptions);
			return this;
		}

		public Builder dnsOptions(final String... dnsOptions) {
			this.dnsOptions = Arrays.asList(dnsOptions);
			return this;
		}

		public Builder dnsSearch(final List<String> dnsSearch) {
			this.dnsSearch = new ArrayList<>(dnsSearch);
			return this;
		}

		public Builder dnsSearch(final String... dnsSearch) {
			this.dnsSearch = Arrays.asList(dnsSearch);
			return this;
		}

		public Builder extraHosts(final List<String> extraHosts) {
			this.extraHosts = new ArrayList<>(extraHosts);
			return this;
		}

		public Builder extraHosts(final String... extraHosts) {
			this.extraHosts = Arrays.asList(extraHosts);
			return this;
		}

		public Builder devices(final List<IDockerDevice> devices) {
			this.devices = new ArrayList<>(devices);
			return this;
		}

		public Builder cgroupParent(final String cgroupParent) {
			this.cgroupParent = cgroupParent;
			return this;
		}

		public Builder networkMode(final String networkMode) {
			this.networkMode = networkMode;
			return this;
		}

		public Builder memory(final Long memory) {
			this.memory = memory;
			return this;
		}
		
		public Builder cpuShares(final Long cpuShares) {
			this.cpuShares = cpuShares;
			return this;
		}

		public Builder memorySwap(final Long memorySwap) {
			this.memorySwap = memorySwap;
			return this;
		}

		public Builder memorySwappiness(final Integer memorySwappiness) {
			this.memorySwappiness = memorySwappiness;
			return this;
		}

		public Builder memoryReservation(final Long memoryReservation) {
			this.memoryReservation = memoryReservation;
			return this;
		}

		public Builder nanoCpus(final Long nanoCpus) {
			this.nanoCpus = nanoCpus;
			return this;
		}

		public Builder cpuPeriod(final Long cpuPeriod) {
			this.cpuPeriod = cpuPeriod;
			return this;
		}

		public Builder cpusetCpus(final String cpusetCpus) {
			this.cpusetCpus = cpusetCpus;
			return this;
		}

		public Builder cpusetMems(final String cpusetMems) {
			this.cpusetMems = cpusetMems;
			return this;
		}

		public Builder cpuQuota(final Long cpuQuota) {
			this.cpuQuota = cpuQuota;
			return this;
		}

		public Builder ipcMode(final String ipcMode) {
			this.ipcMode = ipcMode;
			return this;
		}

		public Builder pidMode(final String pidMode) {
			this.pidMode = pidMode;
			return this;
		}

		public Builder shmSize(final Long shmSize) {
			this.shmSize = shmSize;
			return this;
		}

		public Builder oomKillDisable(final Boolean oomKillDisable) {
			this.oomKillDisable = oomKillDisable;
			return this;
		}

		public Builder oomScoreAdj(final Integer oomScoreAdj) {
			this.oomScoreAdj = oomScoreAdj;
			return this;
		}

		public Builder autoRemove(final Boolean autoRemove) {
			this.autoRemove = autoRemove;
			return this;
		}

		public Builder pidsLimit(final Integer pidsLimit) {
			this.pidsLimit = pidsLimit;
			return this;
		}

		public Builder restartPolicy(final IDockerRestartPolicy restartPolicy) {
			this.restartPolicy = restartPolicy;
			return this;
		}

		public Builder ulimits(final List<IDockerUlimit> ulimits) {
			this.ulimits = ulimits;
			return this;
		}

		public Builder logConfig(final IDockerLogConfig logConfig) {
			this.logConfig = logConfig;
			return this;
		}

		public Builder storageOpt(final Map<String, String> storageOpt) {
			this.storageOpt = storageOpt;
			return this;
		}

		public Builder blkioWeight(final Integer blkioWeight) {
			this.blkioWeight = blkioWeight;
			return this;
		}

		public Builder blkioWeightDevice(
				final List<IDockerBlkioWeightDevice> blkioWeightDevice) {
			this.blkioWeightDevice = blkioWeightDevice;
			return this;
		}

		public Builder blkioDeviceReadBps(
				final List<IDockerBlkioDeviceRate> blkioDeviceReadBps) {
			this.blkioDeviceReadBps = blkioDeviceReadBps;
			return this;
		}

		public Builder blkioDeviceWriteBps(
				final List<IDockerBlkioDeviceRate> blkioDeviceWriteBps) {
			this.blkioDeviceWriteBps = blkioDeviceWriteBps;
			return this;
		}

		public Builder blkioDeviceReadIOps(
				final List<IDockerBlkioDeviceRate> blkioDeviceReadIOps) {
			this.blkioDeviceReadIOps = blkioDeviceReadIOps;
			return this;
		}

		public Builder blkioDeviceWriteIOps(
				final List<IDockerBlkioDeviceRate> blkioDeviceWriteIOps) {
			this.blkioDeviceWriteIOps = blkioDeviceWriteIOps;
			return this;
		}

		public IDockerHostConfig build() {
			return new DockerHostConfig(this);
		}

	}
}
