/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * (C) Copyright 2010 IBM Corp. 2010
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> -
 *    Thavidu Ranatunga (IBM) - derived from
 *       org.eclipse.linuxtools.oprofile.launch.configuration.OprofileSetupTab
 *******************************************************************************/
package org.eclipse.linuxtools.internal.perf.launch;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class PerfOptionsTab extends AbstractLaunchConfigurationTab {
	protected Text txtKernelLocation;
	protected Button chkRecordRealtime;
	protected Button chkRecordVerbose;
	protected Button chkSourceLineNumbers;
	protected Button chkKernelSourceLineNumbers;
	protected Button chkMultiplexEvents;
	protected Button chkModuleSymbols;
	protected Button chkHideUnresolvedSymbols;
	protected Button chkShowSourceDisassembly;
	protected Button chkShowStat;
	protected Spinner statRunCount;

	protected Composite top;
	protected ScrolledComposite scrollTop;

	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		if (txtKernelLocation != null) {
			String filename = txtKernelLocation.getText();
			if (filename.length() > 0) {
				File file = new File(filename);
				return (file.exists() && file.isFile());
			}
		}
		return true;
	}

	@Override
	public void createControl(Composite parent) {
		scrollTop = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrollTop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollTop.setExpandVertical(true);
		scrollTop.setExpandHorizontal(true);

		setControl(scrollTop);

		top = new Composite(scrollTop, SWT.NONE);
		top.setLayout(new GridLayout());

		createVerticalSpacer(top, 1);
		GridData data;

		// Kernel Selection
		Composite kernelComp = new Composite(top, SWT.NONE);
		GridLayout parallelLayout = new GridLayout(2, false);
		data = new GridData(GridData.FILL_HORIZONTAL);
		parallelLayout.marginHeight = 0;
		parallelLayout.marginWidth = 0;
		kernelComp.setLayout(parallelLayout);
		kernelComp.setLayoutData(data);

		Label kernelLabel = new Label(kernelComp, SWT.NONE);
		kernelLabel.setText(PerfPlugin.STRINGS_Kernel_Location);
		data = new GridData();
		data.horizontalSpan = 2;
		kernelLabel.setLayoutData(data);

		txtKernelLocation = new Text(kernelComp, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		txtKernelLocation.setLayoutData(data);
		txtKernelLocation.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent mev) {
				handleKernelImageFileTextModify(txtKernelLocation);
			}
		});

		Button button = createPushButton(kernelComp, "Browse", null);
		final Shell shell = top.getShell();
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent sev) {
				showFileDialog(shell);
			}
		});

		createVerticalSpacer(top, 1);

		// Create checkbox options container
		Composite chkBoxComp = new Composite(top, SWT.NONE);
		GridLayout chkBoxLayout = new GridLayout();
		chkBoxLayout.marginHeight = 0;
		chkBoxLayout.marginWidth = 0;
		chkBoxComp.setLayout(chkBoxLayout);

		chkRecordVerbose = createCheckButtonHelper(chkBoxComp, PerfPlugin.STRINGS_Record_Verbose);
		chkModuleSymbols = createCheckButtonHelper(chkBoxComp, PerfPlugin.STRINGS_ModuleSymbols);
		chkHideUnresolvedSymbols = createCheckButtonHelper(chkBoxComp, PerfPlugin.STRINGS_HideUnresolvedSymbols);
		chkSourceLineNumbers = createCheckButtonHelper(chkBoxComp, PerfPlugin.STRINGS_SourceLineNumbers);
		chkShowSourceDisassembly = createCheckButtonHelper(chkBoxComp, PerfPlugin.STRINGS_ShowSourceDisassembly);

		Composite showStatComp = new Composite(top, SWT.NONE);
		showStatComp.setLayout(parallelLayout);

		chkShowStat = createCheckButtonHelper(showStatComp, PerfPlugin.STRINGS_ShowStat);
		chkShowStat.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				handleShowStatSelection();
			}
		});
		statRunCount = new Spinner(showStatComp, SWT.BORDER);
		statRunCount.setEnabled(false);
		statRunCount.setMinimum(1);
		statRunCount.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		chkSourceLineNumbers.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				if ((chkKernelSourceLineNumbers != null) && (!chkSourceLineNumbers.getSelection())) {
					chkKernelSourceLineNumbers.setEnabled(false);
				} else {
					chkKernelSourceLineNumbers.setEnabled(true);
				}
			}
		});
		chkKernelSourceLineNumbers = createCheckButtonHelper(chkBoxComp, PerfPlugin.STRINGS_Kernel_SourceLineNumbers);
		chkRecordRealtime = createCheckButtonHelper(chkBoxComp, PerfPlugin.STRINGS_Record_Realtime);
		chkMultiplexEvents = createCheckButtonHelper(chkBoxComp, PerfPlugin.STRINGS_Multiplex);

		scrollTop.setContent(top);
		recomputeSize();
		updateLaunchConfigurationDialog();
	}

	protected void recomputeSize() {
		Point point = top.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		top.setSize(point);
		scrollTop.setMinSize(point);
	}

	// Helper function for creating buttons.
	private Button createCheckButtonHelper(Composite parent, String label) {
		final Button b = new Button(parent, SWT.CHECK);
		b.setText(label);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		});
		return b;
	}

	// handles text modification event for kernel file location
	private void handleKernelImageFileTextModify(Text text) {
		String errorMessage = null;
		String filename = text.getText();

		if (filename.length() > 0) {
			File file = new File(filename);
			if (!file.exists() || !file.isFile()) {
				errorMessage = "The entered location does not exist.";
			}
		}

		// Update dialog and error message
		setErrorMessage(errorMessage);
		updateLaunchConfigurationDialog();
	}

	/**
	 * Handle selection of show stat button
	 */
	private void handleShowStatSelection() {
		if (chkShowStat.getSelection()) {
			statRunCount.setEnabled(true);
			toggleButtonsEnablement(false);
		} else {
			statRunCount.setEnabled(false);
			toggleButtonsEnablement(true);
		}
	}

	// Displays a file dialog to allow the user to select the kernel image file
	private void showFileDialog(Shell shell) {
		FileDialog fDialog = new FileDialog(shell, SWT.OPEN);
		File kernel = new File(txtKernelLocation.getText());
		if (!kernel.exists()) {
			kernel = new File("/boot"); //$NON-NLS-1$
			if (!kernel.exists()) {
				kernel = new File("/"); //$NON-NLS-1$
			}
		}
		fDialog.setFileName(kernel.toString());
		fDialog.setText("Select location of kernel image file");
		String newKernel = fDialog.open();
		if (newKernel != null) {
			kernel = new File(newKernel);
			if (!kernel.exists()) {
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
				mb.setMessage("File does not exist");
				switch (mb.open()) {
					case SWT.RETRY:
						showFileDialog(shell);
						break;
					default:
				}
			} else {
				txtKernelLocation.setText(newKernel);
			}
		}
	}

	/**
	 * Toggle enablement of all buttons, excluding the stat button.
	 * @param enable enablement of buttons
	 */
	public void toggleButtonsEnablement(boolean enable) {
		txtKernelLocation.setEnabled(enable);
		chkRecordRealtime.setEnabled(enable);
		chkRecordVerbose.setEnabled(enable);
		chkSourceLineNumbers.setEnabled(enable);
		chkKernelSourceLineNumbers.setEnabled(enable);
		chkMultiplexEvents.setEnabled(enable);
		chkModuleSymbols.setEnabled(enable);
		chkHideUnresolvedSymbols.setEnabled(enable);
		chkShowSourceDisassembly.setEnabled(enable);
	}

	@Override
	public String getName() {
		return "Perf Options";
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {

		try {
			txtKernelLocation.setText(config.getAttribute(PerfPlugin.ATTR_Kernel_Location, PerfPlugin.ATTR_Kernel_Location_default));
			chkRecordRealtime.setSelection(config.getAttribute(PerfPlugin.ATTR_Record_Realtime, PerfPlugin.ATTR_Record_Realtime_default));
			chkRecordVerbose.setSelection(config.getAttribute(PerfPlugin.ATTR_Record_Verbose, PerfPlugin.ATTR_Record_Verbose_default));
			chkSourceLineNumbers.setSelection(config.getAttribute(PerfPlugin.ATTR_SourceLineNumbers, PerfPlugin.ATTR_SourceLineNumbers_default));
			chkKernelSourceLineNumbers.setSelection(config.getAttribute(PerfPlugin.ATTR_Kernel_SourceLineNumbers, PerfPlugin.ATTR_Kernel_SourceLineNumbers_default));

			chkMultiplexEvents.setSelection(config.getAttribute(PerfPlugin.ATTR_Multiplex, PerfPlugin.ATTR_Multiplex_default));
			chkModuleSymbols.setSelection(config.getAttribute(PerfPlugin.ATTR_ModuleSymbols, PerfPlugin.ATTR_ModuleSymbols_default));
			chkHideUnresolvedSymbols.setSelection(config.getAttribute(PerfPlugin.ATTR_HideUnresolvedSymbols, PerfPlugin.ATTR_HideUnresolvedSymbols_default));
			chkShowSourceDisassembly.setSelection(config.getAttribute(PerfPlugin.ATTR_ShowSourceDisassembly, PerfPlugin.ATTR_ShowSourceDisassembly_default));
			chkShowStat.setSelection(config.getAttribute(PerfPlugin.ATTR_ShowStat, PerfPlugin.ATTR_ShowStat_default));
			int runCount = config.getAttribute(PerfPlugin.ATTR_StatRunCount, PerfPlugin.ATTR_StatRunCount_default);
			statRunCount.setSelection(runCount);
			handleShowStatSelection();
		} catch (CoreException e) {
			// do nothing
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy wconfig) {
		wconfig.setAttribute(PerfPlugin.ATTR_Kernel_Location, txtKernelLocation.getText());
		wconfig.setAttribute(PerfPlugin.ATTR_Record_Realtime, chkRecordRealtime.getSelection());
		wconfig.setAttribute(PerfPlugin.ATTR_Record_Verbose, chkRecordVerbose.getSelection());
		wconfig.setAttribute(PerfPlugin.ATTR_SourceLineNumbers, chkSourceLineNumbers.getSelection());
		wconfig.setAttribute(PerfPlugin.ATTR_Kernel_SourceLineNumbers, chkKernelSourceLineNumbers.getSelection());
		wconfig.setAttribute(PerfPlugin.ATTR_Multiplex, chkMultiplexEvents.getSelection());
		wconfig.setAttribute(PerfPlugin.ATTR_ModuleSymbols, chkModuleSymbols.getSelection());
		wconfig.setAttribute(PerfPlugin.ATTR_HideUnresolvedSymbols, chkHideUnresolvedSymbols.getSelection());
		wconfig.setAttribute(PerfPlugin.ATTR_ShowSourceDisassembly, chkShowSourceDisassembly.getSelection());
		wconfig.setAttribute(PerfPlugin.ATTR_ShowStat, chkShowStat.getSelection());
		wconfig.setAttribute(PerfPlugin.ATTR_StatRunCount, statRunCount.getSelection());
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy wconfig) {
		wconfig.setAttribute(PerfPlugin.ATTR_Kernel_Location, PerfPlugin.ATTR_Kernel_Location_default);
		wconfig.setAttribute(PerfPlugin.ATTR_Record_Realtime, PerfPlugin.ATTR_Record_Realtime_default);
		wconfig.setAttribute(PerfPlugin.ATTR_Record_Verbose, PerfPlugin.ATTR_Record_Verbose_default);
		wconfig.setAttribute(PerfPlugin.ATTR_SourceLineNumbers, PerfPlugin.ATTR_SourceLineNumbers_default);
		wconfig.setAttribute(PerfPlugin.ATTR_Kernel_SourceLineNumbers, PerfPlugin.ATTR_Kernel_SourceLineNumbers_default);
		wconfig.setAttribute(PerfPlugin.ATTR_Multiplex, PerfPlugin.ATTR_Multiplex_default);
		wconfig.setAttribute(PerfPlugin.ATTR_ModuleSymbols, PerfPlugin.ATTR_ModuleSymbols_default);
		wconfig.setAttribute(PerfPlugin.ATTR_HideUnresolvedSymbols, PerfPlugin.ATTR_HideUnresolvedSymbols_default);
		wconfig.setAttribute(PerfPlugin.ATTR_ShowSourceDisassembly, PerfPlugin.ATTR_ShowSourceDisassembly_default);
		wconfig.setAttribute(PerfPlugin.ATTR_ShowStat, PerfPlugin.ATTR_ShowStat_default);
		wconfig.setAttribute(PerfPlugin.ATTR_StatRunCount, PerfPlugin.ATTR_StatRunCount_default);
	}

}
