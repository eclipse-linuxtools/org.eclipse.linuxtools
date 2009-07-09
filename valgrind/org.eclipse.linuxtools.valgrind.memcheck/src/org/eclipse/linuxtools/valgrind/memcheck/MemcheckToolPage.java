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
import org.eclipse.linuxtools.valgrind.launch.ValgrindLaunchPlugin;
import org.eclipse.osgi.util.NLS;
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
import org.osgi.framework.Version;

public class MemcheckToolPage extends AbstractLaunchConfigurationTab implements IValgrindToolPage {
	public static final String MEMCHECK = "memcheck"; //$NON-NLS-1$
	public static final String PLUGIN_ID = MemcheckPlugin.PLUGIN_ID;
	
	// MEMCHECK controls
	protected Button leakCheckButton;
	protected Combo leakResCombo;
	protected Button showReachableButton;
	protected Spinner freelistSpinner;
	protected Button partialLoadsButton;
	protected Button undefValueButton;
	protected Button gccWorkaroundButton;
	protected Spinner alignmentSpinner;
	
	// VG >= 3.4.0
	protected Button trackOriginsButton;
	
	protected boolean isInitializing = false;
	protected CoreException ex = null;
	
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
		
		leakCheckButton = new Button(top, SWT.CHECK);
		leakCheckButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		leakCheckButton.setText(Messages.getString("MemcheckToolPage.leak_check")); //$NON-NLS-1$
		leakCheckButton.addSelectionListener(selectListener);
		
		Composite leakResTop = new Composite(top, SWT.NONE);
		leakResTop.setLayout(new GridLayout(2, false));
		leakResTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label leakResLabel = new Label(leakResTop, SWT.NONE);
		leakResLabel.setText(Messages.getString("MemcheckToolPage.leak_resolution")); //$NON-NLS-1$
		leakResCombo = new Combo(leakResTop, SWT.READ_ONLY);
		String[] leakResOpts = { MemcheckLaunchConstants.LEAK_RES_LOW, MemcheckLaunchConstants.LEAK_RES_MED, MemcheckLaunchConstants.LEAK_RES_HIGH };
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

		// 3.4.0 specific
		try {
			Version ver = ValgrindLaunchPlugin.getDefault().getValgrindVersion();
			if (ver.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
				trackOriginsButton = new Button(top, SWT.CHECK);
				trackOriginsButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				trackOriginsButton.setText(Messages.getString("MemcheckToolPage.Track_origins")); //$NON-NLS-1$
				trackOriginsButton.addSelectionListener(selectListener);
			}
		} catch (CoreException e) {
			ex = e;
		}
		
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
			leakCheckButton.setSelection(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_LEAKCHECK, MemcheckLaunchConstants.DEFAULT_MEMCHECK_LEAKCHECK));
			leakResCombo.setText(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_LEAKRES, MemcheckLaunchConstants.DEFAULT_MEMCHECK_LEAKRES));
			showReachableButton.setSelection(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_SHOWREACH, MemcheckLaunchConstants.DEFAULT_MEMCHECK_SHOWREACH));
			freelistSpinner.setSelection(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_FREELIST, MemcheckLaunchConstants.DEFAULT_MEMCHECK_FREELIST));
			partialLoadsButton.setSelection(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_PARTIAL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_PARTIAL));
			undefValueButton.setSelection(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_UNDEF, MemcheckLaunchConstants.DEFAULT_MEMCHECK_UNDEF));
			gccWorkaroundButton.setSelection(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_GCCWORK, MemcheckLaunchConstants.DEFAULT_MEMCHECK_GCCWORK));
			alignmentSpinner.setSelection(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_ALIGNMENT, MemcheckLaunchConstants.DEFAULT_MEMCHECK_ALIGNMENT));
			
			// 3.4.0 specific
			Version ver = ValgrindLaunchPlugin.getDefault().getValgrindVersion();
			if (ver.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
				trackOriginsButton.setSelection(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_TRACKORIGINS, MemcheckLaunchConstants.DEFAULT_MEMCHECK_TRACKORIGINS));
			}
		} catch (CoreException e) {
			ex = e;
		}
		isInitializing = false;
	}
	
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_LEAKCHECK, leakCheckButton.getSelection());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_LEAKRES, leakResCombo.getText());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_SHOWREACH, showReachableButton.getSelection());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_FREELIST, freelistSpinner.getSelection());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_PARTIAL, partialLoadsButton.getSelection());		
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_UNDEF, undefValueButton.getSelection());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_GCCWORK, gccWorkaroundButton.getSelection());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_ALIGNMENT, alignmentSpinner.getSelection());
		
		// 3.4.0 specific
		try {
			Version ver = ValgrindLaunchPlugin.getDefault().getValgrindVersion();
			if (ver.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
				configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_TRACKORIGINS, trackOriginsButton.getSelection());
			}
		} catch (CoreException e) {
			ex = e;
		}
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		
		boolean result = false;
		try {
			// check alignment
			int alignment = launchConfig.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_ALIGNMENT, MemcheckLaunchConstants.DEFAULT_MEMCHECK_ALIGNMENT);
			result = (alignment & (alignment - 1)) == 0; // is power of two?			
			if (!result) {
				setErrorMessage(Messages.getString("MemcheckToolPage.Alignment_must_be_power_2")); //$NON-NLS-1$
			}
			else {
				// 3.4.0 specific
				Version ver = ValgrindLaunchPlugin.getDefault().getValgrindVersion();
				if (ver.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
					// check track-origins
					boolean trackOrigins = launchConfig.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_TRACKORIGINS, MemcheckLaunchConstants.DEFAULT_MEMCHECK_TRACKORIGINS);
					if (trackOrigins) {
						// undef-value-errors must be selected
						result = launchConfig.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_UNDEF, MemcheckLaunchConstants.DEFAULT_MEMCHECK_UNDEF);
						if (!result) {
							setErrorMessage(NLS.bind(Messages.getString("MemcheckToolPage.Track_origins_needs_undef"), Messages.getString("MemcheckToolPage.Track_origins"), Messages.getString("MemcheckToolPage.undef_value_errors"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
					}
				}
			}
		} catch (CoreException e) {
			ex = e;
		}
		
		if (ex != null) {
			setErrorMessage(ex.getLocalizedMessage());
		}
		return result;
	}
	
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_LEAKCHECK, MemcheckLaunchConstants.DEFAULT_MEMCHECK_LEAKCHECK);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_LEAKRES, MemcheckLaunchConstants.DEFAULT_MEMCHECK_LEAKRES);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_SHOWREACH, MemcheckLaunchConstants.DEFAULT_MEMCHECK_SHOWREACH);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_FREELIST, MemcheckLaunchConstants.DEFAULT_MEMCHECK_FREELIST);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_PARTIAL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_PARTIAL);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_UNDEF, MemcheckLaunchConstants.DEFAULT_MEMCHECK_UNDEF);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_GCCWORK, MemcheckLaunchConstants.DEFAULT_MEMCHECK_GCCWORK);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_ALIGNMENT, MemcheckLaunchConstants.DEFAULT_MEMCHECK_ALIGNMENT);
		
		// 3.4.0 specific
		try {
			Version ver = ValgrindLaunchPlugin.getDefault().getValgrindVersion();
			if (ver.compareTo(ValgrindLaunchPlugin.VER_3_4_0) >= 0) {
				configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_TRACKORIGINS, MemcheckLaunchConstants.DEFAULT_MEMCHECK_TRACKORIGINS);
			}
		} catch (CoreException e) {
			ex = e;
		}
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


	public Button getLeakCheckButton() {
		return leakCheckButton;
	}
	
	
	public Combo getLeakResCombo() {
		return leakResCombo;
	}


	public Button getShowReachableButton() {
		return showReachableButton;
	}


	public Spinner getFreelistSpinner() {
		return freelistSpinner;
	}


	public Button getPartialLoadsButton() {
		return partialLoadsButton;
	}


	public Button getUndefValueButton() {
		return undefValueButton;
	}


	public Button getGccWorkaroundButton() {
		return gccWorkaroundButton;
	}


	public Spinner getAlignmentSpinner() {
		return alignmentSpinner;
	}


	public Button getTrackOriginsButton() {
		return trackOriginsButton;
	}

}
