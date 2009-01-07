/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.massif;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

public class PowerOfTwoSpinner {
	protected Spinner spinner;
	protected int previous;
	
	public PowerOfTwoSpinner(Composite parent, int style) {
		spinner = new Spinner(parent, style | SWT.READ_ONLY);
		spinner.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int selection = spinner.getSelection();
				if (selection > previous) {
					setSelection((selection - 1) << 1); 
				}
				else {
					setSelection((selection + 1) >> 1); 
				}
			}			
		});
		spinner.setPageIncrement(1);
		previous = 0;
	}
	
	public void setSelection(int value) {
		previous = value;
		spinner.setSelection(value);
	}
	
	public void addModifyListener(ModifyListener listener) {
		spinner.addModifyListener(listener);
	}
	
	public void setMaximum(int value) {
		spinner.setMaximum(value);
	}
	
	public void setMinimum(int value) {
		spinner.setMinimum(value);
	}
	
	public int getSelection() {
		return spinner.getSelection();
	}
}
