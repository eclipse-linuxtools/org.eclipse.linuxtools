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

import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.MB;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.IDockerConnectionInfo;

/**
 * @author xcoulon
 *
 */
public class ConnectionInfoContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY = new Object[0];

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof IDockerConnectionInfo) {
			final IDockerConnectionInfo connectionInfo = (IDockerConnectionInfo) inputElement;
			return new Object[] {
					new Object[] { "Containers", //$NON-NLS-1$
							connectionInfo.getContainers() },
					new Object[] { "Images", connectionInfo.getImages() }, //$NON-NLS-1$
					new Object[] { "Storage driver", //$NON-NLS-1$
							connectionInfo.getStorageDriver() },
					new Object[] { "Execution driver", //$NON-NLS-1$
							connectionInfo.getExecutionDriver() },
					new Object[] { "Kernel version", //$NON-NLS-1$
							connectionInfo.getKernelVersion() },
					new Object[] { "Operating system", connectionInfo.getOs() }, //$NON-NLS-1$
					new Object[] { "CPU number", //$NON-NLS-1$
							connectionInfo.getCPUNumber() },
					new Object[] { "Total memory", //$NON-NLS-1$
							Long.toString(connectionInfo.getTotalMemory() / MB)
									+ " MB" },
					new Object[] { "File descriptors", //$NON-NLS-1$
							connectionInfo.getFileDescriptors() },
					new Object[] { "Go routines", //$NON-NLS-1$
							connectionInfo.getGoroutines() },
					new Object[] { "Init path", connectionInfo.getInitPath() }, //$NON-NLS-1$
					new Object[] { "API version", //$NON-NLS-1$
							connectionInfo.getApiVersion() },
					new Object[] { "Version", connectionInfo.getVersion() }, //$NON-NLS-1$
					new Object[] { "Git commit", //$NON-NLS-1$
							connectionInfo.getGitCommit() }, 
			};
		}
		return EMPTY;
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		return EMPTY;
	}

	@Override
	public Object getParent(final Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(final Object element) {
		return false;
	}
	
}
