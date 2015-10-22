/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.vagrant.ui.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.vagrant.core.IVagrantConnection;
import org.eclipse.swt.widgets.Display;

public class VagrantVMContentProvider implements ITreeContentProvider{

	private static final Object[] EMPTY = new Object[0];
	private TableViewer viewer;

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		this.viewer = (TableViewer)viewer;
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if(inputElement instanceof IVagrantConnection) {
			final IVagrantConnection connection = (IVagrantConnection)inputElement;
			if (connection.isVMsLoaded()) {
				return connection.getVMs().toArray();
			}
			loadContainers(connection);
			return EMPTY;
		}
		return EMPTY;
	}

	/**
	 * Call the {@link IVagrantConnection#getContainers(boolean)} in a background job to avoid blocking the UI.
	 * @param containersCategory the selected {@link VagrantVMCategory}
	 */
	private void loadContainers(final IVagrantConnection connection) {
		final Job loadContainersJob = new Job(DVMessages.getString("VMLoadJob.msg")) { //$NON-NLS-1$
			@Override
			protected IStatus run(final IProgressMonitor monitor) {
				connection.getVMs(true);
				Display.getDefault().asyncExec(() -> viewer.refresh());
				return Status.OK_STATUS;
			}
		};
		loadContainersJob.schedule();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return false;
	}

}
