/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.filter;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

public abstract class FilterWizardPage extends WizardPage {
	public FilterWizardPage(String title) {
		super("selectFilterOptions"); //$NON-NLS-1$
		setTitle(title);
	}

	@Override
	public void createControl(Composite parent) {
		wizard = (SelectFilterWizard)super.getWizard();
	}

	@Override
	public boolean canFlipToNextPage() {
		return false;
	}

	abstract void createFilter();

	@Override
	public void dispose() {
		wizard = null;
		super.dispose();
	}

	protected final SelectionListener selectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			createFilter();
			wizard.getContainer().updateButtons();
		}
	};

	protected final ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			createFilter();
			wizard.getContainer().updateButtons();
		}
	};

	protected SelectFilterWizard wizard;
}