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
		if(inputElement instanceof IDockerConnectionInfo) {
			final IDockerConnectionInfo connectionInfo = (IDockerConnectionInfo) inputElement;
			return new Object[] {
					new Object[]{"Containers", connectionInfo.getContainers()}, //$NON-NLS-1$
					new Object[]{"Images", connectionInfo.getImages()}, //$NON-NLS-1$
					new Object[]{"Storage driver", connectionInfo.getStorageDriver()}, //$NON-NLS-1$
					new Object[]{"Execution driver", connectionInfo.getExecutionDriver()}, //$NON-NLS-1$
					new Object[]{"Kernel version", connectionInfo.getKernelVersion()}, //$NON-NLS-1$
					new Object[]{"Operating system", connectionInfo.getOs()}, //$NON-NLS-1$
					new Object[] { "CPU number", //$NON-NLS-1$
							connectionInfo.getCPUNumber() },
					new Object[] { "Total memory", //$NON-NLS-1$
							Long.toString(
									connectionInfo.getTotalMemory() / 1048576)
									+ " MB" },
					new Object[]{"File descriptors", connectionInfo.getFileDescriptors()}, //$NON-NLS-1$
					new Object[]{"Go routines", connectionInfo.getGoroutines()}, //$NON-NLS-1$
					new Object[]{"Init path", connectionInfo.getInitPath()}, //$NON-NLS-1$
					new Object[]{"API version", connectionInfo.getApiVersion()}, //$NON-NLS-1$
					new Object[]{"Version", connectionInfo.getVersion()}, //$NON-NLS-1$
					new Object[]{"Git commit", connectionInfo.getGitCommit()}, //$NON-NLS-1$
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
