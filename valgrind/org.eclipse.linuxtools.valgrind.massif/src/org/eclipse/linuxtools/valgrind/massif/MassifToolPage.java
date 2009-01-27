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

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.valgrind.launch.IValgrindToolPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Spinner;

public class MassifToolPage extends AbstractLaunchConfigurationTab
		implements IValgrindToolPage {
	public static final String MASSIF = "massif"; //$NON-NLS-1$
	public static final String PLUGIN_ID = MassifPlugin.PLUGIN_ID;
	
	public static final String TIME_I = "i"; //$NON-NLS-1$
	public static final String TIME_MS = "ms"; //$NON-NLS-1$
	public static final String TIME_B = "B"; //$NON-NLS-1$
	
	// Massif controls
	protected Button heapButton;
	protected Spinner heapAdminSpinner;
	protected Button stacksButton;
	protected Spinner depthSpinner;
	protected List allocFnList;
	protected Spinner thresholdSpinner;
	protected Spinner peakInaccuracySpinner;
	protected Combo timeUnitCombo;
	protected Spinner detailedFreqSpinner;
	protected Spinner maxSnapshotsSpinner;
	protected Spinner alignmentSpinner;
	
	// LaunchConfiguration attributes
	public static final String ATTR_MASSIF_OUTFILE = PLUGIN_ID + ".MASSIF_OUTFILE"; //$NON-NLS-1$
	
	public static final String ATTR_MASSIF_HEAP = PLUGIN_ID + ".MASSIF_HEAP"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_HEAPADMIN = PLUGIN_ID + ".MASSIF_HEAPADMIN"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_STACKS = PLUGIN_ID + ".MASSIF_STACKS"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_DEPTH = PLUGIN_ID + ".MASSIF_DEPTH"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_ALLOCFN = PLUGIN_ID + ".MASSIF_ALLOCFN"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_THRESHOLD = PLUGIN_ID + ".MASSIF_THRESHOLD"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_PEAKINACCURACY = PLUGIN_ID + ".MASSIF_PEAKINACCURACY"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_TIMEUNIT = PLUGIN_ID + ".MASSIF_TIMEUNIT"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_DETAILEDFREQ = PLUGIN_ID + ".MASSIF_DETAILEDFREQ"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_MAXSNAPSHOTS = PLUGIN_ID + ".MASSIF_MAXSNAPSHOTS"; //$NON-NLS-1$
	public static final String ATTR_MASSIF_ALIGNMENT = PLUGIN_ID + ".MASSIF_ALIGNMENT"; //$NON-NLS-1$
	
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
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setLayoutData(new GridData(GridData.FILL_BOTH));
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		Composite top = new Composite(sc, SWT.NONE);
		
		GridLayout topLayout = new GridLayout(2, true);
		topLayout.horizontalSpacing = 10;
		top.setLayout(topLayout);
		top.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite heapTop = new Composite(top, SWT.NONE);
		heapTop.setLayout(new GridLayout(2, false));
		
		Label heapLabel = new Label(heapTop, SWT.NONE);
		heapLabel.setText(Messages.getString("MassifToolPage.profile_heap")); //$NON-NLS-1$
		
		heapButton = new Button(heapTop, SWT.CHECK);
		heapButton.addSelectionListener(selectListener);		
		
		Composite heapAdminTop = new Composite(top, SWT.NONE);
		heapAdminTop.setLayout(new GridLayout(2, false));
		
		Label heapAdminLabel = new Label(heapAdminTop, SWT.NONE);
		heapAdminLabel.setText(Messages.getString("MassifToolPage.administrative_bytes")); //$NON-NLS-1$
		
		heapAdminSpinner = new Spinner(heapAdminTop, SWT.BORDER);		
		heapAdminSpinner.addModifyListener(modifyListener);
			
		Composite stacksTop = new Composite(top, SWT.NONE);
		stacksTop.setLayout(new GridLayout(2, false));
		
		Label stacksLabel = new Label(stacksTop, SWT.NONE);
		stacksLabel.setText(Messages.getString("MassifToolPage.profile_stack")); //$NON-NLS-1$
		
		stacksButton = new Button(stacksTop, SWT.CHECK);
		stacksButton.addSelectionListener(selectListener);
		
		Composite depthTop = new Composite(top, SWT.NONE);
		depthTop.setLayout(new GridLayout(2, false));
		
		Label depthLabel = new Label(depthTop, SWT.NONE);
		depthLabel.setText(Messages.getString("MassifToolPage.allocation_tree_depth")); //$NON-NLS-1$
		
		depthSpinner = new Spinner(depthTop, SWT.BORDER);		
		depthSpinner.addModifyListener(modifyListener);
		
		Composite thresholdTop = new Composite(top, SWT.NONE);
		thresholdTop.setLayout(new GridLayout(3, false));
		
		Label thresholdLabel = new Label(thresholdTop, SWT.NONE);
		thresholdLabel.setText(Messages.getString("MassifToolPage.heap_allocation_threshold")); //$NON-NLS-1$
		
		thresholdSpinner = new Spinner(thresholdTop, SWT.BORDER);
		thresholdSpinner.setDigits(1);
		thresholdSpinner.setMaximum(1000);
		thresholdSpinner.addModifyListener(modifyListener);
		
		Label thresholdPercentLabel = new Label(thresholdTop, SWT.NONE);
		thresholdPercentLabel.setText("%"); //$NON-NLS-1$
		
		Composite peakInaccuracyTop = new Composite(top, SWT.NONE);
		peakInaccuracyTop.setLayout(new GridLayout(3, false));
		
		Label peakInaccuracyLabel = new Label(peakInaccuracyTop, SWT.NONE);
		peakInaccuracyLabel.setText(Messages.getString("MassifToolPage.allocation_peak_inaccuracy")); //$NON-NLS-1$
		
		peakInaccuracySpinner = new Spinner(peakInaccuracyTop, SWT.BORDER);
		peakInaccuracySpinner.setDigits(1);
		peakInaccuracySpinner.setMaximum(1000);
		peakInaccuracySpinner.addModifyListener(modifyListener);
		
		Label peakInaccuracyPercentLabel = new Label(peakInaccuracyTop, SWT.NONE);
		peakInaccuracyPercentLabel.setText("%"); //$NON-NLS-1$
		
		Composite timeUnitTop = new Composite(top, SWT.NONE);
		timeUnitTop.setLayout(new GridLayout(2, false));
		
		Label timeUnitLabel = new Label(timeUnitTop, SWT.NONE);
		timeUnitLabel.setText(Messages.getString("MassifToolPage.time_unit")); //$NON-NLS-1$
		
		timeUnitCombo = new Combo(timeUnitTop, SWT.READ_ONLY);
		String[] items = new String[] { Messages.getString("MassifToolPage.instructions"), Messages.getString("MassifToolPage.milliseconds"), Messages.getString("MassifToolPage.bytes") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		timeUnitCombo.setItems(items);
		timeUnitCombo.addSelectionListener(selectListener);
		
		Composite detailedFreqTop = new Composite(top, SWT.NONE);
		detailedFreqTop.setLayout(new GridLayout(2, false));
		
		Label detailedFreqLabel = new Label(detailedFreqTop, SWT.NONE);
		detailedFreqLabel.setText(Messages.getString("MassifToolPage.detailed_snapshot_freq")); //$NON-NLS-1$
		
		detailedFreqSpinner = new Spinner(detailedFreqTop, SWT.BORDER);
		detailedFreqSpinner.setMaximum(Integer.MAX_VALUE);
		detailedFreqSpinner.addModifyListener(modifyListener);
		
		Composite maxSnapshotsTop = new Composite(top, SWT.NONE);
		maxSnapshotsTop.setLayout(new GridLayout(2, false));
		
		Label maxSnapshotsLabel = new Label(maxSnapshotsTop, SWT.NONE);
		maxSnapshotsLabel.setText(Messages.getString("MassifToolPage.max_snapshots")); //$NON-NLS-1$
		
		maxSnapshotsSpinner = new Spinner(maxSnapshotsTop, SWT.BORDER);
		maxSnapshotsSpinner.setMaximum(Integer.MAX_VALUE);
		maxSnapshotsSpinner.addModifyListener(modifyListener);
		
		Composite alignmentTop = new Composite(top, SWT.NONE);
		alignmentTop.setLayout(new GridLayout(2, false));
		
		Label alignmentLabel = new Label(alignmentTop, SWT.NONE);
		alignmentLabel.setText(Messages.getString("MassifToolPage.minimum_heap_block")); //$NON-NLS-1$
		
		alignmentSpinner = new Spinner(alignmentTop, SWT.BORDER);
		alignmentSpinner.setMinimum(8);
		alignmentSpinner.setMaximum(4096);
		alignmentSpinner.addModifyListener(modifyListener);
		
		Composite allocFnTop = new Composite(top, SWT.NONE);
		allocFnTop.setLayout(new GridLayout(3, false));
		allocFnTop.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
		
		Label allocFnLabel = new Label(allocFnTop, SWT.NONE);
		allocFnLabel.setText(Messages.getString("MassifToolPage.allocation_functions")); //$NON-NLS-1$
		allocFnLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
				
		createAllocFnControls(allocFnTop);
		
		sc.setContent(top);
		sc.setMinSize(top.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	private void createAllocFnControls(Composite top) {
			
		allocFnList = new List(top, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);		
		FontMetrics fm = MassifPlugin.getFontMetrics(allocFnList);
		allocFnList.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fm, 50), Dialog.convertHeightInCharsToPixels(fm, 5)));
		
		Composite allocButtons = new Composite(top, SWT.NONE);
		GridLayout allocButtonsLayout = new GridLayout();
		allocButtonsLayout.marginWidth = allocButtonsLayout.marginHeight = 0;
		allocButtons.setLayout(allocButtonsLayout);
		allocButtons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		
		Button newButton = new Button(allocButtons, SWT.PUSH);
		newButton.setText(Messages.getString("MassifToolPage.New")); //$NON-NLS-1$
		newButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		newButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleNewButtonPressed();
				updateLaunchConfigurationDialog();
			}			
		});
				
		Button removeButton = new Button(allocButtons, SWT.PUSH);
		removeButton.setText(Messages.getString("MassifToolPage.Remove")); //$NON-NLS-1$
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleRemoveButtonPressed();
				updateLaunchConfigurationDialog();
			}			
		});
	}

	protected void handleNewButtonPressed() {
		InputDialog dialog = new InputDialog(getShell(), Messages.getString("MassifToolPage.New_Allocation_Function"), Messages.getString("MassifToolPage.Function_name"), "", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (dialog.open() == Window.OK) {
			String function = dialog.getValue();
			if (!function.equals("")) { //$NON-NLS-1$
				allocFnList.add(function);
			}
		}
	}

	protected void handleRemoveButtonPressed() {
		 int[] selections = allocFnList.getSelectionIndices();
		 allocFnList.remove(selections);
	}

	public String getName() {
		return Messages.getString("MassifToolPage.Massif_Options"); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	public void initializeFrom(ILaunchConfiguration configuration) {
		isInitializing = true;
		try {
			heapButton.setSelection(configuration.getAttribute(ATTR_MASSIF_HEAP, true));
			heapAdminSpinner.setSelection(configuration.getAttribute(ATTR_MASSIF_HEAPADMIN, 8));
			stacksButton.setSelection(configuration.getAttribute(ATTR_MASSIF_STACKS, false));
			depthSpinner.setSelection(configuration.getAttribute(ATTR_MASSIF_DEPTH, 30));
			java.util.List<String> allocFns = configuration.getAttribute(ATTR_MASSIF_ALLOCFN, Collections.EMPTY_LIST);
			allocFnList.setItems(allocFns.toArray(new String[allocFns.size()]));
			thresholdSpinner.setSelection(configuration.getAttribute(ATTR_MASSIF_THRESHOLD, 10));
			peakInaccuracySpinner.setSelection(configuration.getAttribute(ATTR_MASSIF_PEAKINACCURACY, 10));
			String timeUnit = configuration.getAttribute(ATTR_MASSIF_TIMEUNIT, TIME_I);
			if (timeUnit.equals(TIME_I)) {
				timeUnitCombo.select(0);
			}
			else if (timeUnit.equals(TIME_MS)) {
				timeUnitCombo.select(1);
			}
			else {
				timeUnitCombo.select(2);
			}
			detailedFreqSpinner.setSelection(configuration.getAttribute(ATTR_MASSIF_DETAILEDFREQ, 10));
			maxSnapshotsSpinner.setSelection(configuration.getAttribute(ATTR_MASSIF_MAXSNAPSHOTS, 100));
			int alignment = configuration.getAttribute(ATTR_MASSIF_ALIGNMENT, 8);
			alignmentSpinner.setSelection(alignment);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		isInitializing = false;
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_MASSIF_HEAP, heapButton.getSelection());
		configuration.setAttribute(ATTR_MASSIF_HEAPADMIN, heapAdminSpinner.getSelection());
		configuration.setAttribute(ATTR_MASSIF_STACKS, stacksButton.getSelection());
		configuration.setAttribute(ATTR_MASSIF_DEPTH, depthSpinner.getSelection());
		configuration.setAttribute(ATTR_MASSIF_ALLOCFN, Arrays.asList(allocFnList.getItems()));
		configuration.setAttribute(ATTR_MASSIF_THRESHOLD, thresholdSpinner.getSelection());
		configuration.setAttribute(ATTR_MASSIF_PEAKINACCURACY, peakInaccuracySpinner.getSelection());
		int ix = timeUnitCombo.getSelectionIndex();
		String value;
		if (ix == 0) {
			value = TIME_I;
		}
		else if (ix == 1) {
			value = TIME_MS;
		}
		else {
			value = TIME_B;
		}
		configuration.setAttribute(ATTR_MASSIF_TIMEUNIT, value);
		configuration.setAttribute(ATTR_MASSIF_DETAILEDFREQ, detailedFreqSpinner.getSelection());
		configuration.setAttribute(ATTR_MASSIF_MAXSNAPSHOTS, maxSnapshotsSpinner.getSelection());
		configuration.setAttribute(ATTR_MASSIF_ALIGNMENT, alignmentSpinner.getSelection());
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_MASSIF_HEAP, true);
		configuration.setAttribute(ATTR_MASSIF_HEAPADMIN, 8);
		configuration.setAttribute(ATTR_MASSIF_STACKS, false);
		configuration.setAttribute(ATTR_MASSIF_DEPTH, 30);
		configuration.setAttribute(ATTR_MASSIF_ALLOCFN, Collections.EMPTY_LIST);
		configuration.setAttribute(ATTR_MASSIF_THRESHOLD, 10);
		configuration.setAttribute(ATTR_MASSIF_PEAKINACCURACY, 10);
		configuration.setAttribute(ATTR_MASSIF_TIMEUNIT, TIME_I);
		configuration.setAttribute(ATTR_MASSIF_DETAILEDFREQ, 10);
		configuration.setAttribute(ATTR_MASSIF_MAXSNAPSHOTS, 100);
		configuration.setAttribute(ATTR_MASSIF_ALIGNMENT, 8);
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
