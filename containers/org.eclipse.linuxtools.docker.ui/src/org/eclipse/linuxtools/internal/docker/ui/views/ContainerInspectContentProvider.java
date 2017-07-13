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

package org.eclipse.linuxtools.internal.docker.ui.views;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerContainerInfo;
import org.eclipse.linuxtools.docker.core.IDockerContainerState;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerNetworkSettings;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerHostConfig;

/**
 * @author xcoulon
 *
 */
public class ContainerInspectContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY = new Object[0];

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if(inputElement instanceof IDockerContainerInfo) {
			final IDockerContainerInfo info = (IDockerContainerInfo) inputElement;
			return new Object[] {
					new Object[]{"Id", info.id().substring(0,  12)}, //$NON-NLS-1$
					new Object[]{"Name", info.name()}, //$NON-NLS-1$
					new Object[]{"Created", LabelProviderUtils.toCreatedDate(info.created())}, //$NON-NLS-1$
					new Object[]{"State", info.state()}, //$NON-NLS-1$
					new Object[]{"Args", LabelProviderUtils.reduce(info.args())}, //$NON-NLS-1$
					new Object[]{"Driver", info.driver()}, //$NON-NLS-1$
					new Object[]{"ExecDriver", info.execDriver()}, //$NON-NLS-1$
					new Object[] { "Config", info.config() }, //$NON-NLS-1$
					new Object[]{"HostConfig", info.hostConfig()}, //$NON-NLS-1$
					new Object[]{"HostnamePath", info.hostnamePath()}, //$NON-NLS-1$
					new Object[]{"HostsPath", info.hostsPath()}, //$NON-NLS-1$
					new Object[]{"Image", info.image()}, //$NON-NLS-1$
					new Object[]{"MountLabel", info.mountLabel()}, //$NON-NLS-1$
					new Object[]{"NetworkSettings", info.networkSettings()}, //$NON-NLS-1$
					new Object[]{"Path", info.path()}, //$NON-NLS-1$
					new Object[]{"ProcessLabel", info.processLabel()}, //$NON-NLS-1$
					new Object[]{"ResolvConfPath", info.resolvConfPath()}, //$NON-NLS-1$
					new Object[]{"Volumes", info.volumes()}, //$NON-NLS-1$
					new Object[]{"VolumesRW", info.volumesRW()}, //$NON-NLS-1$
			};
		}return EMPTY;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		final Object propertyValue = ((Object[])parentElement)[1];
		if(propertyValue instanceof IDockerContainerState) {
			final IDockerContainerState containerState = (IDockerContainerState) propertyValue;
			return new Object[] {
					new Object[]{"ExitCode", containerState.exitCode()}, //$NON-NLS-1$
					new Object[]{"Finished at", LabelProviderUtils.toFinishedDate(containerState.finishDate())}, //$NON-NLS-1$
					new Object[]{"Running", containerState.running()}, //$NON-NLS-1$
					new Object[]{"Paused", containerState.paused()}, //$NON-NLS-1$
					new Object[]{"Pid", containerState.pid()}, //$NON-NLS-1$
			};		
		} else if(propertyValue instanceof IDockerHostConfig) {
			final DockerHostConfig hostConfig = (DockerHostConfig) propertyValue;
			return new Object[] {
					new Object[]{"Binds", LabelProviderUtils.reduce(hostConfig.binds())}, //$NON-NLS-1$
					new Object[]{"ContainerIDFile", hostConfig.containerIDFile()}, //$NON-NLS-1$
					new Object[]{"Dns", LabelProviderUtils.reduce(hostConfig.dns())}, //$NON-NLS-1$
					new Object[]{"DnsSearch", LabelProviderUtils.reduce(hostConfig.dnsSearch())}, //$NON-NLS-1$
					new Object[]{"Links", splitLinks(hostConfig.links())}, //$NON-NLS-1$
					new Object[]{"LxcConf", hostConfig.lxcConf()}, //$NON-NLS-1$
					new Object[]{"NetworkMode", hostConfig.networkMode()}, //$NON-NLS-1$
					new Object[]{"PortBindings", LabelProviderUtils.reduce(hostConfig.portBindings())}, //$NON-NLS-1$
					new Object[]{"Privileged", hostConfig.privileged()}, //$NON-NLS-1$
					new Object[]{"PublishAllPorts", hostConfig.publishAllPorts()}, //$NON-NLS-1$
					new Object[] { "ReadonlyRootfs", //$NON-NLS-1$
							hostConfig.readonlyRootfs() },
					new Object[] { "SecurityOpt", hostConfig.securityOpt() }, //$NON-NLS-1$
					new Object[] { "Tmpfs", hostConfig.tmpfs() }, //$NON-NLS-1$
					new Object[]{"VolumesFrom", LabelProviderUtils.reduce(hostConfig.volumesFrom())}, //$NON-NLS-1$
			};
		} else if(propertyValue instanceof IDockerContainerConfig) {
			final IDockerContainerConfig config = (IDockerContainerConfig) propertyValue;
			return new Object[] {
					new Object[]{"AttachStderr", config.attachStderr()}, //$NON-NLS-1$
					new Object[]{"AttachStdin", config.attachStdin()}, //$NON-NLS-1$
					new Object[]{"AttachStdout", config.attachStdout()}, //$NON-NLS-1$
					new Object[]{"Cmd", LabelProviderUtils.reduce(config.cmd())}, //$NON-NLS-1$
					new Object[]{"CpuSet", config.cpuset()}, //$NON-NLS-1$
					new Object[]{"CpuShares", config.cpuShares()}, //$NON-NLS-1$
					new Object[]{"Domainname", config.domainname()}, //$NON-NLS-1$
					new Object[]{"Entrypoint", LabelProviderUtils.reduce(config.entrypoint())}, //$NON-NLS-1$
					new Object[]{"Env", LabelProviderUtils.reduce(config.env())}, //$NON-NLS-1$
					new Object[]{"ExposedPorts", LabelProviderUtils.reduce(config.exposedPorts())}, //$NON-NLS-1$
					new Object[]{"Hostname", config.hostname()}, //$NON-NLS-1$
					new Object[]{"Image", config.image()}, //$NON-NLS-1$
					new Object[] { "Labels", //$NON-NLS-1$
							((DockerContainerConfig) config).labels() },
					new Object[]{"Memory", config.memory()}, //$NON-NLS-1$
					new Object[]{"MemorySwap", config.memorySwap()}, //$NON-NLS-1$
					new Object[]{"NetworkDisabled", config.networkDisabled()}, //$NON-NLS-1$
					new Object[]{"OnBuild", config.onBuild()}, //$NON-NLS-1$
					new Object[]{"OpenStdin", config.openStdin()}, //$NON-NLS-1$
					new Object[]{"PortSpecs", LabelProviderUtils.reduce(config.portSpecs())}, //$NON-NLS-1$
					new Object[]{"StdinOnce", config.stdinOnce()}, //$NON-NLS-1$
					new Object[]{"Tty", config.tty()}, //$NON-NLS-1$
					new Object[] { "Volumes", //$NON-NLS-1$
							LabelProviderUtils.reduce(config.volumes()) },
					new Object[]{"WorkingDir", config.workingDir()}, //$NON-NLS-1$
			};
		} else if(propertyValue instanceof IDockerPortBinding) {
			final IDockerPortBinding portBinding = (IDockerPortBinding) propertyValue;
			return new Object[] {
					new Object[]{"Host IP/Port", LabelProviderUtils.toString(portBinding)} //$NON-NLS-1$
			};
		} else if(propertyValue instanceof IDockerNetworkSettings) {
			final IDockerNetworkSettings networkSettings = (IDockerNetworkSettings) propertyValue;
			return new Object[] {
					new Object[]{"Bridge", networkSettings.bridge()}, //$NON-NLS-1$
					new Object[]{"Gateway", networkSettings.gateway()}, //$NON-NLS-1$
					new Object[]{"IPAddress", networkSettings.ipAddress()}, //$NON-NLS-1$
					new Object[]{"IPPrefixLen", networkSettings.ipPrefixLen()}, //$NON-NLS-1$
					new Object[]{"PortMapping", networkSettings.portMapping()}, //$NON-NLS-1$
					new Object[]{"Ports", LabelProviderUtils.reduce(networkSettings.ports())}, //$NON-NLS-1$
			};
		} else if(propertyValue instanceof List<?>) {
			@SuppressWarnings("unchecked")
			final List<Object> propertyValues = (List<Object>)propertyValue;
			final Object[] result = new Object[propertyValues.size()];
			for (int i = 0; i < propertyValues.size(); i++) {
				result[i] = new Object[]{"", LabelProviderUtils.toString(propertyValues.get(i))}; //$NON-NLS-1$
			}
			return result;
		} else if (propertyValue instanceof Set<?>) {
			@SuppressWarnings("unchecked")
			final Set<Object> propertyValues = (Set<Object>) propertyValue;
			final Object[] result = new Object[propertyValues.size()];
			Iterator<Object> iterator = propertyValues.iterator();
			for (int i = 0; i < propertyValues.size(); i++) {
				result[i] = new Object[] { "", //$NON-NLS-1$
						LabelProviderUtils.toString(iterator.next()) };
			}
			return result;
		} else if(propertyValue instanceof Map<?,?>) {
			final Map<?,?> propertyValues = (Map<?,?>)propertyValue;
			final Object[] result = new Object[propertyValues.size()];
			int i = 0;
			for (Entry<?, ?> entry : propertyValues.entrySet()) {
				result[i] = new Object[]{entry.getKey(), entry.getValue()};
				i++;
			}
			return result;
		} 
		return EMPTY;
	}

	private Object splitLinks(final List<String> links) {
		if(links == null || links.isEmpty()) {
			return ""; //$NON-NLS-1$
		} else if(links.size() == 1) {
			return links.get(0);
		} else {
			final Object[] result = new Object[links.size()];
			for(int i = 0; i < links.size(); i++) {
				final String[] split = links.get(i).split(":");
				result[i] = new Object[]{split[0], split[1]};
			}
			return links;
		}
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if(element instanceof Object[]) {
			final Object value = ((Object[])element)[1];
			return (value instanceof List || value instanceof Map
					|| value instanceof IDockerContainerState
					|| value instanceof IDockerNetworkSettings
					|| value instanceof IDockerHostConfig
					|| value instanceof IDockerPortBinding
					|| value instanceof IDockerContainerConfig);
		}
		return false;
	}
	
}
