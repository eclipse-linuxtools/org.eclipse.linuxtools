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
package org.eclipse.linuxtools.internal.valgrind.memcheck;

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
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Version;

public class MemcheckToolPage extends AbstractLaunchConfigurationTab implements IValgrindToolPage {
	private static final Version VER_3_4_0 = new Version(3, 4, 0);
	private static final Version VER_3_6_0 = new Version(3, 6, 0);
	
	// MEMCHECK controls
	private Button leakCheckButton;
	private Combo leakResCombo;
	private Button showReachableButton;
	private Spinner freelistSpinner;
	private Button partialLoadsButton;
	private Button undefValueButton;
	private Button gccWorkaroundButton;
	private Button alignmentButton;
	private Spinner alignmentSpinner;
	private Button mallocFillButton;
	private Text mallocFillText;
	private Button freeFillButton;
	private Text freeFillText;
	private List ignoreRangesList;
	
	// VG >= 3.4.0
	private Button trackOriginsButton;
	
	// VG >= 3.6.0
	private Button showPossiblyLostButton;
	
	private boolean isInitializing = false;
	private Version valgrindVersion;
	private CoreException ex = null;
	
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

		// VG >= 3.4.0
		if (valgrindVersion == null || valgrindVersion.compareTo(VER_3_4_0) >= 0) {
			trackOriginsButton = new Button(top, SWT.CHECK);
			trackOriginsButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			trackOriginsButton.setText(Messages.getString("MemcheckToolPage.Track_origins")); //$NON-NLS-1$
			trackOriginsButton.addSelectionListener(selectListener);
		}
		
		gccWorkaroundButton = new Button(top, SWT.CHECK);
		gccWorkaroundButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		gccWorkaroundButton.setText(Messages.getString("MemcheckToolPage.gcc_296_workarounds")); //$NON-NLS-1$
		gccWorkaroundButton.addSelectionListener(selectListener);
				
		Composite alignmentTop = new Composite(top, SWT.NONE);
		GridLayout alignmentLayout = new GridLayout(2, false);
		alignmentLayout.marginWidth = alignmentLayout.marginHeight = 0;
		alignmentTop.setLayout(alignmentLayout);
		alignmentTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		alignmentButton = new Button(alignmentTop, SWT.CHECK);
		alignmentButton.setText(Messages.getString("MemcheckToolPage.minimum_heap_block")); //$NON-NLS-1$
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

		// VG >= 3.6.0
		if (valgrindVersion == null || valgrindVersion.compareTo(VER_3_6_0) >= 0) {
			showPossiblyLostButton = new Button(top, SWT.CHECK);
			showPossiblyLostButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			showPossiblyLostButton.setText(Messages.getString("MemcheckToolPage.Show_Possibly_Lost")); //$NON-NLS-1$
			showPossiblyLostButton.addSelectionListener(selectListener);
		}
	
		Composite mallocFillTop = new Composite(top, SWT.NONE);
		GridLayout mallocFillLayout = new GridLayout(2, false);
		mallocFillLayout.marginWidth = mallocFillLayout.marginHeight = 0;
		mallocFillTop.setLayout(mallocFillLayout);
		mallocFillTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		mallocFillButton = new Button(mallocFillTop, SWT.CHECK);
		mallocFillButton.setText(Messages.getString("MemcheckToolPage.Malloc_Fill")); //$NON-NLS-1$
		mallocFillButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkMallocFillEnablement();
				updateLaunchConfigurationDialog();
			}
		});
		mallocFillText = new Text(mallocFillTop, SWT.BORDER);
		mallocFillText.setTextLimit(8);
		mallocFillText.addModifyListener(modifyListener);


		Composite freeFillTop = new Composite(top, SWT.NONE);
		GridLayout freeFillLayout = new GridLayout(2, false);
		freeFillLayout.marginWidth = freeFillLayout.marginHeight = 0;
		freeFillTop.setLayout(freeFillLayout);
		freeFillTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		freeFillButton = new Button(freeFillTop, SWT.CHECK);
		freeFillButton.setText(Messages.getString("MemcheckToolPage.Free_Fill")); //$NON-NLS-1$
		freeFillButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkFreeFillEnablement();
				updateLaunchConfigurationDialog();
			}
		});
		freeFillText = new Text(freeFillTop, SWT.BORDER);
		mallocFillText.setTextLimit(8);
		freeFillText.addModifyListener(modifyListener);
		
		Composite ignoreRangesTop = new Composite(top, SWT.NONE);
		ignoreRangesTop.setLayout(new GridLayout(3, false));
		ignoreRangesTop.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 2, 1));
		
		Label ignoreRangesLabel = new Label(ignoreRangesTop, SWT.NONE);
		ignoreRangesLabel.setText(Messages.getString("MemcheckToolPage.Ignore_Ranges")); //$NON-NLS-1$
		ignoreRangesLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
				
		createIgnoreRangesControls(ignoreRangesTop);
}
	
	private void createIgnoreRangesControls(Composite top) {
		
		ignoreRangesList = new List(top, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);		
		FontMetrics fm = MemcheckPlugin.getFontMetrics(ignoreRangesList);
		ignoreRangesList.setLayoutData(new GridData(Dialog.convertWidthInCharsToPixels(fm, 50), Dialog.convertHeightInCharsToPixels(fm, 5)));
		
		Composite ignoreButtons = new Composite(top, SWT.NONE);
		GridLayout ignoreButtonsLayout = new GridLayout();
		ignoreButtonsLayout.marginWidth = ignoreButtonsLayout.marginHeight = 0;
		ignoreButtons.setLayout(ignoreButtonsLayout);
		ignoreButtons.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		
		Button newButton = new Button(ignoreButtons, SWT.PUSH);
		newButton.setText(Messages.getString("MemcheckToolPage.New")); //$NON-NLS-1$
		newButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		newButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleIgnoreNewButtonPressed();
				updateLaunchConfigurationDialog();
			}			
		});
		
		Button removeButton = new Button(ignoreButtons, SWT.PUSH);
		removeButton.setText(Messages.getString("MemcheckToolPage.Remove")); //$NON-NLS-1$
		removeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleIgnoreRemoveButtonPressed();
				updateLaunchConfigurationDialog();
			}			
		});
		
	}
		
	private void handleIgnoreNewButtonPressed() {
		InputDialog dialog = new InputDialog(getShell(), Messages.getString("MemcheckToolPage.Ignore_Ranges"), Messages.getString("MemcheckToolPage.Range"), "", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (dialog.open() == Window.OK) {
			String function = dialog.getValue();
			if (!function.equals("")) { //$NON-NLS-1$
				ignoreRangesList.add(function);
			}
		}
	}

	private void handleIgnoreRemoveButtonPressed() {
		int[] selections = ignoreRangesList.getSelectionIndices();
		ignoreRangesList.remove(selections);
	}

	private void checkAlignmentEnablement() {
		alignmentSpinner.setEnabled(alignmentButton.getSelection());
	}
	
	private void checkMallocFillEnablement() {
		mallocFillText.setEnabled(mallocFillButton.getSelection());
	}
	
	private void checkFreeFillEnablement() {
		freeFillText.setEnabled(freeFillButton.getSelection());
	}
	
	@Override
	public String getName() {
		return Messages.getString("MemcheckToolPage.Memcheck_Options"); //$NON-NLS-1$
	}

	@Override
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
			alignmentButton.setSelection(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_ALIGNMENT_BOOL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_ALIGNMENT_BOOL));
			checkAlignmentEnablement();
			alignmentSpinner.setSelection(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_ALIGNMENT_VAL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_ALIGNMENT_VAL));
			
			// VG >= 3.4.0
			if (valgrindVersion == null || valgrindVersion.compareTo(VER_3_4_0) >= 0) {
				trackOriginsButton.setSelection(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_TRACKORIGINS, MemcheckLaunchConstants.DEFAULT_MEMCHECK_TRACKORIGINS));
			}

			// VG >= 3.6.0
			if (valgrindVersion == null || valgrindVersion.compareTo(VER_3_6_0) >= 0) {
				showPossiblyLostButton.setSelection(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_POSSIBLY_LOST_BOOL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_POSSIBLY_LOST_BOOL));
			}
			
			mallocFillButton.setSelection(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_MALLOCFILL_BOOL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_MALLOCFILL_BOOL));
			checkMallocFillEnablement();
			mallocFillText.setText(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_MALLOCFILL_VAL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_MALLOCFILL_VAL));
			freeFillButton.setSelection(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_FREEFILL_BOOL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_FREEFILL_BOOL));
			checkFreeFillEnablement();
			freeFillText.setText(configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_FREEFILL_VAL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_FREEFILL_VAL));
			java.util.List<String> ignoreFns = configuration.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_IGNORE_RANGES, MemcheckLaunchConstants.DEFAULT_MEMCHECK_IGNORE_RANGES);
			ignoreRangesList.setItems(ignoreFns.toArray(new String[ignoreFns.size()]));

			
		} catch (CoreException e) {
			ex = e;
		}
		isInitializing = false;
	}
	
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_LEAKCHECK, leakCheckButton.getSelection());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_LEAKRES, leakResCombo.getText());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_SHOWREACH, showReachableButton.getSelection());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_FREELIST, freelistSpinner.getSelection());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_PARTIAL, partialLoadsButton.getSelection());		
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_UNDEF, undefValueButton.getSelection());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_GCCWORK, gccWorkaroundButton.getSelection());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_ALIGNMENT_BOOL, alignmentButton.getSelection());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_ALIGNMENT_VAL, alignmentSpinner.getSelection());
		
		// VG >= 3.4.0
		if (valgrindVersion == null || valgrindVersion.compareTo(VER_3_4_0) >= 0) {
			configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_TRACKORIGINS, trackOriginsButton.getSelection());
		}
		
		// VG >= 3.6.0
		if (valgrindVersion == null || valgrindVersion.compareTo(VER_3_6_0) >= 0) {
			configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_POSSIBLY_LOST_BOOL, showPossiblyLostButton.getSelection());
		}
		
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_MALLOCFILL_BOOL, mallocFillButton.getSelection());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_MALLOCFILL_VAL, mallocFillText.getText());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_FREEFILL_BOOL, freeFillButton.getSelection());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_FREEFILL_VAL, freeFillText.getText());
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_IGNORE_RANGES, Arrays.asList(ignoreRangesList.getItems()));
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		
		boolean result = false;
		try {
			// check alignment
			int alignment = launchConfig.getAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_ALIGNMENT_VAL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_ALIGNMENT_VAL);
			result = (alignment & (alignment - 1)) == 0; // is power of two?			
			if (!result) {
				setErrorMessage(Messages.getString("MemcheckToolPage.Alignment_must_be_power_2")); //$NON-NLS-1$
			}
			else {
				// VG >= 3.4.0
				if (valgrindVersion == null || valgrindVersion.compareTo(VER_3_4_0) >= 0) {
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
	
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(LaunchConfigurationConstants.ATTR_TOOL, MemcheckPlugin.TOOL_ID);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_LEAKCHECK, MemcheckLaunchConstants.DEFAULT_MEMCHECK_LEAKCHECK);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_LEAKRES, MemcheckLaunchConstants.DEFAULT_MEMCHECK_LEAKRES);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_SHOWREACH, MemcheckLaunchConstants.DEFAULT_MEMCHECK_SHOWREACH);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_FREELIST, MemcheckLaunchConstants.DEFAULT_MEMCHECK_FREELIST);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_PARTIAL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_PARTIAL);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_UNDEF, MemcheckLaunchConstants.DEFAULT_MEMCHECK_UNDEF);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_GCCWORK, MemcheckLaunchConstants.DEFAULT_MEMCHECK_GCCWORK);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_ALIGNMENT_BOOL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_ALIGNMENT_BOOL);
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_ALIGNMENT_VAL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_ALIGNMENT_VAL);
		
		// VG >= 3.4.0
		if (valgrindVersion == null || valgrindVersion.compareTo(VER_3_4_0) >= 0) {
			configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_TRACKORIGINS, MemcheckLaunchConstants.DEFAULT_MEMCHECK_TRACKORIGINS);
		}
		
		// VG >= 3.6.0
		if (valgrindVersion == null || valgrindVersion.compareTo(VER_3_6_0) >= 0) {
			configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_POSSIBLY_LOST_BOOL, MemcheckLaunchConstants.DEFAULT_MEMCHECK_POSSIBLY_LOST_BOOL);
		}
		
		configuration.setAttribute(MemcheckLaunchConstants.ATTR_MEMCHECK_IGNORE_RANGES, MemcheckLaunchConstants.DEFAULT_MEMCHECK_IGNORE_RANGES);
	}
		
	@Override
	public void setValgrindVersion(Version ver) {
		valgrindVersion = ver;
	}

	@Override
	public void updateLaunchConfigurationDialog() {
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

	public Button getAlignmentButton() {
		return alignmentButton;
	}
	
	public Spinner getAlignmentSpinner() {
		return alignmentSpinner;
	}

	public Button getTrackOriginsButton() {
		return trackOriginsButton;
	}
}
