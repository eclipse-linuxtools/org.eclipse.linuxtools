/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.valgrind.tests;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.linuxtools.valgrind.launch.ValgrindExportWizardPage;
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;

public class ValgrindTestExportWizardPage extends ValgrindExportWizardPage {

	protected ValgrindTestExportWizardPage(String pageName, String title,
			ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	
	public CheckboxTableViewer getViewer() {
		return viewer;
	}
	
	public Text getDestText() {
		return destText;
	}	

	public Button getSelectAllButton() {
		return selectAllButton;
	}
	
	public Button getDeselectAllButton() {
		return deselectAllButton;
	}
	
	@Override
	protected ValgrindLaunchPlugin getPlugin() {
		return ValgrindTestLaunchPlugin.getDefault();
	}
}
