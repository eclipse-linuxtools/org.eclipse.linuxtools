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
package org.eclipse.linuxtools.valgrind.memcheck;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.valgrind.launch.IValgrindToolPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class MemcheckToolPage extends AbstractLaunchConfigurationTab implements IValgrindToolPage {
	public static final String MEMCHECK = "memcheck"; //$NON-NLS-1$
	public static final String PLUGIN_ID = MemcheckPlugin.PLUGIN_ID;
	
	// MEMCHECK controls
	protected Combo leakCheckCombo;
	protected Combo leakResCombo;
	protected Button showReachableButton;
	protected Spinner freelistSpinner;
	protected Button partialLoadsButton;
	protected Button undefValueButton;
	protected Button gccWorkaroundButton;
	
	// LaunchConfiguration attributes
	public static final String ATTR_MEMCHECK_LEAKCHECK = PLUGIN_ID + ".MEMCHECK_LEAKCHECK"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_LEAKRES = PLUGIN_ID + ".MEMCHECK_LEAKRES"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_SHOWREACH = PLUGIN_ID + ".MEMCHECK_SHOWREACH"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_PARTIAL = PLUGIN_ID + ".MEMCHECK_PARTIAL"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_UNDEF = PLUGIN_ID + ".MEMCHECK_UNDEF"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_FREELIST = PLUGIN_ID + ".MEMCHECK_FREELIST"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_GCCWORK = PLUGIN_ID + ".MEMCHECK_GCCWORK"; //$NON-NLS-1$
	
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	protected static final String NO = "no"; //$NON-NLS-1$
	protected static final String YES = "yes"; //$NON-NLS-1$
	protected static final String EQUALS = "="; //$NON-NLS-1$
	
	protected boolean isInitializing = false;
	protected SelectionListener selectListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			updateLaunchConfigurationDialog();
		}
	};
	protected ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();	
		}			
	};
	
	public void createControl(Composite parent) {
		Composite memcheckTop = new Composite(parent, SWT.NONE);
		GridLayout memcheckLayout = new GridLayout(8, false);
		memcheckTop.setLayout(memcheckLayout);
		memcheckTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		setControl(memcheckTop);

		Label leakCheckLabel = new Label(memcheckTop, SWT.NONE);
		leakCheckLabel.setText(Messages.getString("MemcheckToolPage.leak_check")); //$NON-NLS-1$
		leakCheckCombo = new Combo(memcheckTop, SWT.READ_ONLY);
		leakCheckCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		String[] leakCheckOpts = { "no", "summary", "yes", "full" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		leakCheckCombo.setItems(leakCheckOpts);
		leakCheckCombo.select(2);
		leakCheckCombo.setEnabled(false);
		//leakCheckCombo.addSelectionListener(selectListener);

		createHorizontalSpacer(memcheckTop, 1);

		Label leakResLabel = new Label(memcheckTop, SWT.NONE);
		leakResLabel.setText(Messages.getString("MemcheckToolPage.leak_resolution")); //$NON-NLS-1$
		leakResCombo = new Combo(memcheckTop, SWT.READ_ONLY);
		leakResCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		String[] leakResOpts = { "low", "med", "high" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		leakResCombo.setItems(leakResOpts);
		leakResCombo.addSelectionListener(selectListener);

		createHorizontalSpacer(memcheckTop, 1);

		Label showReachableLabel = new Label(memcheckTop, SWT.NONE);
		showReachableLabel.setText(Messages.getString("MemcheckToolPage.show_reachable")); //$NON-NLS-1$
		showReachableButton = new Button(memcheckTop, SWT.CHECK);
		showReachableButton.addSelectionListener(selectListener);

		createVerticalSpacer(memcheckTop, 1);

		Label freelistLabel = new Label(memcheckTop, SWT.NONE);
		freelistLabel.setText(Messages.getString("MemcheckToolPage.freelist_size")); //$NON-NLS-1$
		freelistSpinner = new Spinner(memcheckTop, SWT.BORDER);
		freelistSpinner.setMaximum(Integer.MAX_VALUE);
		freelistSpinner.addModifyListener(modifyListener);

		createHorizontalSpacer(memcheckTop, 1);

		Label partialLoadsLabel = new Label(memcheckTop, SWT.NONE);
		partialLoadsLabel.setText(Messages.getString("MemcheckToolPage.allow_partial")); //$NON-NLS-1$
		partialLoadsButton = new Button(memcheckTop, SWT.CHECK);
		partialLoadsButton.addSelectionListener(selectListener);

		createHorizontalSpacer(memcheckTop, 1);

		Label undefValueLabel = new Label(memcheckTop, SWT.NONE);
		undefValueLabel.setText(Messages.getString("MemcheckToolPage.undef_value_errors")); //$NON-NLS-1$
		undefValueButton = new Button(memcheckTop, SWT.CHECK);
		undefValueButton.addSelectionListener(selectListener);

		createVerticalSpacer(memcheckTop, 1);

		Label gccWorkaroundLabel = new Label(memcheckTop, SWT.NONE);
		gccWorkaroundLabel.setText(Messages.getString("MemcheckToolPage.gcc_296_workarounds")); //$NON-NLS-1$
		gccWorkaroundButton = new Button(memcheckTop, SWT.CHECK);
		gccWorkaroundButton.addSelectionListener(selectListener);
	}

	
	public String getName() {
		return Messages.getString("MemcheckToolPage.Memcheck_Options"); //$NON-NLS-1$
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		isInitializing = true;
		try {
			initializeMemcheck(configuration);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		isInitializing = false;
	}

	protected void initializeMemcheck(ILaunchConfiguration configuration)
	throws CoreException {
//		leakCheckCombo.setText(configuration.getAttribute(ATTR_MEMCHECK_LEAKCHECK, "summary"));
		leakResCombo.setText(configuration.getAttribute(ATTR_MEMCHECK_LEAKRES, "low")); //$NON-NLS-1$
		showReachableButton.setSelection(configuration.getAttribute(ATTR_MEMCHECK_SHOWREACH, false));
		freelistSpinner.setSelection(configuration.getAttribute(ATTR_MEMCHECK_FREELIST, 10000000));
		partialLoadsButton.setSelection(configuration.getAttribute(ATTR_MEMCHECK_PARTIAL, false));
		undefValueButton.setSelection(configuration.getAttribute(ATTR_MEMCHECK_UNDEF, true));
		gccWorkaroundButton.setSelection(configuration.getAttribute(ATTR_MEMCHECK_GCCWORK, false));
	}
	
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
//		configuration.setAttribute(ATTR_MEMCHECK_LEAKCHECK, leakCheckCombo.getText());
		configuration.setAttribute(ATTR_MEMCHECK_LEAKRES, leakResCombo.getText());
		configuration.setAttribute(ATTR_MEMCHECK_SHOWREACH, showReachableButton.getSelection());
		configuration.setAttribute(ATTR_MEMCHECK_FREELIST, freelistSpinner.getSelection());
		configuration.setAttribute(ATTR_MEMCHECK_PARTIAL, partialLoadsButton.getSelection());		
		configuration.setAttribute(ATTR_MEMCHECK_UNDEF, undefValueButton.getSelection());
		configuration.setAttribute(ATTR_MEMCHECK_GCCWORK, gccWorkaroundButton.getSelection());	
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		setDefaultToolAttributes(configuration);
	}
	
	protected static void setDefaultToolAttributes(
			ILaunchConfigurationWorkingCopy configuration) {
//		configuration.setAttribute(ATTR_MEMCHECK_LEAKCHECK, "summary");
		configuration.setAttribute(ATTR_MEMCHECK_LEAKRES, "low"); //$NON-NLS-1$
		configuration.setAttribute(ATTR_MEMCHECK_SHOWREACH, false);
		configuration.setAttribute(ATTR_MEMCHECK_FREELIST, 10000000);
		configuration.setAttribute(ATTR_MEMCHECK_PARTIAL, false);
		configuration.setAttribute(ATTR_MEMCHECK_UNDEF, true);
		configuration.setAttribute(ATTR_MEMCHECK_GCCWORK, false);
	}
	
	protected void createHorizontalSpacer(Composite comp, int numlines) {
		Label lbl = new Label(comp, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = numlines;
		lbl.setLayoutData(gd);
	}
	
	@Override
	protected void updateLaunchConfigurationDialog() {
		if (!isInitializing) {
			super.updateLaunchConfigurationDialog();
		}
	}

}
