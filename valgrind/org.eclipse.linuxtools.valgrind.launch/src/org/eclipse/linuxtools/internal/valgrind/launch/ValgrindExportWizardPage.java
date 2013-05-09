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
package org.eclipse.linuxtools.internal.valgrind.launch;

import java.io.File;
import java.io.FileFilter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class ValgrindExportWizardPage extends WizardPage {

	protected IPath outputPath;
	protected CheckboxTableViewer viewer;
	protected Text destText;
	protected Button selectAllButton;
	protected Button deselectAllButton;

	protected ValgrindExportWizardPage(String pageName) {
		super(pageName);
	}

	protected ValgrindExportWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}


	public void createControl(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout());
		top.setLayoutData(new GridData(GridData.FILL_BOTH));

		IPath logPath = null;

		// Retrieve location of Valgrind logs from launch configuration
		ILaunchConfiguration config = getPlugin().getCurrentLaunchConfiguration();
		if (config != null && config.exists()) {
			String strpath;
			try {
				strpath = config.getAttribute(LaunchConfigurationConstants.ATTR_INTERNAL_OUTPUT_DIR, (String) null);
				if (strpath != null) {
					logPath = Path.fromPortableString(strpath);
				}
			} catch (CoreException e) {
				setErrorMessage(e.getLocalizedMessage());
				e.printStackTrace();
			}			
		}

		Label selectFilesLabel = new Label(top, SWT.NONE);
		selectFilesLabel.setText(Messages.getString("ValgrindExportWizardPage.Viewer_label")); //$NON-NLS-1$

		viewer = CheckboxTableViewer.newCheckList(top, SWT.BORDER);
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((File) element).getName();
			}

			@Override
			public Image getImage(Object element) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
			}
		});

		Composite selectAllNoneTop = new Composite(top, SWT.NONE);
		selectAllNoneTop.setLayout(new GridLayout(2, true));
		selectAllNoneTop.setLayoutData(new GridData(SWT.TRAIL, SWT.DEFAULT, false, false));

		selectAllButton = new Button(selectAllNoneTop, SWT.NONE);
		selectAllButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectAllButton.setText(Messages.getString("ValgrindExportWizardPage.Select_all")); //$NON-NLS-1$
		selectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setAllChecked(true);
			}
		});

		deselectAllButton = new Button(selectAllNoneTop, SWT.NONE);
		deselectAllButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		deselectAllButton.setText(Messages.getString("ValgrindExportWizardPage.Deselect_all")); //$NON-NLS-1$
		deselectAllButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				viewer.setAllChecked(false);
			}
		});

		createDestinationGroup(top);

		if (logPath != null) {
			// List all output files in our output directory from the recent launch
			File logs[] = logPath.toFile().listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isFile();
				}				
			});
			viewer.setInput(logs);
			viewer.setAllChecked(true);
		}

		// catch any errors so far
		setPageComplete(isValid());
		
		setControl(top);
	}

	public IPath getOutputPath() {
		return outputPath;
	}

	public File[] getSelectedFiles() {
		Object[] selected = viewer.getCheckedElements();
		File[] files = new File[selected.length];
		System.arraycopy(selected, 0, files, 0, selected.length);
		return files;
	}

	private void createDestinationGroup(Composite top) {
		Group destGroup = new Group(top, SWT.SHADOW_OUT);
		destGroup.setText(Messages.getString("ValgrindExportWizardPage.Destination_group")); //$NON-NLS-1$
		destGroup.setLayout(new GridLayout(2, false));
		destGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		destText = new Text(destGroup, SWT.BORDER);
		destText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		destText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(isValid());
			}
		});

		Button browseButton = new Button(destGroup, SWT.PUSH);
		browseButton.setText(Messages.getString("ValgrindExportWizardPage.Browse")); //$NON-NLS-1$
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Prompt for output directory
				Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				DirectoryDialog dialog = new DirectoryDialog(parent);
				dialog.setText(Messages.getString("ValgrindLaunchConfigurationDelegate.Select_Destination")); //$NON-NLS-1$
				String strpath = dialog.open();
				if (strpath != null) {
					destText.setText(strpath);					
				}
			}
		});
	}

	protected boolean isValid() {
		boolean valid = false;
		int length = -1;
		
		setErrorMessage(null);
		setMessage(null);

		Object obj = viewer.getInput();
		// Check if there are no launch files (either null because directory is missing
		// or 0 files are found in directory)
		if (obj instanceof File[])
			length = ((File[])obj).length;
		if (viewer.getInput() == null || length == 0) {
			setErrorMessage(Messages.getString("ValgrindExportWizardPage.Err_No_Valgrind_run")); //$NON-NLS-1$
		}
		else if (destText.getText().equals("")) { //$NON-NLS-1$
			setMessage(Messages.getString("ValgrindExportWizardPage.Msg_Select_destination")); //$NON-NLS-1$
		}
		else {
			IPath path = Path.fromOSString(destText.getText());
			if (!path.toFile().exists()) {
				setErrorMessage(NLS.bind(Messages.getString("ValgrindExportWizardPage.Err_Dir_not_exist"), path.toOSString())); //$NON-NLS-1$
			}
			else if (!path.toFile().isDirectory()) {
				setErrorMessage(NLS.bind(Messages.getString("ValgrindExportWizardPage.Err_Not_dir"), path.toOSString())); //$NON-NLS-1$
			}
			else {
				outputPath = path;
				valid = true;
			}
		}
		
		return valid;
	}

	protected ValgrindLaunchPlugin getPlugin() {
		return ValgrindLaunchPlugin.getDefault();
	}

	public CheckboxTableViewer getViewer() {
		return viewer;
	}

	public Text getDestText() {
		return destText;
	}

	public Button getSelectAllButton() {
		return selectAllButton;
	}

	public Button getDeselectAllButton() {
		return deselectAllButton;
	}

}
