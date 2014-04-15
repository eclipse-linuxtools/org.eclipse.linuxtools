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
package org.eclipse.linuxtools.internal.valgrind.cachegrind;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.internal.valgrind.launch.LaunchConfigurationConstants;
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
import org.osgi.framework.Version;

public class CachegrindToolPage extends AbstractLaunchConfigurationTab
		implements IValgrindToolPage {
	// Cachegrind controls
	private Button cacheButton;
	private Button branchButton;
	private Spinner i1SizeSpinner;
	private Spinner i1AssocSpinner;
	private Spinner i1LineSizeSpinner;
	private Button i1Button;
	private Spinner d1SizeSpinner;
	private Spinner d1AssocSpinner;
	private Spinner d1LineSizeSpinner;
	private Button d1Button;
	private Spinner l2SizeSpinner;
	private Spinner l2AssocSpinner;
	private Spinner l2LineSizeSpinner;
	private Button l2Button;

	private boolean isInitializing = false;
	private SelectionListener selectListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			updateLaunchConfigurationDialog();
		}
	};
	private ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}
	};

	@Override
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

		createVerticalSpacer(top, 1);

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

		Composite i1Top = new Composite(cacheGroup, SWT.BORDER);
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

		Composite d1Top = new Composite(cacheGroup, SWT.BORDER);
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

		Composite l2Top = new Composite(cacheGroup, SWT.BORDER);
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

	@Override
	public String getName() {
		return Messages.getString("CachegrindToolPage.Cachegrind_Options"); //$NON-NLS-1$
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {
		isInitializing = true;

		try
		{
			cacheButton.setSelection(config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_CACHE_SIM, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_CACHE_SIM));
			branchButton.setSelection(config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_BRANCH_SIM, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_BRANCH_SIM));

			i1Button.setSelection(config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_I1));
			i1SizeSpinner.setSelection(config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1_SIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_I1_SIZE));
			i1AssocSpinner.setSelection(config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1_ASSOC, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_I1_ASSOC));
			i1LineSizeSpinner.setSelection(config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1_LSIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_I1_LSIZE));
			checkI1Enablement();

			d1Button.setSelection(config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_D1));
			d1SizeSpinner.setSelection(config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1_SIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_D1_SIZE));
			d1AssocSpinner.setSelection(config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1_ASSOC, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_D1_ASSOC));
			d1LineSizeSpinner.setSelection(config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1_LSIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_D1_LSIZE));
			checkD1Enablement();

			l2Button.setSelection(config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_L2));
			l2SizeSpinner.setSelection(config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2_SIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_L2_SIZE));
			l2AssocSpinner.setSelection(config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2_ASSOC, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_L2_ASSOC));
			l2LineSizeSpinner.setSelection(config.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2_LSIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_L2_LSIZE));
			checkL2Enablement();

		} catch (CoreException e) {
			e.printStackTrace();
		}
		isInitializing = false;
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_CACHE_SIM, cacheButton.getSelection());
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_BRANCH_SIM, branchButton.getSelection());

		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1, i1Button.getSelection());
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1_SIZE, i1SizeSpinner.getSelection());
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1_ASSOC, i1AssocSpinner.getSelection());
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1_LSIZE, i1LineSizeSpinner.getSelection());

		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1, d1Button.getSelection());
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1_SIZE, d1SizeSpinner.getSelection());
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1_ASSOC, d1AssocSpinner.getSelection());
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1_LSIZE, d1LineSizeSpinner.getSelection());

		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2, l2Button.getSelection());
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2_SIZE, l2SizeSpinner.getSelection());
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2_ASSOC, l2AssocSpinner.getSelection());
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2_LSIZE, l2LineSizeSpinner.getSelection());
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		config.setAttribute(LaunchConfigurationConstants.ATTR_TOOL, CachegrindPlugin.TOOL_ID);
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_CACHE_SIM, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_CACHE_SIM);
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_BRANCH_SIM, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_BRANCH_SIM);

		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_I1);
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1_SIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_I1_SIZE);
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1_ASSOC, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_I1_ASSOC);
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_I1_LSIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_I1_LSIZE);

		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_D1);
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1_SIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_D1_SIZE);
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1_ASSOC, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_D1_ASSOC);
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_D1_LSIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_D1_LSIZE);

		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_L2);
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2_SIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_L2_SIZE);
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2_ASSOC, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_L2_ASSOC);
		config.setAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_L2_LSIZE, CachegrindLaunchConstants.DEFAULT_CACHEGRIND_L2_LSIZE);
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);

		boolean result = false;
		try {
			result = launchConfig.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_CACHE_SIM, true)
			|| launchConfig.getAttribute(CachegrindLaunchConstants.ATTR_CACHEGRIND_BRANCH_SIM, false);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		if (!result) {
			setErrorMessage(Messages.getString("CachegrindToolPage.At_least_one_of")); //$NON-NLS-1$
		}
		return result;
	}

	@Override
	public void setValgrindVersion(Version ver) {
		// Not used
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

	public Button getCacheButton() {
		return cacheButton;
	}

	public Button getBranchButton() {
		return branchButton;
	}

	public Spinner getI1SizeSpinner() {
		return i1SizeSpinner;
	}

	public Spinner getI1AssocSpinner() {
		return i1AssocSpinner;
	}

	public Spinner getI1LineSizeSpinner() {
		return i1LineSizeSpinner;
	}

	public Button getI1Button() {
		return i1Button;
	}

	public Spinner getD1SizeSpinner() {
		return d1SizeSpinner;
	}

	public Spinner getD1AssocSpinner() {
		return d1AssocSpinner;
	}

	public Spinner getD1LineSizeSpinner() {
		return d1LineSizeSpinner;
	}

	public Button getD1Button() {
		return d1Button;
	}

	public Spinner getL2SizeSpinner() {
		return l2SizeSpinner;
	}

	public Spinner getL2AssocSpinner() {
		return l2AssocSpinner;
	}

	public Spinner getL2LineSizeSpinner() {
		return l2LineSizeSpinner;
	}

	public Button getL2Button() {
		return l2Button;
	}
}
