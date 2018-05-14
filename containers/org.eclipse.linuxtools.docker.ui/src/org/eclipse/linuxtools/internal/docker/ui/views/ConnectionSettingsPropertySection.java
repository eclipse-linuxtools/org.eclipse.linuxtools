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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.linuxtools.docker.core.IDockerConnection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class ConnectionSettingsPropertySection extends BasePropertySection {

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage propertySheetPage) {
		super.createControls(parent, propertySheetPage);
		getTreeViewer()
				.setContentProvider(new ConnectionSettingsContentProvider());
	}

	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		Assert.isTrue(selection instanceof ITreeSelection);
		Object input = ((ITreeSelection) selection).getFirstElement();
		Assert.isTrue(input instanceof IDockerConnection);
		IDockerConnection connection = (IDockerConnection) input;
		if (getTreeViewer() != null) {
			getTreeViewer().setInput(connection);
			getTreeViewer().expandAll();
		}
	}

}
