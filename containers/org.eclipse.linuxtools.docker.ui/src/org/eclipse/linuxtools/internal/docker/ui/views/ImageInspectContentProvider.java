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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.IDockerContainerConfig;
import org.eclipse.linuxtools.docker.core.IDockerImageInfo;

/**
 * @author jjohnstn
 *
 */
public class ImageInspectContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY = new Object[0];

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof IDockerImageInfo) {
			final IDockerImageInfo info = (IDockerImageInfo) inputElement;
			return new Object[] {
					new Object[] { "Id", info.id().substring(0, 12) }, //$NON-NLS-1$
					new Object[] { "Parent", info.parent() }, //$NON-NLS-1$
					new Object[] { "Comment", info.comment() }, //$NON-NLS-1$
					new Object[] {
							"Created", LabelUtils.toCreatedDate(info.created()) }, //$NON-NLS-1$
					new Object[] { "Container", info.container() }, //$NON-NLS-1$
					new Object[] { "ContainerConfig", info.containerConfig() }, //$NON-NLS-1$
					new Object[] { "DockerVersion", info.dockerVersion() }, //$NON-NLS-1$
					new Object[] { "Author", info.author() }, //$NON-NLS-1$
					new Object[] { "Config", info.config() }, //$NON-NLS-1$
					new Object[] { "Architecture", info.architecture() }, //$NON-NLS-1$
					new Object[] { "Os", info.os() }, //$NON-NLS-1$
					new Object[] { "Size", LabelUtils.toString(info.size()) }, //$NON-NLS-1$
			};
		}
		return EMPTY;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		final Object propertyValue = ((Object[]) parentElement)[1];
		if (propertyValue instanceof IDockerContainerConfig) {
			final IDockerContainerConfig config = (IDockerContainerConfig) propertyValue;
			return new Object[] {
					new Object[] { "Hostname", config.hostname() }, //$NON-NLS-1$
					new Object[] { "Domainname", config.domainname() }, //$NON-NLS-1$
					new Object[] { "User", config.user() }, //$NON-NLS-1$
					new Object[] {
							"Memory", LabelUtils.toString(config.memory()) }, //$NON-NLS-1$
					new Object[] {
							"MemorySwap", LabelUtils.toString(config.memorySwap()) }, //$NON-NLS-1$
					new Object[] {
							"CpuShares", LabelUtils.toString(config.cpuShares()) }, //$NON-NLS-1$
					new Object[] { "Cpuset", config.cpuset() }, //$NON-NLS-1$
					new Object[] { "AttachStdin", config.attachStdin() }, //$NON-NLS-1$
					new Object[] { "AttachStdout", config.attachStdout() }, //$NON-NLS-1$
					new Object[] { "AttachStderr", config.attachStderr() }, //$NON-NLS-1$
					new Object[] {
							"PortSpecs", LabelUtils.reduce(config.portSpecs()) }, //$NON-NLS-1$
					new Object[] { "ExposedPorts", config.exposedPorts() }, //$NON-NLS-1$
					new Object[] { "Tty", config.tty() }, //$NON-NLS-1$
					new Object[] { "OpenStdin", config.openStdin() }, //$NON-NLS-1$
					new Object[] { "StdinOnce", config.stdinOnce() }, //$NON-NLS-1$
					new Object[] { "Env", LabelUtils.reduce(config.env()) }, //$NON-NLS-1$
					new Object[] { "Cmd", LabelUtils.reduce(config.cmd()) }, //$NON-NLS-1$
					new Object[] { "Image", config.image() }, //$NON-NLS-1$
					new Object[] {
							"Volumes", LabelUtils.reduce(config.volumes()) }, //$NON-NLS-1$
					new Object[] { "WorkingDir", config.workingDir() }, //$NON-NLS-1$
					new Object[] {
							"EntryPoint", LabelUtils.reduce(config.entrypoint()) }, //$NON-NLS-1$
					new Object[] { "NetworkDisabled", config.networkDisabled() }, //$NON-NLS-1$
					new Object[] {
							"OnBuild", LabelUtils.reduce(config.onBuild()) }, //$NON-NLS-1$
			};
		} else if (propertyValue instanceof List<?>) {
			@SuppressWarnings("unchecked")
			final List<Object> propertyValues = (List<Object>) propertyValue;
			final Object[] result = new Object[propertyValues.size()];
			for (int i = 0; i < propertyValues.size(); i++) {
				result[i] = new Object[] {
						"", LabelUtils.toString(propertyValues.get(i)) }; //$NON-NLS-1$
			}
			return result;
		} else if (propertyValue instanceof Map<?, ?>) {
			final Map<?, ?> propertyValues = (Map<?, ?>) propertyValue;
			final Object[] result = new Object[propertyValues.size()];
			int i = 0;
			for (Entry<?, ?> entry : propertyValues.entrySet()) {
				result[i] = new Object[] { entry.getKey(), entry.getValue() };
				i++;
			}
			return result;
		}
		return EMPTY;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof Object[]) {
			final Object value = ((Object[]) element)[1];
			return (value instanceof List || value instanceof Map || value instanceof IDockerContainerConfig);
		}
		return false;
	}

}
