/*******************************************************************************
 * Copyright (c) 2004,2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Keith Seitz <keiths@redhat.com> - initial API and implementation
 *    Kent Sebastian <ksebasti@redhat.com> - 
 *******************************************************************************/
package org.eclipse.linuxtools.internal.oprofile.launch.configuration;

import java.text.MessageFormat;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.linuxtools.internal.oprofile.core.Oprofile;
import org.eclipse.linuxtools.internal.oprofile.core.daemon.OprofileDaemonOptions;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchMessages;
import org.eclipse.linuxtools.internal.oprofile.launch.OprofileLaunchPlugin;
import org.eclipse.linuxtools.profiling.launch.IRemoteFileProxy;
import org.eclipse.linuxtools.profiling.launch.RemoteProxyManager;
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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * This tab is used by the launcher to configure global oprofile run options.
 */
public class OprofileSetupTab extends AbstractLaunchConfigurationTab {
	protected Text kernelImageFileText;

	protected Button checkSeparateLibrary;
	protected Button checkSeparateKernel;

	protected LaunchOptions options = null;

	protected Spinner executionsSpinner;

	private IRemoteFileProxy proxy;

	public String getName() {
		return OprofileLaunchMessages.getString("tab.global.name"); //$NON-NLS-1$
	}

	@Override
	public boolean isValid(ILaunchConfiguration config) {
		boolean b = options.isValid();
		return b;
	}

	public void performApply(ILaunchConfigurationWorkingCopy config) {
		options.saveConfiguration(config);
	}

	public void initializeFrom(ILaunchConfiguration config) {
		options.loadConfiguration(config);
		
		kernelImageFileText.setText(options.getKernelImageFile());
		executionsSpinner.setSelection(options.getExecutionsNumber());
		
		int separate = options.getSeparateSamples();
		
		if (separate == OprofileDaemonOptions.SEPARATE_NONE) {
			checkSeparateLibrary.setSelection(false);
			checkSeparateKernel.setSelection(false);
		} else {
			//note that opcontrol will nicely ignore the trailing comma
			if ((separate & OprofileDaemonOptions.SEPARATE_LIBRARY) != 0)
				checkSeparateLibrary.setSelection(true);
			if ((separate & OprofileDaemonOptions.SEPARATE_KERNEL) != 0)
				checkSeparateKernel.setSelection(true);
		}
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy config) {
		options = new LaunchOptions();
		options.saveConfiguration(config);
	}
	
	@Override
	public Image getImage() {
		return OprofileLaunchPlugin.getImageDescriptor(OprofileLaunchPlugin.ICON_GLOBAL_TAB).createImage();
	}

	public void createControl(Composite parent) {
		options = new LaunchOptions();

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
		l.setText(OprofileLaunchMessages.getString("tab.global.kernelImage.label.text")); //$NON-NLS-1$
		data = new GridData();
		data.horizontalSpan = 2;
		l.setLayoutData(data);

		kernelImageFileText = new Text(p, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		kernelImageFileText.setLayoutData(data);
		kernelImageFileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent mev) {
				handleKernelImageFileTextModify(kernelImageFileText);
			};
		});

		Button button = createPushButton(p, OprofileLaunchMessages.getString("tab.global.kernelImage.browse.button.text"), null); //$NON-NLS-1$
		final Shell shell = top.getShell();
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent sev) {
				showFileDialog(shell);
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

		checkSeparateLibrary = myCreateCheckButton(p, OprofileLaunchMessages.getString("tab.global.check.separateLibrary.text")); //$NON-NLS-1$
		checkSeparateKernel = myCreateCheckButton(p, OprofileLaunchMessages.getString("tab.global.check.separateKernel.text")); //$NON-NLS-1$

		//Number of executions spinner
		Composite executionsComposite = new Composite(top, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		executionsComposite.setLayout(gridLayout);
		Label executionsLabel = new Label(executionsComposite, SWT.LEFT);
		executionsLabel.setText(OprofileLaunchMessages.getString("tab.global.executionsNumber.label.text")); //$NON-NLS-1
		executionsLabel.setToolTipText(OprofileLaunchMessages.getString("tab.global.executionsNumber.label.tooltip")); //$NON-NLS-1$
		executionsSpinner = new Spinner(executionsComposite, SWT.BORDER);
		executionsSpinner.setMinimum(1);
		executionsSpinner.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				options.setExecutionsNumber(executionsSpinner.getSelection());
				updateLaunchConfigurationDialog();
			}
		});
	}



	// convenience method to create radio buttons with the given label
	private Button myCreateCheckButton(Composite parent, String label) {
		final Button b = new Button(parent, SWT.CHECK);
		b.setText(label);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent se) {
				handleCheckSelected(b);
			}
		});

		return b;
	}

	//sets the proper separation mask for sample separation 
	private void handleCheckSelected(Button button) {
		int oldSeparate = options.getSeparateSamples();
		int newSeparate = oldSeparate;		//initalize
		
		if (button == checkSeparateLibrary) {
			if (button.getSelection()) {
				newSeparate = oldSeparate | OprofileDaemonOptions.SEPARATE_LIBRARY;
			} else {
				newSeparate = oldSeparate & ~OprofileDaemonOptions.SEPARATE_LIBRARY;
			}
		} else if (button == checkSeparateKernel) {
			if (button.getSelection()) {
				newSeparate = oldSeparate | OprofileDaemonOptions.SEPARATE_KERNEL;
			} else {
				newSeparate = oldSeparate & ~OprofileDaemonOptions.SEPARATE_KERNEL;
			}
		}
		
		options.setSeparateSamples(newSeparate);

		updateLaunchConfigurationDialog();
	}

	// handles text modification events for all text boxes in this tab
	private void handleKernelImageFileTextModify(Text text) {
		String errorMessage = null;
		String filename = text.getText();

		if (filename.length() > 0) {
			try {
				proxy = RemoteProxyManager.getInstance().getFileProxy(getOprofileProject());
			} catch (CoreException e) {
				e.printStackTrace();
			}
			IFileStore fileStore = proxy.getResource(filename);
			if (!fileStore.fetchInfo().exists() || fileStore.fetchInfo().isDirectory()){
				String msg = OprofileLaunchMessages.getString("tab.global.kernelImage.kernel.nonexistent"); //$NON-NLS-1$
				Object[] args = new Object[] { filename };
				errorMessage = MessageFormat.format(msg, args);
			}

			//seems odd, but must set it even if it is invalid so that performApply
			// and isValid work properly
			options.setKernelImageFile(filename);
		} else {
			// no kernel image file
			options.setKernelImageFile(""); //$NON-NLS-1$
		}

		// Update dialog and error message
		setErrorMessage(errorMessage);
		updateLaunchConfigurationDialog();
	}

	// Displays a file dialog to allow the user to select the kernel image file
	private void showFileDialog(Shell shell) {
		try {
			proxy = RemoteProxyManager.getInstance().getFileProxy(getOprofileProject());
		} catch (CoreException e) {
			e.printStackTrace();
		}

		FileDialog d = new FileDialog(shell, SWT.OPEN);
		IFileStore kernel = proxy.getResource(options.getKernelImageFile());
		if (!kernel.fetchInfo().exists()) {
			kernel = proxy.getResource("/boot");	//$NON-NLS-1$

			if (!kernel.fetchInfo().exists())
				kernel = proxy.getResource("/");	//$NON-NLS-1$
		}
		d.setFileName(kernel.toString());
		d.setText(OprofileLaunchMessages.getString("tab.global.selectKernelDialog.text")); //$NON-NLS-1$
		String newKernel = d.open();
		if (newKernel != null) {
			kernel = proxy.getResource(newKernel);
			if (!kernel.fetchInfo().exists()) {
				MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.RETRY | SWT.CANCEL);
				mb.setMessage(OprofileLaunchMessages.getString("tab.global.selectKernelDialog.error.kernelDoesNotExist.text")); 	//$NON-NLS-1$
				switch (mb.open()) {
					case SWT.RETRY:
						// Ok, it's recursive, but it shouldn't matter
						showFileDialog(shell);
						break;
					default:
					case SWT.CANCEL:
						break;
				}
			} else {
				kernelImageFileText.setText(newKernel);
			}
		}
	}

	/**
	 * Get project to profile
	 * @return IProject project to profile
	 */
	protected IProject getOprofileProject(){
		return Oprofile.OprofileProject.getProject();
	}
}
