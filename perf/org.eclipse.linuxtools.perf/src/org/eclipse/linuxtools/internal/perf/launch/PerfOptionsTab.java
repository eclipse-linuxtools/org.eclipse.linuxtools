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
import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.linuxtools.internal.perf.PerfCore;
import org.eclipse.linuxtools.internal.perf.PerfPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class PerfOptionsTab extends AbstractLaunchConfigurationTab {
	protected Text _txtKernel_Location;
	protected Button _chkRecord_Realtime;
	protected Button _chkRecord_Verbose;
	protected Button _chkSourceLineNumbers;
	protected Button _chkKernel_SourceLineNumbers;
	protected Button _chkMultiplexEvents;
	protected Button _chkModuleSymbols;
	protected Button _chkHideUnresolvedSymbols;
	protected Exception ex;
	
	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		//return PerfPlugin.getImageDescriptor("icons/event.gif").createImage();
		return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
	}
	
	//TODO Implement more options.
	//perf record -c, count?
	//profile frequency --freq <n>	
	//--For later?----
	//record -g, call graph?
	//mmap pages?
	//child tasks inherit counts?
	//--stat, per thread counts
	
	/*// hm this flag doesn't seem to actually do anything.
	public boolean canSave() {
		return isValid(); // probably not best practice but for this case the two are the same.
	}*/
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);

		if (ex != null) {
			setErrorMessage(ex.getLocalizedMessage());
			return false;
		}
		
		if (_txtKernel_Location != null) {
			String filename = _txtKernel_Location.getText();
			if (filename.length() > 0) {
				File file = new File(filename);
				return (file.exists() && file.isFile());
			}
		}
		return true;
	}
	
	//Function adapted from org.eclipse.linuxtools.oprofile.launch.configuration.OprofileSetupTab.java
	@Override
	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		setControl(top);
		top.setLayout(new GridLayout());

		GridData data;
		GridLayout layout;
		createVerticalSpacer(top, 1);

		// Create container for kernel image file selection
		Composite p = new Composite(top, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		p.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		p.setLayoutData(data);

		Label l = new Label(p, SWT.NONE);
		l.setText(PerfPlugin.STRINGS_Kernel_Location); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 2;
		l.setLayoutData(data);

		_txtKernel_Location = new Text(p, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		_txtKernel_Location.setLayoutData(data);
		_txtKernel_Location.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent mev) {
				_handleKernelImageFileTextModify(_txtKernel_Location);
			}
		});

		Button button = createPushButton(p, "Browse", null); //$NON-NLS-1$
		final Shell shell = top.getShell();
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent sev) {
				_showFileDialog(shell);
			}
		});

		createVerticalSpacer(top, 1);

		// Create checkbox options container
		p = new Composite(top, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		p.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		p.setLayoutData(data);
		
		
		_chkRecord_Verbose = _createCheckButton(p, PerfPlugin.STRINGS_Record_Verbose);
		_chkModuleSymbols = _createCheckButton(p, PerfPlugin.STRINGS_ModuleSymbols);
		_chkHideUnresolvedSymbols = _createCheckButton(p, PerfPlugin.STRINGS_HideUnresolvedSymbols);
		_chkSourceLineNumbers = _createCheckButton(p, PerfPlugin.STRINGS_SourceLineNumbers);
		_chkSourceLineNumbers.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				if ((_chkKernel_SourceLineNumbers != null) && (!_chkSourceLineNumbers.getSelection())) {
					_chkKernel_SourceLineNumbers.setEnabled(false);
				} else {
					_chkKernel_SourceLineNumbers.setEnabled(true);
				}
			}
		});
		_chkKernel_SourceLineNumbers = _createCheckButton(p, PerfPlugin.STRINGS_Kernel_SourceLineNumbers);
		_chkRecord_Realtime = _createCheckButton(p, PerfPlugin.STRINGS_Record_Realtime);
		_chkMultiplexEvents = _createCheckButton(p, PerfPlugin.STRINGS_Multiplex);		
	}	
	//Function adapted from org.eclipse.linuxtools.oprofile.launch.configuration.OprofileSetupTab.java
	// Helper function for creating buttons. 
	private Button _createCheckButton(Composite parent, String label) {
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
	//Function adapted from org.eclipse.linuxtools.oprofile.launch.configuration.OprofileSetupTab.java
	// handles text modification event for kernel file location
	private void _handleKernelImageFileTextModify(Text text) {
		String errorMessage = null;
		String filename = text.getText();

		if (filename.length() > 0) {
			File file = new File(filename);
			if (!file.exists() || !file.isFile()) {
				String msg = "The entered location does not exist"; //$NON-NLS-1$
				Object[] args = new Object[] { filename };
				errorMessage = MessageFormat.format(msg, args);
			}
		}

		//setDirty(true);
		// Update dialog and error message
		setErrorMessage(errorMessage);
		updateLaunchConfigurationDialog();
	}
	//Function adapted from org.eclipse.linuxtools.oprofile.launch.configuration.OprofileSetupTab.java
	// Displays a file dialog to allow the user to select the kernel image file
	private void _showFileDialog(Shell shell) {
		FileDialog d = new FileDialog(shell, SWT.OPEN);
		File kernel = new File(_txtKernel_Location.getText());
		if (!kernel.exists()) {
			kernel = new File("/boot"); 	//$NON-NLS-1$
			if (!kernel.exists())
				kernel = new File("/"); 	//$NON-NLS-1$
		}
		d.setFileName(kernel.toString());
		d.setText("Select location of kernel image file"); //$NON-NLS-1$
		String newKernel = d.open();
		if (newKernel != null) {
			kernel = new File(newKernel);
			if (!kernel.exists()) {
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
				mb.setMessage("File doesn't exist"); 	//$NON-NLS-1$
				switch (mb.open()) {
					case SWT.RETRY:
						// Ok, it's recursive, but it shouldn't matter
						_showFileDialog(shell);
						break;
					default:
					case SWT.CANCEL:
						break;
				}
			} else {
				//setDirty(true);
				_txtKernel_Location.setText(newKernel);
			}
		}
	}

	@Override
	public String getName() {
		return "Perf Options";
	}

	@Override
	public void initializeFrom(ILaunchConfiguration config) {

		try {
			_txtKernel_Location.setText(config.getAttribute(PerfPlugin.ATTR_Kernel_Location, PerfPlugin.ATTR_Kernel_Location_default));
			_chkRecord_Realtime.setSelection(config.getAttribute(PerfPlugin.ATTR_Record_Realtime, PerfPlugin.ATTR_Record_Realtime_default));
			_chkRecord_Verbose.setSelection(config.getAttribute(PerfPlugin.ATTR_Record_Verbose, PerfPlugin.ATTR_Record_Verbose_default));
			_chkSourceLineNumbers.setSelection(config.getAttribute(PerfPlugin.ATTR_SourceLineNumbers, PerfPlugin.ATTR_SourceLineNumbers_default));
			_chkKernel_SourceLineNumbers.setSelection(config.getAttribute(PerfPlugin.ATTR_Kernel_SourceLineNumbers, PerfPlugin.ATTR_Kernel_SourceLineNumbers_default));
			
			_chkMultiplexEvents.setSelection(config.getAttribute(PerfPlugin.ATTR_Multiplex, PerfPlugin.ATTR_Multiplex_default));
			_chkModuleSymbols.setSelection(config.getAttribute(PerfPlugin.ATTR_ModuleSymbols, PerfPlugin.ATTR_ModuleSymbols_default));
			_chkHideUnresolvedSymbols.setSelection(config.getAttribute(PerfPlugin.ATTR_HideUnresolvedSymbols, PerfPlugin.ATTR_HideUnresolvedSymbols_default));
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy wconfig) {
		wconfig.setAttribute(PerfPlugin.ATTR_Kernel_Location, _txtKernel_Location.getText());
		wconfig.setAttribute(PerfPlugin.ATTR_Record_Realtime, _chkRecord_Realtime.getSelection());
		wconfig.setAttribute(PerfPlugin.ATTR_Record_Verbose, _chkRecord_Verbose.getSelection());
		wconfig.setAttribute(PerfPlugin.ATTR_SourceLineNumbers, _chkSourceLineNumbers.getSelection());
		wconfig.setAttribute(PerfPlugin.ATTR_Kernel_SourceLineNumbers, _chkKernel_SourceLineNumbers.getSelection());
		wconfig.setAttribute(PerfPlugin.ATTR_Multiplex, _chkMultiplexEvents.getSelection());
		wconfig.setAttribute(PerfPlugin.ATTR_ModuleSymbols, _chkModuleSymbols.getSelection());
		wconfig.setAttribute(PerfPlugin.ATTR_HideUnresolvedSymbols, _chkHideUnresolvedSymbols.getSelection());
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
	}

}
