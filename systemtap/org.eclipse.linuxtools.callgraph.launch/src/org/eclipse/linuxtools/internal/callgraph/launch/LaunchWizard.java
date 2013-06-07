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

package org.eclipse.linuxtools.internal.callgraph.launch;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.callgraph.core.PluginConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
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

	private static final int WIDTH = 670;
	private static final int HEIGHT = 630;

	/**
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
		super.initialize();
		promptForInputs();

		this.mode = mode;
	}

	@Override
	public void launch(ISelection selection, String mode) {
		super.initialize();
		completed = false;
		promptForInputs();

		this.mode = mode;
	}

	private void promptForInputs() {
		InputDialog id = new InputDialog(new Shell(), Messages.getString("LaunchWizard.WelcomeWizard"),   //$NON-NLS-1$
				Messages.getString("LaunchWizard.Text1") +  //$NON-NLS-1$
				Messages.getString("LaunchWizard.Text2") +  //$NON-NLS-1$
				Messages.getString("LaunchWizard.Text3"),   //$NON-NLS-1$
				getLaunchManager().generateLaunchConfigurationName(
						Messages.getString("LaunchWizard.NamePrefix")), null);  //$NON-NLS-1$
		id.open();

		if (id.getReturnCode() == Window.CANCEL){
			return;
		}

		name = id.getValue();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IPath location = root.getLocation();
		workspacePath = location.toString();

		sh = new Shell();
		sh.setSize(WIDTH,HEIGHT);
		sh.setLayout(new GridLayout(1, false));
		sh.setText(name);


		Image img = new Image(sh.getDisplay(), PluginConstants.getPluginLocation() + "systemtapbanner.png"); //$NON-NLS-1$
		Composite imageCmp = new Composite(sh, SWT.BORDER);
		imageCmp.setLayout(new FillLayout());
		GridData imageData = new GridData(650, 157);
		imageData.horizontalAlignment = SWT.CENTER;
		imageCmp.setLayoutData(imageData);
		imageCmp.setBackgroundImage(img);

		fileComp = new Composite(sh, SWT.NONE);
		fileComp.setLayout(new GridLayout(2, false));
		fileComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));


		GridDataFactory labelData = GridDataFactory.fillDefaults().grab(true, false)
									.span(2,1);

		Label scriptLabel = new Label(fileComp, SWT.HORIZONTAL);
		scriptLabel.setText(Messages.getString("LaunchWizard.Script")); //$NON-NLS-1$
		labelData.applyTo(scriptLabel);

		GridDataFactory textData = GridDataFactory.fillDefaults().grab( true, false )
								  .hint(WIDTH, SWT.DEFAULT);

		scriptLocation = new Text(fileComp, SWT.SINGLE | SWT.BORDER);
		textData.applyTo(scriptLocation);
		Button scriptButton = new Button(fileComp, SWT.PUSH);
		scriptButton.setText(Messages.getString("SystemTapOptionsTab.BrowseFiles")); //$NON-NLS-1$
		scriptButton.setLayoutData(new GridData());
		scriptButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filePath = scriptLocation.getText();
				FileDialog dialog = new FileDialog(sh, SWT.SAVE);
				filePath = dialog.open();
				if (filePath != null) {
					scriptLocation.setText(filePath);
				}
			}
		});


		GridData gd2 = new GridData();
		gd2.horizontalSpan = 3;
		Label binaryLabel= new Label(fileComp, SWT.HORIZONTAL);
		binaryLabel.setText(Messages.getString("LaunchWizard.BinFile")); //$NON-NLS-1$
		labelData.applyTo(binaryLabel);

		binaryLocation = new Text(fileComp, SWT.SINGLE | SWT.BORDER);
		textData.applyTo(binaryLocation);
		Button binaryButton = new Button(fileComp, SWT.PUSH);
		binaryButton.setText(Messages.getString("SystemTapOptionsTab.WorkspaceButton2")); //$NON-NLS-1$
		binaryButton.addSelectionListener(new SelectionAdapter() {
			@Override
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
		argumentsComp.setLayout(new GridLayout(2, false));

		Label argumentsLabel= new Label(argumentsComp, SWT.HORIZONTAL);
		argumentsLabel.setText(Messages.getString("LaunchWizard.Args")); //$NON-NLS-1$
		labelData.applyTo(argumentsLabel);

		argumentsLocation = new Text(argumentsComp, SWT.MULTI | SWT.WRAP | SWT.BORDER);
		GridData gd3 = new GridData(GridData.FILL_HORIZONTAL);
		gd3.heightHint=200;
		argumentsLocation.setLayoutData(gd3);
		Button argumentsButton = new Button(argumentsComp, SWT.PUSH);
		argumentsButton.setText(Messages.getString("LaunchWizard.Func")); //$NON-NLS-1$
		argumentsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				argumentsLocation.setText(
						argumentsLocation.getText() + " process(\""  //$NON-NLS-1$
						+ binaryLocation.getText() + "\").function(\"\")"); //$NON-NLS-1$
			}
		});


		//TODO: Don't use blank labels to move button to the right column :P
		Label blankLabel2 = new Label(argumentsComp, SWT.HORIZONTAL);
		blankLabel2.setText(""); //$NON-NLS-1$


		Button launch = new Button(sh, SWT.PUSH);
		launch.setLayoutData(new GridData(GridData.CENTER, GridData.BEGINNING, false, false));
		launch.setText(Messages.getString("LaunchWizard.Launch")); //$NON-NLS-1$
		launch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
					scriptPath = scriptLocation.getText();
					binaryPath = binaryLocation.getText();
					arguments = argumentsLocation.getText();
					ILaunchConfigurationWorkingCopy wc = createConfiguration(null, name);
					try {
						finishLaunch(scriptPath + ": " + binName, mode, wc);//$NON-NLS-1$
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					completed = true;
					sh.dispose();
				}

		});

		//TODO: Verify that this works
		Display.getCurrent().asyncExec(new Runnable() {

			@Override
			public void run() {
				sh.open();
				completed = true;
			}

		});


	}

	public boolean isCompleted() {
		return completed;
	}

	@Override
	public String setScriptPath() {
		scriptPath = "IMPLEMENT"; //$NON-NLS-1$
		return scriptPath;
	}

	@Override
	public String setParserID() {
		return null;
	}

	@Override
	public String setViewID() {
		return null;
	}

}
