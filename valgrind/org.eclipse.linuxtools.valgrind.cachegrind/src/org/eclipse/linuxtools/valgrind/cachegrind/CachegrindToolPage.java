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
package org.eclipse.linuxtools.valgrind.cachegrind;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

public class CachegrindToolPage extends AbstractLaunchConfigurationTab
		implements IValgrindToolPage {
	public static final String CACHEGRIND = "cachegrind"; //$NON-NLS-1$
	public static final String PLUGIN_ID = CachegrindPlugin.PLUGIN_ID;
	
	// Cachegrind controls
	protected Button cacheButton;
	protected Button branchButton;
	protected Composite i1Top;
	protected Spinner i1SizeSpinner;
	protected Spinner i1AssocSpinner;
	protected Spinner i1LineSizeSpinner;
	protected Button i1Button;
	protected Composite d1Top;
	protected Spinner d1SizeSpinner;
	protected Spinner d1AssocSpinner;
	protected Spinner d1LineSizeSpinner;
	protected Button d1Button;
	protected Composite l2Top;
	protected Spinner l2SizeSpinner;
	protected Spinner l2AssocSpinner;
	protected Spinner l2LineSizeSpinner;
	protected Button l2Button;
	
	// LaunchConfiguration attributes
	public static final String ATTR_CACHEGRIND_CACHE_SIM = PLUGIN_ID + ".CACHE_SIM"; //$NON-NLS-1$
	public static final String ATTR_CACHEGRIND_BRANCH_SIM = PLUGIN_ID + ".BRANCH_SIM"; //$NON-NLS-1$
	public static final String ATTR_CACHEGRIND_I1 = PLUGIN_ID + ".I1"; //$NON-NLS-1$
	public static final String ATTR_CACHEGRIND_I1_SIZE = PLUGIN_ID + ".I1_SIZE"; //$NON-NLS-1$
	public static final String ATTR_CACHEGRIND_I1_ASSOC = PLUGIN_ID + ".I1_ASSOC"; //$NON-NLS-1$
	public static final String ATTR_CACHEGRIND_I1_LSIZE = PLUGIN_ID + ".I1_LSIZE"; //$NON-NLS-1$
	public static final String ATTR_CACHEGRIND_D1 = PLUGIN_ID + ".D1"; //$NON-NLS-1$
	public static final String ATTR_CACHEGRIND_D1_SIZE = PLUGIN_ID + ".D1_SIZE"; //$NON-NLS-1$
	public static final String ATTR_CACHEGRIND_D1_ASSOC = PLUGIN_ID + ".D1_ASSOC"; //$NON-NLS-1$
	public static final String ATTR_CACHEGRIND_D1_LSIZE = PLUGIN_ID + ".D1_LSIZE"; //$NON-NLS-1$
	public static final String ATTR_CACHEGRIND_L2 = PLUGIN_ID + ".L2"; //$NON-NLS-1$
	public static final String ATTR_CACHEGRIND_L2_SIZE = PLUGIN_ID + ".L2_SIZE"; //$NON-NLS-1$
	public static final String ATTR_CACHEGRIND_L2_ASSOC = PLUGIN_ID + ".L2_ASSOC"; //$NON-NLS-1$
	public static final String ATTR_CACHEGRIND_L2_LSIZE = PLUGIN_ID + ".L2_LSIZE"; //$NON-NLS-1$
	
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
		top.setLayout(new GridLayout());
		top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Event options
		cacheButton = new Button(top, SWT.CHECK);
		cacheButton.setText(Messages.getString("CachegrindToolPage.Profile_Cache")); //$NON-NLS-1$
		cacheButton.addSelectionListener(selectListener);
		
		branchButton = new Button(top, SWT.CHECK);
		branchButton.setText(Messages.getString("CachegrindToolPage.Profile_Branch")); //$NON-NLS-1$
		branchButton.addSelectionListener(selectListener);
		
		Group cacheGroup = new Group(top, SWT.SHADOW_OUT);
		cacheGroup.setLayout(new GridLayout(2, false));
		cacheGroup.setText(Messages.getString("CachegrindToolPage.Manually_Set_Cache")); //$NON-NLS-1$
		
		// I1 Cache
		i1Button = new Button(cacheGroup, SWT.CHECK);
		i1Button.setText(Messages.getString("CachegrindToolPage.I1_Cache")); //$NON-NLS-1$
		i1Button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkI1Enablement();
				updateLaunchConfigurationDialog();
			}
		});
		
		i1Top = new Composite(cacheGroup, SWT.BORDER);
		GridLayout i1Layout = new GridLayout(6, false);
		i1Layout.horizontalSpacing = 10;
		i1Top.setLayout(i1Layout);
		
		Label i1SizeLabel = new Label(i1Top, SWT.NONE);
		i1SizeLabel.setText(Messages.getString("CachegrindToolPage.Size")); //$NON-NLS-1$
		
		i1SizeSpinner = new Spinner(i1Top, SWT.BORDER);
		i1SizeSpinner.setMaximum(Integer.MAX_VALUE);
		i1SizeSpinner.addModifyListener(modifyListener);
		
		Label i1AssocLabel = new Label(i1Top, SWT.NONE);
		i1AssocLabel.setText(Messages.getString("CachegrindToolPage.Assoc")); //$NON-NLS-1$
		
		i1AssocSpinner = new Spinner(i1Top, SWT.BORDER);
		i1AssocSpinner.setMaximum(Integer.MAX_VALUE);
		i1AssocSpinner.addModifyListener(modifyListener);
		
		Label i1LineSizeLabel = new Label(i1Top, SWT.NONE);
		i1LineSizeLabel.setText(Messages.getString("CachegrindToolPage.Line_Size")); //$NON-NLS-1$
		
		i1LineSizeSpinner = new Spinner(i1Top, SWT.BORDER);
		i1LineSizeSpinner.setMaximum(Integer.MAX_VALUE);
		i1LineSizeSpinner.addModifyListener(modifyListener);
		
		// D1 Cache
		d1Button = new Button(cacheGroup, SWT.CHECK);
		d1Button.setText(Messages.getString("CachegrindToolPage.D1_Cache")); //$NON-NLS-1$
		d1Button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkD1Enablement();
				updateLaunchConfigurationDialog();
			}
		});
		
		d1Top = new Composite(cacheGroup, SWT.BORDER);
		GridLayout d1Layout = new GridLayout(6, false);
		d1Layout.horizontalSpacing = 10;
		d1Top.setLayout(d1Layout);
		
		Label d1SizeLabel = new Label(d1Top, SWT.NONE);
		d1SizeLabel.setText(Messages.getString("CachegrindToolPage.Size")); //$NON-NLS-1$
		
		d1SizeSpinner = new Spinner(d1Top, SWT.BORDER);
		d1SizeSpinner.setMaximum(Integer.MAX_VALUE);
		d1SizeSpinner.addModifyListener(modifyListener);
		
		Label d1AssocLabel = new Label(d1Top, SWT.NONE);
		d1AssocLabel.setText(Messages.getString("CachegrindToolPage.Assoc")); //$NON-NLS-1$
		
		d1AssocSpinner = new Spinner(d1Top, SWT.BORDER);
		d1AssocSpinner.setMaximum(Integer.MAX_VALUE);
		d1AssocSpinner.addModifyListener(modifyListener);
		
		Label d1LineSizeLabel = new Label(d1Top, SWT.NONE);
		d1LineSizeLabel.setText(Messages.getString("CachegrindToolPage.Line_Size")); //$NON-NLS-1$
		
		d1LineSizeSpinner = new Spinner(d1Top, SWT.BORDER);
		d1LineSizeSpinner.setMaximum(Integer.MAX_VALUE);
		d1LineSizeSpinner.addModifyListener(modifyListener);
		
		// D1 Cache
		l2Button = new Button(cacheGroup, SWT.CHECK);
		l2Button.setText(Messages.getString("CachegrindToolPage.L2_Cache")); //$NON-NLS-1$
		l2Button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkL2Enablement();
				updateLaunchConfigurationDialog();
			}
		});
		
		l2Top = new Composite(cacheGroup, SWT.BORDER);
		GridLayout l2Layout = new GridLayout(6, false);
		l2Layout.horizontalSpacing = 10;
		l2Top.setLayout(l2Layout);
		
		Label l2SizeLabel = new Label(l2Top, SWT.NONE);
		l2SizeLabel.setText(Messages.getString("CachegrindToolPage.Size")); //$NON-NLS-1$
		
		l2SizeSpinner = new Spinner(l2Top, SWT.BORDER);
		l2SizeSpinner.setMaximum(Integer.MAX_VALUE);
		l2SizeSpinner.addModifyListener(modifyListener);
		
		Label l2AssocLabel = new Label(l2Top, SWT.NONE);
		l2AssocLabel.setText(Messages.getString("CachegrindToolPage.Assoc")); //$NON-NLS-1$
		
		l2AssocSpinner = new Spinner(l2Top, SWT.BORDER);
		l2AssocSpinner.setMaximum(Integer.MAX_VALUE);
		l2AssocSpinner.addModifyListener(modifyListener);
		
		Label l2LineSizeLabel = new Label(l2Top, SWT.NONE);
		l2LineSizeLabel.setText(Messages.getString("CachegrindToolPage.Line_Size")); //$NON-NLS-1$
		
		l2LineSizeSpinner = new Spinner(l2Top, SWT.BORDER);
		l2LineSizeSpinner.setMaximum(Integer.MAX_VALUE);
		l2LineSizeSpinner.addModifyListener(modifyListener);
	}

	public String getName() {
		return Messages.getString("CachegrindToolPage.Cachegrind_Options"); //$NON-NLS-1$
	}

	public void initializeFrom(ILaunchConfiguration config) {
		try {
			cacheButton.setSelection(config.getAttribute(ATTR_CACHEGRIND_CACHE_SIM, true));
			branchButton.setSelection(config.getAttribute(ATTR_CACHEGRIND_BRANCH_SIM, false));
			
			i1Button.setSelection(config.getAttribute(ATTR_CACHEGRIND_I1, false));
			i1SizeSpinner.setSelection(config.getAttribute(ATTR_CACHEGRIND_I1_SIZE, 0));
			i1AssocSpinner.setSelection(config.getAttribute(ATTR_CACHEGRIND_I1_ASSOC, 0));
			i1LineSizeSpinner.setSelection(config.getAttribute(ATTR_CACHEGRIND_I1_LSIZE, 0));
			checkI1Enablement();
			
			d1Button.setSelection(config.getAttribute(ATTR_CACHEGRIND_D1, false));
			d1SizeSpinner.setSelection(config.getAttribute(ATTR_CACHEGRIND_D1_SIZE, 0));
			d1AssocSpinner.setSelection(config.getAttribute(ATTR_CACHEGRIND_D1_ASSOC, 0));
			d1LineSizeSpinner.setSelection(config.getAttribute(ATTR_CACHEGRIND_D1_LSIZE, 0));
			checkD1Enablement();
			
			l2Button.setSelection(config.getAttribute(ATTR_CACHEGRIND_L2, false));
			l2SizeSpinner.setSelection(config.getAttribute(ATTR_CACHEGRIND_L2_SIZE, 0));
			l2AssocSpinner.setSelection(config.getAttribute(ATTR_CACHEGRIND_L2_ASSOC, 0));
			l2LineSizeSpinner.setSelection(config.getAttribute(ATTR_CACHEGRIND_L2_LSIZE, 0));
			checkL2Enablement();
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(ATTR_CACHEGRIND_CACHE_SIM, cacheButton.getSelection());
		config.setAttribute(ATTR_CACHEGRIND_BRANCH_SIM, branchButton.getSelection());
		
		config.setAttribute(ATTR_CACHEGRIND_I1, i1Button.getSelection());
		config.setAttribute(ATTR_CACHEGRIND_I1_SIZE, i1SizeSpinner.getSelection());
		config.setAttribute(ATTR_CACHEGRIND_I1_ASSOC, i1AssocSpinner.getSelection());
		config.setAttribute(ATTR_CACHEGRIND_I1_LSIZE, i1LineSizeSpinner.getSelection());
		
		config.setAttribute(ATTR_CACHEGRIND_D1, d1Button.getSelection());
		config.setAttribute(ATTR_CACHEGRIND_D1_SIZE, d1SizeSpinner.getSelection());
		config.setAttribute(ATTR_CACHEGRIND_D1_ASSOC, d1AssocSpinner.getSelection());
		config.setAttribute(ATTR_CACHEGRIND_D1_LSIZE, d1LineSizeSpinner.getSelection());
		
		config.setAttribute(ATTR_CACHEGRIND_L2, l2Button.getSelection());
		config.setAttribute(ATTR_CACHEGRIND_L2_SIZE, l2SizeSpinner.getSelection());
		config.setAttribute(ATTR_CACHEGRIND_L2_ASSOC, l2AssocSpinner.getSelection());
		config.setAttribute(ATTR_CACHEGRIND_L2_LSIZE, l2LineSizeSpinner.getSelection());
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(ATTR_CACHEGRIND_CACHE_SIM, true);
		config.setAttribute(ATTR_CACHEGRIND_BRANCH_SIM, false);
		
		config.setAttribute(ATTR_CACHEGRIND_I1, false);
		config.setAttribute(ATTR_CACHEGRIND_I1_SIZE, 0);
		config.setAttribute(ATTR_CACHEGRIND_I1_ASSOC, 0);
		config.setAttribute(ATTR_CACHEGRIND_I1_LSIZE, 0);
		
		config.setAttribute(ATTR_CACHEGRIND_D1, false);
		config.setAttribute(ATTR_CACHEGRIND_D1_SIZE, 0);
		config.setAttribute(ATTR_CACHEGRIND_D1_ASSOC, 0);
		config.setAttribute(ATTR_CACHEGRIND_D1_LSIZE, 0);
		
		config.setAttribute(ATTR_CACHEGRIND_L2, false);
		config.setAttribute(ATTR_CACHEGRIND_L2_SIZE, 0);
		config.setAttribute(ATTR_CACHEGRIND_L2_ASSOC, 0);
		config.setAttribute(ATTR_CACHEGRIND_L2_LSIZE, 0);
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);

		boolean result = false;
		try {
			result = launchConfig.getAttribute(ATTR_CACHEGRIND_CACHE_SIM, true)
			|| launchConfig.getAttribute(ATTR_CACHEGRIND_BRANCH_SIM, false);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if (!result) {
			setErrorMessage(Messages.getString("CachegrindToolPage.At_least_one_of")); //$NON-NLS-1$
		}
		return result;
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

	private void checkI1Enablement() {
		boolean checked = i1Button.getSelection();
		i1SizeSpinner.setEnabled(checked);
		i1AssocSpinner.setEnabled(checked);
		i1LineSizeSpinner.setEnabled(checked);
	}
	
	private void checkD1Enablement() {
		boolean checked = d1Button.getSelection();
		d1SizeSpinner.setEnabled(checked);
		d1AssocSpinner.setEnabled(checked);
		d1LineSizeSpinner.setEnabled(checked);
	}
	
	private void checkL2Enablement() {
		boolean checked = l2Button.getSelection();
		l2SizeSpinner.setEnabled(checked);
		l2AssocSpinner.setEnabled(checked);
		l2LineSizeSpinner.setEnabled(checked);
	}
}
