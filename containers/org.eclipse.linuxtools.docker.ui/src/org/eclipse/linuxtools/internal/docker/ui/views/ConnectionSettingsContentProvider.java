/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
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

package org.eclipse.linuxtools.internal.docker.ui.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.linuxtools.docker.core.IDockerConnectionSettings;

public class ConnectionSettingsContentProvider implements ITreeContentProvider {

	private static final Object[] EMPTY = new Object[0];

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
	}

	@Override
	public Object[] getElements(final Object inputElement) {
		if (inputElement instanceof IDockerConnection) {
			final IDockerConnection connection = (IDockerConnection) inputElement;
			IDockerConnectionSettings data = connection.getSettings();
			if (data != null) {
				return data.getProperties();
			}
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
