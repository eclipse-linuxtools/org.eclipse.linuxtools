/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.localgui.launch;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.linuxtools.systemtap.localgui.core.PluginConstants;
import org.eclipse.linuxtools.systemtap.localgui.core.ShellOpener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

public class LaunchWizard extends SystemTapLaunchShortcut {
	
	private Text scriptLocation;
	private Text binaryLocation;
	private Text argumentsLocation;
	private String workspacePath;
	private String mode;
	
	private Shell sh;
	private Composite fileComp;
	private boolean completed;
	/*
	 * The following protected parameters are provided by SystemTapLaunchShortcut:
	 *
	 *	Optional customization parameters:
	 * protected String name;
	 * protected String binaryPath;
	 * protected String arguments;
	 * protected String outputPath;
	 * protected String dirPath;
	 * protected String generatedScript;
	 * protected boolean needToGenerate;
	 * protected boolean overwrite;
	 * 
	 *	Mandatory:
 	 * protected String scriptPath;
	 * protected ILaunchConfiguration config;
	 */


	/**
	 * Launch method for a generated script that executes on a binary 
	 * 
	 * MUST specify (String) scriptPath and call config = createConfiguration(bin)!
	 * 
	 * Noteworthy defaults:
	 * name defaults to "", but please set it (for usability)
	 * overwrite defaults to true - don't change it unless you really have to.
	 * 
	 * To create new launches:
	 * 		-Copy shortcut code in xml, changing class name and label accordingly
	 * 		-Create a class that extends SystemTapLaunchShortcut with a function
	 * 		 launch(IBinary bin, String mode)
	 * 		-Call super.Init()
	 * 		-Set name (this is shortcut-specific)
	 * 		-If a binary is used, call binName = getName(bin)
	 * 		-Call createConfiguration(bin, name)
	 * 
	 * 		-Specify whichever of the optional parameters you need
	 * 		-Set scriptPath
	 * 		-Set an ILaunchConfiguration
	 * 		-Call finishLaunch or finishLaunchWithoutBinary
	 */
	
	@Override
	public void launch(IEditorPart ed, String mode) {
		super.Init();
		promptForInputs();
		
		this.mode = mode;
		
//		finishLaunch(scriptPath + ": " + binName, mode); //$NON-NLS-1$
	}
	
	@Override
	public void launch(ISelection selection, String mode) {
		super.Init();
		completed = false;
		promptForInputs();
		
		this.mode = mode; 
		
//		finishLaunch(scriptPath + ": " + binName, mode); //$NON-NLS-1$
	}
	
	
	private void promptForInputs() {
		InputDialog id = new InputDialog(new Shell(), Messages.getString("LaunchWizard.0"),   //$NON-NLS-1$
				Messages.getString("LaunchWizard.1") +  //$NON-NLS-1$
				Messages.getString("LaunchWizard.2") +  //$NON-NLS-1$
				Messages.getString("LaunchWizard.3"),   //$NON-NLS-1$
				Messages.getString("LaunchWizard.4"), null);  //$NON-NLS-1$
		id.open();
		
		if (id.getReturnCode() == InputDialog.CANCEL){			
			return;
		}
		
		name = id.getValue();
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IPath location = root.getLocation();
		workspacePath = location.toString();
		
		sh = new Shell();
		sh.setSize(670,650);
		sh.setLayout(new GridLayout(1, false));
		sh.setText(name);
		
		
		Image img = new Image(sh.getDisplay(), PluginConstants.PLUGIN_LOCATION + "systemtapbanner.png"); //$NON-NLS-1$
		Composite imageCmp = new Composite(sh, SWT.BORDER);
		imageCmp.setLayout(new FillLayout());
		GridData imageData = new GridData(650, 157);
		imageData.horizontalAlignment = SWT.CENTER;
		imageCmp.setLayoutData(imageData);
		imageCmp.setBackgroundImage(img);
		
		fileComp = new Composite(sh, SWT.NONE);
		fileComp.setLayout(new GridLayout(3, false));
		fileComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label scriptLabel = new Label(fileComp, SWT.HORIZONTAL);
		scriptLabel.setText(Messages.getString("LaunchWizard.19")); //$NON-NLS-1$
		scriptLabel.setLayoutData(new GridData());
		scriptLocation = new Text(fileComp, SWT.SINGLE);
		scriptLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button scriptButton = new Button(fileComp, SWT.PUSH);
		scriptButton.setText(Messages.getString("SystemTapOptionsTab.BrowseFiles")); //$NON-NLS-1$
		scriptButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				String filePath = scriptLocation.getText();
				FileDialog dialog = new FileDialog(sh, SWT.SAVE);
				filePath = dialog.open();
				if (filePath != null) {
					scriptLocation.setText(filePath);
				}
			}
		});
		
		
		Label binaryLabel= new Label(fileComp, SWT.HORIZONTAL);
		binaryLabel.setText(Messages.getString("LaunchWizard.20")); //$NON-NLS-1$
		scriptLabel.setLayoutData(new GridData());
		
		binaryLocation = new Text(fileComp, SWT.SINGLE);
		binaryLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button binaryButton = new Button(fileComp, SWT.PUSH);
		binaryButton.setText(Messages.getString("SystemTapOptionsTab.WorkspaceButton2")); //$NON-NLS-1$
		binaryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(sh, new WorkbenchLabelProvider(), new WorkbenchContentProvider());
				dialog.setTitle(Messages.getString("SystemTapOptionsTab.SelectResource"));  //$NON-NLS-1$
				dialog.setMessage(Messages.getString("SystemTapOptionsTab.SelectSuppressions"));  //$NON-NLS-1$
				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot()); 
				dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
				if (dialog.open() == IDialogConstants.OK_ID) {
					IResource resource = (IResource) dialog.getFirstResult();
					String arg = resource.getFullPath().toString();
					binaryLocation.setText(workspacePath + arg);
				}
			}
		});

		Composite argumentsComp = new Composite(sh, SWT.BORDER_DASH);
		argumentsComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		argumentsComp.setLayout(new GridLayout(3, false));
		
		Label argumentsLabel= new Label(argumentsComp, SWT.HORIZONTAL);
		argumentsLabel.setText(Messages.getString("LaunchWizard.21")); //$NON-NLS-1$
		
		argumentsLocation = new Text(argumentsComp, SWT.MULTI | SWT.WRAP);
//		argumentsLocation.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
//		gd.grabExcessVerticalSpace=true;
		gd.heightHint=300;
		argumentsLocation.setLayoutData(gd);
		Button argumentsButton = new Button(argumentsComp, SWT.PUSH);
		argumentsButton.setText(Messages.getString("LaunchWizard.22")); //$NON-NLS-1$
		argumentsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				argumentsLocation.setText(
						argumentsLocation.getText() + " process(\""  //$NON-NLS-1$
						+ binaryLocation.getText() + "\").function(\"\")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
		
		
		//TODO: Don't use blank labels to move button to the right column :P
		Label blankLabel = new Label(argumentsComp, SWT.HORIZONTAL);
		blankLabel.setText(" "); //$NON-NLS-1$
		
		
		Button launch = new Button(argumentsComp, SWT.PUSH);
		launch.setLayoutData(new GridData(GridData.CENTER, GridData.BEGINNING, false, false));
		launch.setText(Messages.getString("LaunchWizard.24")); //$NON-NLS-1$
		launch.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
					scriptPath = scriptLocation.getText();
					binaryPath = binaryLocation.getText();
					arguments = argumentsLocation.getText();
					int index = binaryPath.lastIndexOf("/");
					if (index > 0 && index < binaryPath.length())
						name+= " - " + binaryPath.substring(index, binaryPath.length());
					name = getLaunchManager().generateUniqueLaunchConfigurationNameFrom(name);
					config = createConfiguration(null, name);

					finishLaunch(scriptPath + ": " + binName, mode); //$NON-NLS-1$
					completed = true;
					sh.dispose();
				}
				
		});
		
		ShellOpener so = new ShellOpener("Wizard Opener", sh);
		so.schedule();
		
//		while (!so.isDisposed()) {
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
//		}
		
		completed = true;

	}
	
	public boolean isCompleted() {
		return completed;
	}
	
}
	
//	
//	public void launch(IBinary bin, String mode) {
//		super.Init();
//		Shell sh = new Shell();
//		MessageDialog.openInformation(sh, "Welcome to SystemTap Wizard", 
//				"The SystemTap Wizard will guide you through the process of \n" +
//				"launching a SystemTap script through Eclipse. First, select\n" +
//				"a script to run.");
//			
//		
//		
//		name = Messages.getString("LaunchWizard.WizardName"); //$NON-NLS-1$
//		promptForInputs();
//		binName = getName(bin);
//		binaryPath =  dirPath + binName;
//		config = createConfiguration(bin);
//
//
//		System.out.println("LaunchCallGraph: launch(IBinary bin, String mode)"); //$NON-NLS-1$
//		
//		finishLaunch(scriptPath + ": " + binName, mode); //$NON-NLS-1$
//	}
//	
//	
//	private void promptForInputs() {
//		
//		Shell sh = new Shell();
//		
//		//Get script
//		MessageDialog.openInformation(sh, "Specify Script", //$NON-NLS-1$
//				"Please specify the location of the SystemTap script to run."); //$NON-NLS-1$
//		FileDialog fd = new FileDialog(sh);
//		scriptPath = fd.open();
//		
//		if (scriptPath == null){
//			scriptPath = ""; //$NON-NLS-1$
//			return;
//		}
//
//		//Get arguments
//		InputDialog inputDialog = new InputDialog(
//				sh,
//				"Specify Arguments", //$NON-NLS-1$
//				"Specify Arguments separated by a space (eg. arg1 arg2 arg3) or CANCEL to specify no arguments.", //$NON-NLS-1$
//				"", null); //$NON-NLS-1$
//		inputDialog.open();
//		arguments = inputDialog.getValue();
//		inputDialog.close();
//
//		if (arguments == null || arguments.equals("")){ //$NON-NLS-1$
//			arguments = ""; //$NON-NLS-1$
//		}
//		
//		sh.dispose();
//	}
//	
//}
