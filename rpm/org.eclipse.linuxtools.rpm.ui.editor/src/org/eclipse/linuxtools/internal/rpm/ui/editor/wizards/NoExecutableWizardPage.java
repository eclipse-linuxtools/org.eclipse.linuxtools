/*******************************************************************************
 * Copyright (c) 2009-2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.rpm.ui.editor.wizards.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class NoExecutableWizardPage extends WizardPage {

	protected NoExecutableWizardPage() {
		super(Messages.NoExecutableWizardPage_0);
		this.setImageDescriptor(ImageDescriptor.createFromFile(getClass(),
				"/icons/rpm.gif")); //$NON-NLS-1$
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;

		// Project
		Label label = new Label(container, SWT.NULL);
		label
				.setText(Messages.NoExecutableWizardPage_1);
		// empty label for the last row.
		new Label(container, SWT.NULL);
		setControl(container);
		this.setTitle(Messages.NoExecutableWizardPage_2);
	}

	@Override
	public boolean isPageComplete() {
		return false;
	}



}
