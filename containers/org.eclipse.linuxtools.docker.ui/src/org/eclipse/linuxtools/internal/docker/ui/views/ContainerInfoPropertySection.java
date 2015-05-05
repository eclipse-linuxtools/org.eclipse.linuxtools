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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * @author xcoulon
 *
 */
public class ContainerInfoPropertySection extends BasePropertySection {

	private IDockerContainer selectedContainer;

	@Override
	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		getTreeViewer().setContentProvider(new ContainerInfoContentProvider());
	}
	
	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		Object input = null;
		if (selection instanceof ITreeSelection)
			input = ((ITreeSelection) selection).getFirstElement();
		else if (selection instanceof IStructuredSelection)
			input = ((IStructuredSelection) selection).getFirstElement();
		Assert.isTrue(input instanceof IDockerContainer);
		this.selectedContainer = (IDockerContainer) input;
		if (getTreeViewer() != null) {
			getTreeViewer().setInput(this.selectedContainer);
		}
	}

}
