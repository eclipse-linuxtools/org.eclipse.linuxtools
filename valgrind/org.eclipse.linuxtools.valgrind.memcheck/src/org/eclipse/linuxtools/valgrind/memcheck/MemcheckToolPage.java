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
//	protected Combo leakCheckCombo;
	protected Combo leakResCombo;
	protected Button showReachableButton;
	protected Spinner freelistSpinner;
	protected Button partialLoadsButton;
	protected Button undefValueButton;
	protected Button gccWorkaroundButton;
	protected Spinner alignmentSpinner;
	
	// LaunchConfiguration attributes
	public static final String ATTR_MEMCHECK_LEAKCHECK = PLUGIN_ID + ".MEMCHECK_LEAKCHECK"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_LEAKRES = PLUGIN_ID + ".MEMCHECK_LEAKRES"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_SHOWREACH = PLUGIN_ID + ".MEMCHECK_SHOWREACH"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_PARTIAL = PLUGIN_ID + ".MEMCHECK_PARTIAL"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_UNDEF = PLUGIN_ID + ".MEMCHECK_UNDEF"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_FREELIST = PLUGIN_ID + ".MEMCHECK_FREELIST"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_GCCWORK = PLUGIN_ID + ".MEMCHECK_GCCWORK"; //$NON-NLS-1$
	public static final String ATTR_MEMCHECK_ALIGNMENT = PLUGIN_ID + ".MEMCHECK_ALIGNMENT"; //$NON-NLS-1$
	
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
		Composite top = new Composite(parent, SWT.NONE);
		GridLayout memcheckLayout = new GridLayout(2, true);
		top.setLayout(memcheckLayout);
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
//		Label leakCheckLabel = new Label(top, SWT.NONE);
//		leakCheckLabel.setText(Messages.getString("MemcheckToolPage.leak_check")); //$NON-NLS-1$
//		leakCheckCombo = new Combo(top, SWT.READ_ONLY);
//		leakCheckCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		String[] leakCheckOpts = { "no", "summary", "yes", "full" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
//		leakCheckCombo.setItems(leakCheckOpts);
//		leakCheckCombo.select(2);
//		leakCheckCombo.setEnabled(false);
//		leakCheckCombo.addSelectionListener(selectListener);

		Composite leakResTop = new Composite(top, SWT.NONE);
		leakResTop.setLayout(new GridLayout(2, false));
		leakResTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label leakResLabel = new Label(leakResTop, SWT.NONE);
		leakResLabel.setText(Messages.getString("MemcheckToolPage.leak_resolution")); //$NON-NLS-1$
		leakResCombo = new Combo(leakResTop, SWT.READ_ONLY);
		String[] leakResOpts = { "low", "med", "high" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		leakResCombo.setItems(leakResOpts);
		leakResCombo.addSelectionListener(selectListener);

		Composite freelistTop = new Composite(top, SWT.NONE);
		freelistTop.setLayout(new GridLayout(2, false));
		freelistTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label freelistLabel = new Label(freelistTop, SWT.NONE);
		freelistLabel.setText(Messages.getString("MemcheckToolPage.freelist_size")); //$NON-NLS-1$
		freelistSpinner = new Spinner(freelistTop, SWT.BORDER);
		freelistSpinner.setMaximum(Integer.MAX_VALUE);
		freelistSpinner.addModifyListener(modifyListener);

		showReachableButton = new Button(top, SWT.CHECK);
		showReachableButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		showReachableButton.setText(Messages.getString("MemcheckToolPage.show_reachable")); //$NON-NLS-1$
		showReachableButton.addSelectionListener(selectListener);
		
		partialLoadsButton = new Button(top, SWT.CHECK);
		partialLoadsButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		partialLoadsButton.setText(Messages.getString("MemcheckToolPage.allow_partial")); //$NON-NLS-1$
		partialLoadsButton.addSelectionListener(selectListener);

		undefValueButton = new Button(top, SWT.CHECK);
		undefValueButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		undefValueButton.setText(Messages.getString("MemcheckToolPage.undef_value_errors")); //$NON-NLS-1$
		undefValueButton.addSelectionListener(selectListener);

		gccWorkaroundButton = new Button(top, SWT.CHECK);
		gccWorkaroundButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		gccWorkaroundButton.setText(Messages.getString("MemcheckToolPage.gcc_296_workarounds")); //$NON-NLS-1$
		gccWorkaroundButton.addSelectionListener(selectListener);
		
		Composite alignmentTop = new Composite(top, SWT.NONE);
		alignmentTop.setLayout(new GridLayout(2, false));
		alignmentTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label alignmentLabel = new Label(alignmentTop, SWT.NONE);
		alignmentLabel.setText(Messages.getString("MemcheckToolPage.minimum_heap_block")); //$NON-NLS-1$		
		alignmentSpinner = new Spinner(alignmentTop, SWT.BORDER);
		alignmentSpinner.setMinimum(8);
		alignmentSpinner.setMaximum(4096);
		alignmentSpinner.addModifyListener(modifyListener);
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
		alignmentSpinner.setSelection(configuration.getAttribute(ATTR_MEMCHECK_ALIGNMENT, 8));
	}
	
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
//		configuration.setAttribute(ATTR_MEMCHECK_LEAKCHECK, leakCheckCombo.getText());
		configuration.setAttribute(ATTR_MEMCHECK_LEAKRES, leakResCombo.getText());
		configuration.setAttribute(ATTR_MEMCHECK_SHOWREACH, showReachableButton.getSelection());
		configuration.setAttribute(ATTR_MEMCHECK_FREELIST, freelistSpinner.getSelection());
		configuration.setAttribute(ATTR_MEMCHECK_PARTIAL, partialLoadsButton.getSelection());		
		configuration.setAttribute(ATTR_MEMCHECK_UNDEF, undefValueButton.getSelection());
		configuration.setAttribute(ATTR_MEMCHECK_GCCWORK, gccWorkaroundButton.getSelection());
		configuration.setAttribute(ATTR_MEMCHECK_ALIGNMENT, alignmentSpinner.getSelection());
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
		configuration.setAttribute(ATTR_MEMCHECK_ALIGNMENT, 8);
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
