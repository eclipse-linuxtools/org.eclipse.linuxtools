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
package org.eclipse.linuxtools.internal.valgrind.massif;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.valgrind.launch.LaunchConfigurationConstants;
import org.eclipse.linuxtools.valgrind.launch.IValgrindToolPage;
import org.eclipse.swt.SWT;
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
import org.osgi.framework.Version;

public class MassifToolPage extends AbstractLaunchConfigurationTab
        implements IValgrindToolPage {
    public static final String TIME_B_STRING = Messages.getString("MassifToolPage.bytes"); //$NON-NLS-1$
    public static final String TIME_MS_STRING = Messages.getString("MassifToolPage.milliseconds"); //$NON-NLS-1$
    public static final String TIME_I_STRING = Messages.getString("MassifToolPage.instructions"); //$NON-NLS-1$
    public static final String MASSIF = "massif"; //$NON-NLS-1$
    public static final String PLUGIN_ID = MassifPlugin.PLUGIN_ID;
    private static final Version VER_3_6_0 = new Version(3, 6, 0);

    // Massif controls
    private Button heapButton;
    private Spinner heapAdminSpinner;
    private Button stacksButton;
    private Spinner depthSpinner;
    private List allocFnList;
    private List ignoreFnList;
    private Spinner thresholdSpinner;
    private Spinner peakInaccuracySpinner;
    private Combo timeUnitCombo;
    private Spinner detailedFreqSpinner;
    private Spinner maxSnapshotsSpinner;
    private Button alignmentButton;
    private Spinner alignmentSpinner;

    // VG >= 3.6.0
    private Button pagesasheapButton;

    private boolean isInitializing = false;
    private Version valgrindVersion;

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

        GridLayout topLayout = new GridLayout(2, true);
        topLayout.horizontalSpacing = 10;
        top.setLayout(topLayout);
        top.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite heapTop = new Composite(top, SWT.NONE);
        heapTop.setLayout(new GridLayout(2, false));

        heapButton = new Button(heapTop, SWT.CHECK);
        heapButton.setText(Messages.getString("MassifToolPage.profile_heap")); //$NON-NLS-1$
        heapButton.addSelectionListener(selectListener);

        Composite heapAdminTop = new Composite(top, SWT.NONE);
        heapAdminTop.setLayout(new GridLayout(2, false));

        Label heapAdminLabel = new Label(heapAdminTop, SWT.NONE);
        heapAdminLabel.setText(Messages.getString("MassifToolPage.administrative_bytes")); //$NON-NLS-1$

        heapAdminSpinner = new Spinner(heapAdminTop, SWT.BORDER);
        heapAdminSpinner.addModifyListener(modifyListener);

        Composite stacksTop = new Composite(top, SWT.NONE);
        stacksTop.setLayout(new GridLayout(2, false));

        stacksButton = new Button(stacksTop, SWT.CHECK);
        stacksButton.setText(Messages.getString("MassifToolPage.profile_stack")); //$NON-NLS-1$
        stacksButton.addSelectionListener(selectListener);

        if (valgrindVersion == null || valgrindVersion.compareTo(VER_3_6_0) >= 0) {
            Composite pagesasheapTop = new Composite(top, SWT.NONE);
            pagesasheapTop.setLayout(new GridLayout(2, false));

            pagesasheapButton = new Button(pagesasheapTop, SWT.CHECK);
            pagesasheapButton.setText(Messages.getString("MassifToolPage.profile_pagesasheap")); //$NON-NLS-1$
            pagesasheapButton.addSelectionListener(selectListener);
        }

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
        String[] items = new String[] { TIME_I_STRING, TIME_MS_STRING, TIME_B_STRING };
        timeUnitCombo.setItems(items);
        timeUnitCombo.addSelectionListener(selectListener);

        Composite detailedFreqTop = new Composite(top, SWT.NONE);
        detailedFreqTop.setLayout(new GridLayout(2, false));

        Label detailedFreqLabel = new Label(detailedFreqTop, SWT.NONE);
        detailedFreqLabel.setText(Messages.getString("MassifToolPage.detailed_snapshot_freq")); //$NON-NLS-1$

        detailedFreqSpinner = new Spinner(detailedFreqTop, SWT.BORDER);
        detailedFreqSpinner.setMinimum(1);
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
        GridLayout alignmentLayout = new GridLayout(2, false);
        alignmentLayout.marginWidth = alignmentLayout.marginHeight = 0;
        alignmentTop.setLayout(alignmentLayout);
        alignmentButton = new Button(alignmentTop, SWT.CHECK);
        alignmentButton.setText(Messages.getString("MassifToolPage.minimum_heap_block")); //$NON-NLS-1$
        alignmentButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                checkAlignmentEnablement();
                updateLaunchConfigurationDialog();
            }
        });
        alignmentSpinner = new Spinner(alignmentTop, SWT.BORDER);
        alignmentSpinner.setMinimum(0);
        alignmentSpinner.setMaximum(4096);
        alignmentSpinner.addModifyListener(modifyListener);

        Composite allocFnTop = new Composite(top, SWT.NONE);
        allocFnTop.setLayout(new GridLayout(3, false));
        allocFnTop.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));

        Label allocFnLabel = new Label(allocFnTop, SWT.NONE);
        allocFnLabel.setText(Messages.getString("MassifToolPage.allocation_functions")); //$NON-NLS-1$
        allocFnLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

        createAllocFnControls(allocFnTop);

        Composite ignoreFnTop = new Composite(top, SWT.NONE);
        ignoreFnTop.setLayout(new GridLayout(3, false));
        ignoreFnTop.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));

        Label ignoreFnLabel = new Label(ignoreFnTop, SWT.NONE);
        ignoreFnLabel.setText(Messages.getString("MassifToolPage.ignore_functions")); //$NON-NLS-1$
        ignoreFnLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

        createIgnoreFnControls(ignoreFnTop);


    }

    private void checkAlignmentEnablement() {
        alignmentSpinner.setEnabled(alignmentButton.getSelection());
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
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleAllocNewButtonPressed();
                updateLaunchConfigurationDialog();
            }
        });

        Button removeButton = new Button(allocButtons, SWT.PUSH);
        removeButton.setText(Messages.getString("MassifToolPage.Remove")); //$NON-NLS-1$
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleAllocRemoveButtonPressed();
                updateLaunchConfigurationDialog();
            }
        });
    }

    private void createIgnoreFnControls(Composite top) {

        ignoreFnList = new List(top, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        FontMetrics fm = MassifPlugin.getFontMetrics(ignoreFnList);
        ignoreFnList.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fm, 50), Dialog.convertHeightInCharsToPixels(fm, 5)));

        Composite ignoreButtons = new Composite(top, SWT.NONE);
        GridLayout ignoreButtonsLayout = new GridLayout();
        ignoreButtonsLayout.marginWidth = ignoreButtonsLayout.marginHeight = 0;
        ignoreButtons.setLayout(ignoreButtonsLayout);
        ignoreButtons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

        Button newButton = new Button(ignoreButtons, SWT.PUSH);
        newButton.setText(Messages.getString("MassifToolPage.New")); //$NON-NLS-1$
        newButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        newButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleIgnoreNewButtonPressed();
                updateLaunchConfigurationDialog();
            }
        });

        Button removeButton = new Button(ignoreButtons, SWT.PUSH);
        removeButton.setText(Messages.getString("MassifToolPage.Remove")); //$NON-NLS-1$
        removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleIgnoreRemoveButtonPressed();
                updateLaunchConfigurationDialog();
            }
        });
    }

    private void handleAllocNewButtonPressed() {
        InputDialog dialog = new InputDialog(getShell(), Messages.getString("MassifToolPage.New_Allocation_Function"), Messages.getString("MassifToolPage.Function_name"), "", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (dialog.open() == Window.OK) {
            String function = dialog.getValue();
            if (!function.equals("")) { //$NON-NLS-1$
                allocFnList.add(function);
            }
        }
    }

    private void handleAllocRemoveButtonPressed() {
         int[] selections = allocFnList.getSelectionIndices();
         allocFnList.remove(selections);
    }

    private void handleIgnoreNewButtonPressed() {
        InputDialog dialog = new InputDialog(getShell(), Messages.getString("MassifToolPage.New_Ignore_Function"), Messages.getString("MassifToolPage.Function_name"), "", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (dialog.open() == Window.OK) {
            String function = dialog.getValue();
            if (!function.equals("")) { //$NON-NLS-1$
                ignoreFnList.add(function);
            }
        }
    }

    private void handleIgnoreRemoveButtonPressed() {
         int[] selections = ignoreFnList.getSelectionIndices();
         ignoreFnList.remove(selections);
    }

    @Override
    public String getName() {
        return Messages.getString("MassifToolPage.Massif_Options"); //$NON-NLS-1$
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        isInitializing = true;
        try {
            heapButton.setSelection(configuration.getAttribute(MassifLaunchConstants.ATTR_MASSIF_HEAP, MassifLaunchConstants.DEFAULT_MASSIF_HEAP));
            heapAdminSpinner.setSelection(configuration.getAttribute(MassifLaunchConstants.ATTR_MASSIF_HEAPADMIN, MassifLaunchConstants.DEFAULT_MASSIF_HEAPADMIN));
            stacksButton.setSelection(configuration.getAttribute(MassifLaunchConstants.ATTR_MASSIF_STACKS, MassifLaunchConstants.DEFAULT_MASSIF_STACKS));
            depthSpinner.setSelection(configuration.getAttribute(MassifLaunchConstants.ATTR_MASSIF_DEPTH, MassifLaunchConstants.DEFAULT_MASSIF_DEPTH));
            java.util.List<String> allocFns = configuration.getAttribute(MassifLaunchConstants.ATTR_MASSIF_ALLOCFN, MassifLaunchConstants.DEFAULT_MASSIF_ALLOCFN);
            allocFnList.setItems(allocFns.toArray(new String[allocFns.size()]));
            java.util.List<String> ignoreFns = configuration.getAttribute(MassifLaunchConstants.ATTR_MASSIF_IGNOREFN, MassifLaunchConstants.DEFAULT_MASSIF_IGNOREFN);
            ignoreFnList.setItems(ignoreFns.toArray(new String[ignoreFns.size()]));
            thresholdSpinner.setSelection(configuration.getAttribute(MassifLaunchConstants.ATTR_MASSIF_THRESHOLD, MassifLaunchConstants.DEFAULT_MASSIF_THRESHOLD));
            peakInaccuracySpinner.setSelection(configuration.getAttribute(MassifLaunchConstants.ATTR_MASSIF_PEAKINACCURACY, MassifLaunchConstants.DEFAULT_MASSIF_PEAKINACCURACY));

            String timeUnit = configuration.getAttribute(MassifLaunchConstants.ATTR_MASSIF_TIMEUNIT, MassifLaunchConstants.DEFAULT_MASSIF_TIMEUNIT);
            if (timeUnit.equals(MassifLaunchConstants.TIME_I)) {
                timeUnitCombo.select(0);
            } else if (timeUnit.equals(MassifLaunchConstants.TIME_MS)) {
                timeUnitCombo.select(1);
            } else {
                timeUnitCombo.select(2);
            }

            detailedFreqSpinner.setSelection(configuration.getAttribute(MassifLaunchConstants.ATTR_MASSIF_DETAILEDFREQ, MassifLaunchConstants.DEFAULT_MASSIF_DETAILEDFREQ));
            maxSnapshotsSpinner.setSelection(configuration.getAttribute(MassifLaunchConstants.ATTR_MASSIF_MAXSNAPSHOTS, MassifLaunchConstants.DEFAULT_MASSIF_MAXSNAPSHOTS));
            alignmentButton.setSelection(configuration.getAttribute(MassifLaunchConstants.ATTR_MASSIF_ALIGNMENT_BOOL, MassifLaunchConstants.DEFAULT_MASSIF_ALIGNMENT_BOOL));
            checkAlignmentEnablement();
            int alignment = configuration.getAttribute(MassifLaunchConstants.ATTR_MASSIF_ALIGNMENT_VAL, MassifLaunchConstants.DEFAULT_MASSIF_ALIGNMENT_VAL);
            alignmentSpinner.setSelection(alignment);

            // VG >= 3.6.0
            if (valgrindVersion == null || valgrindVersion.compareTo(VER_3_6_0) >= 0) {
                pagesasheapButton.setSelection(configuration.getAttribute(MassifLaunchConstants.ATTR_MASSIF_PAGESASHEAP, MassifLaunchConstants.DEFAULT_MASSIF_PAGESASHEAP));
            }
        } catch (CoreException e) {
            e.printStackTrace();
        }
        isInitializing = false;
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_HEAP, heapButton.getSelection());
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_HEAPADMIN, heapAdminSpinner.getSelection());
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_STACKS, stacksButton.getSelection());
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_DEPTH, depthSpinner.getSelection());
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_ALLOCFN, Arrays.asList(allocFnList.getItems()));
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_IGNOREFN, Arrays.asList(ignoreFnList.getItems()));
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_THRESHOLD, thresholdSpinner.getSelection());
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_PEAKINACCURACY, peakInaccuracySpinner.getSelection());
        int ix = timeUnitCombo.getSelectionIndex();
        String value;
        if (ix == 0) {
            value = MassifLaunchConstants.TIME_I;
        } else if (ix == 1) {
            value = MassifLaunchConstants.TIME_MS;
        } else {
            value = MassifLaunchConstants.TIME_B;
        }
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_TIMEUNIT, value);
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_DETAILEDFREQ, detailedFreqSpinner.getSelection());
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_MAXSNAPSHOTS, maxSnapshotsSpinner.getSelection());
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_ALIGNMENT_BOOL, alignmentButton.getSelection());
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_ALIGNMENT_VAL, alignmentSpinner.getSelection());

        // VG >= 3.6.0
        if (valgrindVersion == null || valgrindVersion.compareTo(VER_3_6_0) >= 0) {
            configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_PAGESASHEAP, pagesasheapButton.getSelection());
        }
    }

    @Override
    public boolean isValid(ILaunchConfiguration launchConfig) {
        setErrorMessage(null);

        boolean result = false;
        try {
            int alignment = launchConfig.getAttribute(MassifLaunchConstants.ATTR_MASSIF_ALIGNMENT_VAL, MassifLaunchConstants.DEFAULT_MASSIF_ALIGNMENT_VAL);
            result = (alignment & (alignment - 1)) == 0; // is power of two?
        } catch (CoreException e) {
            e.printStackTrace();
        }

        if (!result) {
            setErrorMessage(Messages.getString("MassifToolPage.Alignment_must_be_power_2")); //$NON-NLS-1$
        }
        return result;
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(LaunchConfigurationConstants.ATTR_TOOL, MassifPlugin.TOOL_ID);
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_HEAP, MassifLaunchConstants.DEFAULT_MASSIF_HEAP);
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_HEAPADMIN, MassifLaunchConstants.DEFAULT_MASSIF_HEAPADMIN);
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_STACKS, MassifLaunchConstants.DEFAULT_MASSIF_STACKS);
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_DEPTH, MassifLaunchConstants.DEFAULT_MASSIF_DEPTH);
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_ALLOCFN, MassifLaunchConstants.DEFAULT_MASSIF_ALLOCFN);
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_THRESHOLD, MassifLaunchConstants.DEFAULT_MASSIF_THRESHOLD);
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_PEAKINACCURACY, MassifLaunchConstants.DEFAULT_MASSIF_PEAKINACCURACY);
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_TIMEUNIT, MassifLaunchConstants.DEFAULT_MASSIF_TIMEUNIT);
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_DETAILEDFREQ, MassifLaunchConstants.DEFAULT_MASSIF_DETAILEDFREQ);
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_MAXSNAPSHOTS, MassifLaunchConstants.DEFAULT_MASSIF_MAXSNAPSHOTS);
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_ALIGNMENT_BOOL, MassifLaunchConstants.DEFAULT_MASSIF_ALIGNMENT_BOOL);
        configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_ALIGNMENT_VAL, MassifLaunchConstants.DEFAULT_MASSIF_ALIGNMENT_VAL);

        // VG >= 3.6.0
        if (valgrindVersion == null || valgrindVersion.compareTo(VER_3_6_0) >= 0) {
            configuration.setAttribute(MassifLaunchConstants.ATTR_MASSIF_PAGESASHEAP, MassifLaunchConstants.DEFAULT_MASSIF_PAGESASHEAP);
        }
    }

    @Override
    public void setValgrindVersion(Version ver) {
        valgrindVersion = ver;
    }

    @Override
    protected void updateLaunchConfigurationDialog() {
        if (!isInitializing) {
            super.updateLaunchConfigurationDialog();
        }
    }

    public Button getHeapButton() {
        return heapButton;
    }

    public Spinner getHeapAdminSpinner() {
        return heapAdminSpinner;
    }

    public Button getStacksButton() {
        return stacksButton;
    }

    public Button getPageasheapButton() {
        return pagesasheapButton;
    }

    public Spinner getDepthSpinner() {
        return depthSpinner;
    }

    public List getAllocFnList() {
        return allocFnList;
    }

    public List getIgnoreFnList() {
        return ignoreFnList;
    }

    public Spinner getThresholdSpinner() {
        return thresholdSpinner;
    }

    public Spinner getPeakInaccuracySpinner() {
        return peakInaccuracySpinner;
    }

    public Combo getTimeUnitCombo() {
        return timeUnitCombo;
    }

    public Spinner getDetailedFreqSpinner() {
        return detailedFreqSpinner;
    }

    public Spinner getMaxSnapshotsSpinner() {
        return maxSnapshotsSpinner;
    }

    public Button getAlignmentButton() {
        return alignmentButton;
    }

    public Spinner getAlignmentSpinner() {
        return alignmentSpinner;
    }

}
