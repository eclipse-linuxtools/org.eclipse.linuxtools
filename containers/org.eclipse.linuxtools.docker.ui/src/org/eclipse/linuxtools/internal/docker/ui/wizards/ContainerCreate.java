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
package org.eclipse.linuxtools.internal.docker.ui.wizards;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerHostConfig;
import org.eclipse.linuxtools.docker.core.IDockerPortBinding;
import org.eclipse.linuxtools.internal.docker.core.DockerContainerConfig;
import org.eclipse.linuxtools.internal.docker.core.DockerHostConfig;

public class ContainerCreate extends Wizard {

	private IDockerConnection connection;
	private IDockerContainerConfig config;
	private IDockerHostConfig hostConfig;
	private String image;
	private ContainerCreatePage mainPage;

	public ContainerCreate(IDockerConnection connection, String image) {
		this.connection = connection;
		this.image = image;
	}

	public IDockerContainerConfig getConfig() {
		return config;
	}

	public IDockerHostConfig getHostConfig() {
		return hostConfig;
	}

	public String getImageId() {
		return image;
	}

	@Override
	public void addPages() {
		// TODO Auto-generated method stub
		mainPage = new ContainerCreatePage(connection, image);
		addPage(mainPage);
	}

	@Override
	public boolean canFinish() {
		return mainPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		final String hostName = mainPage.getHostName();
		final String domainName = mainPage.getDomainName();
		final String user = mainPage.getUser();
		final Long memory = mainPage.getMemory();
		final Long memorySwap = mainPage.getMemorySwap();
		final Long cpuShares = mainPage.getCpuShares();
		final String cpuSet = mainPage.getCpuSet();
		final Boolean attachStdin = mainPage.getAttachStdin();
		final Boolean attachStdout = mainPage.getAttachStdout();
		final Boolean attachStderr = mainPage.getAttachStderr();
		final List<String> portSpecs = mainPage.getPortSpecs();
		final Set<String> exposedPorts = mainPage.getExposedPorts();
		final Boolean tty = mainPage.getTty();
		final Boolean openStdin = mainPage.getOpenStdin();
		final Boolean stdinOnce = mainPage.getStdinOnce();
		final List<String> env = mainPage.getEnv();
		final List<String> cmd = mainPage.getCmd();
		final Set<String> volumes = mainPage.getVolumes();
		final String workingDir = mainPage.getWorkingDir();
		final List<String> entryPoint = mainPage.getEntryPoint();
		final Boolean networkDisabled = mainPage.getNetworkDisabled();
		final List<String> onBuild = mainPage.getOnBuild();

		image = mainPage.getImageId();

		DockerContainerConfig.Builder builder = new DockerContainerConfig.Builder()
				.hostname(hostName).domainname(domainName).user(user)
				.memory(memory).memorySwap(memorySwap).cpuShares(cpuShares)
				.cpuset(cpuSet).attachStdin(attachStdin)
				.attachStdout(attachStdout).attachStderr(attachStderr).tty(tty)
				.openStdin(openStdin).stdinOnce(stdinOnce).cmd(cmd)
				.image(image).workingDir(workingDir)
				.networkDisabled(networkDisabled);
		if (portSpecs != null)
			builder = builder.portSpecs(portSpecs);
		if (exposedPorts != null)
			builder = builder.exposedPorts(exposedPorts);
		if (onBuild != null)
			builder = builder.onBuild(onBuild);
		if (entryPoint != null)
			builder = builder.entryPoint(entryPoint);
		if (env != null)
			builder = builder.env(env);
		if (volumes != null)
			builder = builder.volumes(volumes);

		config = builder.build();

		final List<String> hostVolumes = mainPage.getHostVolumes();
		final String networkMode = mainPage.getNetworkMode();
		final Boolean privileged = mainPage.getPrivileged();
		final Boolean publishAllPorts = mainPage.getPublishAllPorts();
		final Map<String, List<IDockerPortBinding>> portBindings = mainPage
				.getPortBindings();

		final DockerHostConfig.Builder builder2 = DockerHostConfig.builder()
				.networkMode(networkMode).privileged(privileged)
				.publishAllPorts(publishAllPorts);

		if (hostVolumes != null && hostVolumes.size() > 0) {
			builder2.binds(hostVolumes);
		}

		if (portBindings != null && portBindings.size() > 0) {
			builder2.portBindings(portBindings);
		}
		hostConfig = builder2.build();
		return true;
	}
}
