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

import java.io.File;
import java.util.ArrayList;

import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.internal.callgraph.core.LaunchConfigurationConstants;
import org.eclipse.linuxtools.internal.callgraph.core.PluginConstants;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapView;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

/**
 * Options tab for SystemTap. Currently does NOT contain all possible options
 *
 */
public class SystemTapOptionsTab extends CLaunchConfigurationTab{

	//Control creation objects
	protected Composite top;
	protected ScrolledComposite scrollTop;
	protected Combo toolsCombo;

	protected TabFolder fileFolder;
	protected TabFolder commandFolder;
	protected TabFolder argumentsFolder;
	protected TabFolder binaryArgumentsFolder;
	protected TabFolder parserFolder;
	protected TabFolder generatedScriptFolder;

	//Controls
	protected Text scriptFile;
	protected Text binaryFile;
	protected Text arguments;
	protected Text generatedScript;
	protected Text outputFile;
	protected Text button_D_text;
	protected Text binaryArguments;
	protected Text parser;
	protected Text viewer;

	protected Button fileBrowseButton;
	protected Button workspaceBrowseButton;
	protected Button parserButton;
	protected Button viewerButton;


	protected Button button_k;
	protected Button button_u;
	protected Button button_w;
	protected Button button_b;
	protected Button button_g;
	protected Button button_P;
	protected Button button_t;
	protected Button button_build;
	protected Button button_F;
	protected Button button_skip_badvars;
	protected Button button_ignore_dwarf;
	protected Button button_q;
	protected Button needsBinaryButton;
	protected Button needToGenerateScriptButton;
	protected Button button_graphicsMode;


	protected Spinner button_p_Spinner;
	protected Spinner button_s_Spinner;
	protected Spinner button_x_Spinner;
	protected Spinner button_v_Spinner;

	private Button useColourButton;

	//Other variables
	protected String workspacePath;
	protected String[] tools;
	protected boolean output_file_has_changed = false;
	protected boolean needsOverwritePermission = false;
	protected boolean overwritePermission = false;
	private boolean changeOverwrite = false;

	/**
	 * The code below is very long, but it boils down to this.
	 *
	 * The main function is createControl. This function prepares
	 * some space for the various tabs and calls the other create*Option
	 * functions to create buttons, text fields and spinners.
	 *
	 * The create*Option functions create their respective sub-tabs.
	 *
	 * Each of the controls (button, text, spinner) needs to have a listener
	 * attached so Eclipse knows what to do with them.
	 *
	 * There are a bunch of functions to update/set defaults/initialize,
	 * and a final
	 */

	protected SelectionListener graphicsModeListener = new SelectionAdapter(){
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (scriptFile.isEnabled()){
				scriptFile.setEnabled(false);
				workspaceBrowseButton.setEnabled(false);
				fileBrowseButton.setEnabled(false);
				scriptFile.setText(PluginConstants.getPluginLocation()+"parse_function.stp"); //$NON-NLS-1$
			}else{
				scriptFile.setEnabled(true);
				workspaceBrowseButton.setEnabled(true);
				fileBrowseButton.setEnabled(true);
			}
			updateLaunchConfigurationDialog();
		}
	};

	protected SelectionListener selectListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			updateLaunchConfigurationDialog();
		}
	};

	protected ModifyListener modifyListener = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}
	};

	protected ModifyListener modifyListenerOutput = new ModifyListener() {
		@Override
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
			output_file_has_changed = true;
		}
	};

	protected FocusListener focusListener = new FocusAdapter() {

		@Override
		public void focusLost(FocusEvent e) {
			if (output_file_has_changed) {
				checkOverwrite();
			}
			output_file_has_changed = false;

			updateLaunchConfigurationDialog();
		}
	};

	/**
	 * Convenience method to check if the overwrite permissions are consistent
	 *
	 * Helps ensure validity of configuration.
	 */
	public void checkOverwrite() {
		File f = new File(outputFile.getText());
		changeOverwrite = true;
		if (f.exists()) {
			needsOverwritePermission = true;
			Shell sh = new Shell();
			if (MessageDialog.openConfirm(sh, Messages.getString("SystemTapOptionsTab.ConfirmOverwriteFileTitle"),   //$NON-NLS-1$
					Messages.getString("SystemTapOptionsTab.ConfirmOverwriteFileMessage"))) {  //$NON-NLS-1$
				overwritePermission = true;
			} else {
				overwritePermission = false;
			}
		}
		else
			needsOverwritePermission = false;
	}

	/**
	 * This function prepares some space for the various
	 * tabs and calls and sets the other create*Option
	 * functions to create buttons, text fields and spinners.
	 */
	@Override
	public void createControl(Composite parent) {
		scrollTop = new ScrolledComposite(parent,	SWT.H_SCROLL | SWT.V_SCROLL);
		scrollTop.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		scrollTop.setExpandVertical(true);
		scrollTop.setExpandHorizontal(true);

		setControl(scrollTop);

		top = new Composite(scrollTop, SWT.NONE);
		top.setLayout(new GridLayout());

		scrollTop.setContent(top);

		/*
		 * File folder - tab for selecting binary/stp file
		 */
		fileFolder = new TabFolder(top, SWT.BORDER);
		fileFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem fileTab = new TabItem(fileFolder, SWT.NONE);
		fileTab.setText(Messages.getString("SystemTapOptionsTab.FilesTab")); //$NON-NLS-1$

		Composite fileTop = new Composite(fileFolder, SWT.NONE);
		fileTop.setLayout(new GridLayout());
		fileTop.setLayoutData(new GridData(GridData.FILL_BOTH));

		createFileOption(fileTop);
		fileTab.setControl(fileTop);


		/*
		 * Commands folder - tab for selecting SystemTap commands
		 */

		commandFolder = new TabFolder(top, SWT.BORDER);
		commandFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem commandTab = new TabItem(fileFolder, SWT.NONE);
		commandTab.setText(Messages.getString("SystemTapOptionsTab.CommandsTab")); //$NON-NLS-1$

		Composite commandTop = new Composite(fileFolder, SWT.NONE);
		commandTop.setLayout(new GridLayout());
		commandTop.setLayoutData(new GridData(GridData.FILL_BOTH));

		createCommandOption(commandTop);
		commandTab.setControl(commandTop);

		/*
		 * Arguments folder - tab for selecting script arguments
		 */
		argumentsFolder = new TabFolder(top, SWT.BORDER);
		argumentsFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem argumentsTab = new TabItem(fileFolder, SWT.NONE);
		argumentsTab.setText(Messages.getString("SystemTapOptionsTab.Arguments")); //$NON-NLS-1$

		Composite argumentsTop = new Composite(fileFolder, SWT.NONE);
		argumentsTop.setLayout(new GridLayout());
		argumentsTop.setLayoutData(new GridData(GridData.FILL_BOTH));

		createArgumentsOption(argumentsTop);
		argumentsTab.setControl(argumentsTop);


		/*
		 * Binary Argument folder - tab for supplying arguments for a binary
		 */
		binaryArgumentsFolder = new TabFolder(top, SWT.BORDER);
		binaryArgumentsFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem binaryArgumentsTab = new TabItem(fileFolder, SWT.NONE);
		binaryArgumentsTab.setText(Messages.getString("SystemTapOptionsTab.44")); //$NON-NLS-1$

		Composite binaryArgumentsTop = new Composite(fileFolder, SWT.NONE);
		binaryArgumentsTop.setLayout(new GridLayout());
		binaryArgumentsTop.setLayoutData(new GridData(GridData.FILL_BOTH));

		createBinaryArgumentsOption(binaryArgumentsTop);
		binaryArgumentsTab.setControl(binaryArgumentsTop);


		/*
		 * Parser folder -- Tab for selecting a parser and viewer to use
		 */
		parserFolder = new TabFolder(top, SWT.BORDER);
		parserFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem parserTab = new TabItem(fileFolder, SWT.NONE);
		parserTab.setText("Parser"); //$NON-NLS-1$

		Composite parserTop = new Composite(fileFolder, SWT.NONE);
		parserTop.setLayout(new GridLayout());
		parserTop.setLayoutData(new GridData(GridData.FILL_BOTH));

		createParserOption(parserTop);
		parserTab.setControl(parserTop);
	}


	private void createBinaryArgumentsOption(Composite binaryArgumentsTop) {
		Composite browseTop = new Composite(binaryArgumentsTop, SWT.NONE);
		browseTop.setLayout(new GridLayout(1, false));
		GridData browseData = new GridData(GridData.FILL_HORIZONTAL);
		browseTop.setLayoutData(browseData);

		Label suppFileLabel = new Label(browseTop, SWT.NONE);
		suppFileLabel.setText(Messages.getString("SystemTapOptionsTab.45")); //$NON-NLS-1$

		binaryArguments = new Text(browseTop,SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 200;
		binaryArguments.setLayoutData(gd);
		binaryArguments.addModifyListener(modifyListener);
	}

	private void createParserOption(Composite parserTop) {
		Composite browseTop = new Composite(parserTop, SWT.NONE);
		browseTop.setLayout(new GridLayout(1, false));
		GridData browseData = new GridData(GridData.FILL_HORIZONTAL);
		browseTop.setLayoutData(browseData);

		Label suppFileLabel = new Label(browseTop, SWT.NONE);
		suppFileLabel.setText("Parser"); //$NON-NLS-1$

		parser = new Text(browseTop, SWT.BORDER);
		parser.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		parser.addModifyListener(modifyListener);

		parserButton = createPushButton(browseTop,
				"Find parsers", null);  //$NON-NLS-1$
		parserButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(),
						new ListLabelProvider());
				dialog.setTitle("Select parser");  //$NON-NLS-1$
				dialog.setMessage("Select parser to use.");  //$NON-NLS-1$
				IExtensionRegistry reg = Platform.getExtensionRegistry();
				IConfigurationElement[] extensions = reg
						.getConfigurationElementsFor(PluginConstants.PARSER_RESOURCE,
								PluginConstants.PARSER_NAME);

				dialog.setElements(extensions);
				if (dialog.open() == IDialogConstants.OK_ID) {
					String arg = getUsefulLabel(dialog.getFirstResult());
					parser.setText(arg);
				}
			}
		});


		viewer = new Text(browseTop, SWT.BORDER);
		viewer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		viewer.addModifyListener(modifyListener);

		viewerButton = createPushButton(browseTop,
				"Find viewers", null);  //$NON-NLS-1$
		viewerButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(),
						new ListLabelProvider());
				dialog.setTitle("Select viewer");  //$NON-NLS-1$
				dialog.setMessage("Select viewer to use.");  //$NON-NLS-1$
				IExtensionRegistry reg = Platform.getExtensionRegistry();
				IConfigurationElement[] extensions = reg
						.getConfigurationElementsFor(PluginConstants.VIEW_RESOURCE,
								PluginConstants.VIEW_NAME);
				ArrayList<IConfigurationElement> ext = new ArrayList<IConfigurationElement>();
				for (IConfigurationElement el : extensions) {
					if (!el.getNamespaceIdentifier().contains("org.eclipse.linuxtools")) //$NON-NLS-1$
						continue;
					//TODO: Rough hack to get all the objects. We restrict to id's containing org.eclipse.linuxtools, then see if the class extends SystemTapView
					try {
						if (el.createExecutableExtension(PluginConstants.ATTR_CLASS)
								instanceof SystemTapView) {
							ext.add(el);
						}
					} catch (CoreException e1) {
					}
				}

				dialog.setElements(ext.toArray());
				if (dialog.open() == IDialogConstants.OK_ID) {
					String arg = getUsefulLabel(dialog.getFirstResult());
					viewer.setText(arg);
				}
			}
		});

	}

	protected void createGeneratedScriptOption(Composite generatedScriptTop) {
		Composite browseTop = new Composite(generatedScriptTop, SWT.NONE);
		browseTop.setLayout(new GridLayout(1, false));
		GridData browseData = new GridData(GridData.FILL_HORIZONTAL);
		browseTop.setLayoutData(browseData);


		Label suppFileLabel = new Label(browseTop, SWT.NONE);
		suppFileLabel.setText(Messages.getString("SystemTapOptionsTab.GeneratedScriptsTitle")); //$NON-NLS-1$

		generatedScript = new Text(browseTop,SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 200;
		generatedScript.setLayoutData(gd);
		generatedScript.addModifyListener(modifyListener);

		needToGenerateScriptButton = new Button(browseTop, SWT.CHECK);
		needToGenerateScriptButton.setText(Messages.getString("SystemTapOptionsTab.GenerateScriptButton"));  //$NON-NLS-1$
		needToGenerateScriptButton.addSelectionListener(selectListener);
		needToGenerateScriptButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));


	}

	protected void createArgumentsOption(Composite argumentsTop) {
		Composite browseTop = new Composite(argumentsTop, SWT.NONE);
		browseTop.setLayout(new GridLayout(1, false));
		GridData browseData = new GridData(GridData.FILL_HORIZONTAL);
		browseTop.setLayoutData(browseData);


		Label suppFileLabel = new Label(browseTop, SWT.NONE);
		suppFileLabel.setText(Messages.getString("SystemTapOptionsTab.SelectArguments")); //$NON-NLS-1$

		arguments = new Text(browseTop,SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 200;
		arguments.setLayoutData(gd);
		arguments.addModifyListener(modifyListener);


		Button probeFunctionButton = createPushButton(browseTop, Messages.getString("SystemTapOptionsTab.ProbeFunction"), null);  //$NON-NLS-1$
		probeFunctionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				arguments.setText(arguments.getText() + " process(\"" + binaryFile.getText() + "\").function(\"\")"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	protected void createFileOption(Composite top) {
		Composite browseTop = new Composite(top, SWT.NONE);
		browseTop.setLayout(new GridLayout(4, false));
		browseTop.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label suppFileLabel = new Label(browseTop, SWT.NONE);
		suppFileLabel.setText(Messages.getString("SystemTapOptionsTab.ScriptSelector")); //$NON-NLS-1$

		scriptFile = new Text(browseTop, SWT.BORDER);
		scriptFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		scriptFile.addModifyListener(modifyListener);

		workspaceBrowseButton = createPushButton(browseTop, Messages.getString("SystemTapOptionsTab.WorkspaceButton"), null);  //$NON-NLS-1$
		workspaceBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider());
				dialog.setTitle(Messages.getString("SystemTapOptionsTab.ResourceButton"));  //$NON-NLS-1$
				dialog.setMessage(Messages.getString("SystemTapOptionsTab.SuppresionsFile"));  //$NON-NLS-1$
				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
				dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
				if (dialog.open() == IDialogConstants.OK_ID) {
					IResource resource = (IResource) dialog.getFirstResult();
					String arg = resource.getFullPath().toString();
					scriptFile.setText(workspacePath + arg);
				}
			}
		});

		fileBrowseButton = createPushButton(browseTop, Messages.getString("SystemTapOptionsTab.FileSystem"), null); //$NON-NLS-1$
		fileBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filePath = scriptFile.getText();
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				filePath = dialog.open();
				if (filePath != null) {
					scriptFile.setText(filePath);
				}
			}
		});


		Label binaryFileLabel = new Label(browseTop, SWT.NONE);
		binaryFileLabel.setText(Messages.getString("SystemTapOptionsTab.SelectBinary")); //$NON-NLS-1$

		binaryFile = new Text(browseTop, SWT.BORDER);
		binaryFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		binaryFile.addModifyListener(modifyListener);


		Button workspaceBrowseButton2 = createPushButton(browseTop, Messages.getString("SystemTapOptionsTab.WorkspaceButton2"), null);  //$NON-NLS-1$
		workspaceBrowseButton2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider());
				dialog.setTitle(Messages.getString("SystemTapOptionsTab.SelectResource"));  //$NON-NLS-1$
				dialog.setMessage(Messages.getString("SystemTapOptionsTab.SelectSuppressions"));  //$NON-NLS-1$
				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
				dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
				if (dialog.open() == IDialogConstants.OK_ID) {
					IResource resource = (IResource) dialog.getFirstResult();
					String arg = resource.getFullPath().toString();
					binaryFile.setText(workspacePath + arg);
				}
			}
		});


		Button fileBrowseButton2 = createPushButton(browseTop, Messages.getString("SystemTapOptionsTab.BrowseFiles"), null); //$NON-NLS-1$
		fileBrowseButton2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filePath = binaryFile.getText();
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				filePath = dialog.open();
				if (filePath != null) {
					File file = new File(filePath);
					if (file.exists()) binaryFile.setText(filePath);
				}
			}
		});


		Label outputFileLabel = new Label(browseTop, SWT.NONE);
		outputFileLabel.setText(Messages.getString("SystemTapOptionsTab.SelectOutput")); //$NON-NLS-1$

		outputFile = new Text(browseTop, SWT.BORDER);
		outputFile.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		outputFile.addModifyListener(modifyListenerOutput);
		outputFile.addFocusListener(focusListener);

		Button workspaceBrowseButton3 = createPushButton(browseTop, Messages.getString("SystemTapOptionsTab.WorkspaceButton2"), null);  //$NON-NLS-1$
		workspaceBrowseButton3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), new WorkbenchLabelProvider(), new WorkbenchContentProvider());
				dialog.setTitle(Messages.getString("SystemTapOptionsTab.SelectResource"));  //$NON-NLS-1$
				dialog.setMessage(Messages.getString("SystemTapOptionsTab.SelectSuppressions"));  //$NON-NLS-1$
				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
				dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
				if (dialog.open() == IDialogConstants.OK_ID) {
					IResource resource = (IResource) dialog.getFirstResult();
					String arg = resource.getFullPath().toString();
					outputFile.setText(workspacePath + arg);
					checkOverwrite();
					updateLaunchConfigurationDialog();

				}
			}
		});

		Button fileBrowseButton3 = createPushButton(browseTop, Messages.getString("SystemTapOptionsTab.BrowseFiles"), null); //$NON-NLS-1$
		fileBrowseButton3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String filePath = outputFile.getText();
				FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
				filePath = dialog.open();
				if (filePath != null) {
					outputFile.setText(filePath);
					checkOverwrite();
					updateLaunchConfigurationDialog();
				}
			}
		});

		useColourButton = new Button(browseTop, SWT.CHECK);
		useColourButton.setText(Messages.getString("SystemTapOptionsTab.ColourCodes"));  //$NON-NLS-1$
		useColourButton.addSelectionListener(selectListener);
		useColourButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}



	protected void createCommandOption(Composite top) {
		Composite browseTop = new Composite(top, SWT.NONE);
		browseTop.setLayout(new GridLayout(3, false));
		GridData browseData = new GridData(GridData.FILL_HORIZONTAL);
		browseTop.setLayoutData(browseData);

		Composite buttonsTop = new Composite(top, SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		gl.horizontalSpacing = PluginConstants.SYSTEMTAP_OPTIONS_TAB_HORIZONTAL_SPACING;
		buttonsTop.setLayout( gl );
		GridData buttonsData = new GridData(SWT.CENTER, SWT.BEGINNING, true, true);
		buttonsData.heightHint = 400;
		buttonsTop.setLayoutData(buttonsData);


		button_k = new Button(buttonsTop, SWT.CHECK);
		button_k.setText(Messages.getString("SystemTapOptionsTab.20")); //$NON-NLS-1$
		button_k.addSelectionListener(selectListener);
		button_k.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_k.setToolTipText(
				Messages.getString("SystemTapOptionsTab.5") + //$NON-NLS-1$
				Messages.getString("SystemTapOptionsTab.6") + //$NON-NLS-1$
				Messages.getString("SystemTapOptionsTab.7")); //$NON-NLS-1$

		button_g = new Button(buttonsTop, SWT.CHECK);
		button_g.setText(Messages.getString("SystemTapOptionsTab.21")); //$NON-NLS-1$
		button_g.addSelectionListener(selectListener);
		button_g.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_g.setToolTipText(
				Messages.getString("SystemTapOptionsTab.8") + //$NON-NLS-1$
				Messages.getString("SystemTapOptionsTab.9")); //$NON-NLS-1$

		button_P = new Button(buttonsTop, SWT.CHECK);
		button_P.setText(Messages.getString("SystemTapOptionsTab.22")); //$NON-NLS-1$
		button_P.addSelectionListener(selectListener);
		button_P.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_P.setToolTipText(
				Messages.getString("SystemTapOptionsTab.10") + //$NON-NLS-1$
			    Messages.getString("SystemTapOptionsTab.11")); //$NON-NLS-1$

		button_u = new Button(buttonsTop, SWT.CHECK);
		button_u.setText(Messages.getString("SystemTapOptionsTab.23")); //$NON-NLS-1$
		button_u.addSelectionListener(selectListener);
		button_u.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_u.setToolTipText(
				Messages.getString("SystemTapOptionsTab.12")); //$NON-NLS-1$

		button_w = new Button(buttonsTop, SWT.CHECK);
		button_w.setText(Messages.getString("SystemTapOptionsTab.24")); //$NON-NLS-1$
		button_w.addSelectionListener(selectListener);
		button_w.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_w.setToolTipText(
				Messages.getString("SystemTapOptionsTab.13")+ //$NON-NLS-1$
				Messages.getString("SystemTapOptionsTab.14")); //$NON-NLS-1$

		button_b = new Button(buttonsTop, SWT.CHECK);
		button_b.setText(Messages.getString("SystemTapOptionsTab.25")); //$NON-NLS-1$
		button_b.addSelectionListener(selectListener);
		button_b.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_b.setToolTipText(
				Messages.getString("SystemTapOptionsTab.15")); //$NON-NLS-1$

		button_t = new Button(buttonsTop, SWT.CHECK);
		button_t.setText(Messages.getString("SystemTapOptionsTab.26")); //$NON-NLS-1$
		button_t.addSelectionListener(selectListener);
		button_t.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_t.setToolTipText(
				Messages.getString("SystemTapOptionsTab.16") + //$NON-NLS-1$
			    Messages.getString("SystemTapOptionsTab.17")); //$NON-NLS-1$

		button_F = new Button(buttonsTop, SWT.CHECK);
		button_F.setText(Messages.getString("SystemTapOptionsTab.LeaveProbesRunning")); //$NON-NLS-1$
		button_F.addSelectionListener(selectListener);
		button_F.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_F.setToolTipText(
				Messages.getString("SystemTapOptionsTab.27") + //$NON-NLS-1$
				Messages.getString("SystemTapOptionsTab.28") + //$NON-NLS-1$
				Messages.getString("SystemTapOptionsTab.29")); //$NON-NLS-1$

		button_skip_badvars = new Button(buttonsTop, SWT.CHECK);
		button_skip_badvars.setText(Messages.getString("SystemTapOptionsTab.IgnoreBadVars")); //$NON-NLS-1$
		button_skip_badvars.addSelectionListener(selectListener);
		button_skip_badvars.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_skip_badvars.setToolTipText(
				Messages.getString("SystemTapOptionsTab.30")); //$NON-NLS-1$

		button_ignore_dwarf = new Button(buttonsTop, SWT.CHECK);
		button_ignore_dwarf.setText(Messages.getString("SystemTapOptionsTab.ForTesting")); //$NON-NLS-1$
		button_ignore_dwarf.addSelectionListener(selectListener);
		button_ignore_dwarf.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_ignore_dwarf.setToolTipText(
				Messages.getString("SystemTapOptionsTab.31") + //$NON-NLS-1$
				Messages.getString("SystemTapOptionsTab.32")); //$NON-NLS-1$

		button_q = new Button(buttonsTop, SWT.CHECK);
		button_q.setText(Messages.getString("SystemTapOptionsTab.Button_qInfo")); //$NON-NLS-1$
		button_q.addSelectionListener(selectListener);
		button_q.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_q.setToolTipText(Messages.getString("SystemTapOptionsTab.33")); //$NON-NLS-1$

		Composite button_p_Spinner_Top = new Composite(buttonsTop, SWT.NONE);
		button_p_Spinner_Top.setLayout(new GridLayout(3, false));
		Label button_p_Spinner_Label = new Label(button_p_Spinner_Top, SWT.NONE);
		button_p_Spinner_Label.setText(Messages.getString("SystemTapOptionsTab.19")); //$NON-NLS-1$
		button_p_Spinner = new Spinner(button_p_Spinner_Top, SWT.BORDER);
		button_p_Spinner.setMaximum(Integer.MAX_VALUE);
		button_p_Spinner.addModifyListener(modifyListener);
		button_p_Spinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_p_Spinner_Label.setToolTipText(
				Messages.getString("SystemTapOptionsTab.34") + //$NON-NLS-1$
				Messages.getString("SystemTapOptionsTab.35") + //$NON-NLS-1$
				Messages.getString("SystemTapOptionsTab.36")); //$NON-NLS-1$

		Composite button_s_Spinner_Top = new Composite(buttonsTop, SWT.NONE);
		button_s_Spinner_Top.setLayout(new GridLayout(2, false));
		Label button_s_Spinner_Label = new Label(button_s_Spinner_Top, SWT.NONE);
		button_s_Spinner_Label.setText(Messages.getString("SystemTapOptionsTab.BufferWith")); //$NON-NLS-1$
		button_s_Spinner = new Spinner(button_s_Spinner_Top, SWT.BORDER);
		button_s_Spinner.setMaximum(Integer.MAX_VALUE);
		button_s_Spinner.addModifyListener(modifyListener);
		button_s_Spinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_s_Spinner_Label.setToolTipText(
				Messages.getString("SystemTapOptionsTab.37") + //$NON-NLS-1$
				Messages.getString("SystemTapOptionsTab.38")); //$NON-NLS-1$

		Composite button_x_Spinner_Top = new Composite(buttonsTop, SWT.NONE);
		button_x_Spinner_Top.setLayout(new GridLayout(2, false));
		Label button_x_Spinner_Label = new Label(button_x_Spinner_Top, SWT.NONE);
		button_x_Spinner_Label.setText(Messages.getString("SystemTapOptionsTab.TargetPID")); //$NON-NLS-1$
		button_x_Spinner = new Spinner(button_x_Spinner_Top, SWT.BORDER);
		button_x_Spinner.setMaximum(Integer.MAX_VALUE);
		button_x_Spinner.addModifyListener(modifyListener);
		button_x_Spinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_x_Spinner_Label.setToolTipText(
				Messages.getString("SystemTapOptionsTab.39") + //$NON-NLS-1$
				Messages.getString("SystemTapOptionsTab.40")); //$NON-NLS-1$

		Composite button_v_Spinner_Top = new Composite(buttonsTop, SWT.NONE);
		button_v_Spinner_Top.setLayout(new GridLayout(2, false));
		Label button_v_Spinner_Label = new Label(button_v_Spinner_Top, SWT.NONE);
		button_v_Spinner_Label.setText(Messages.getString("SystemTapOptionsTab.18")); //$NON-NLS-1$
		button_v_Spinner = new Spinner(button_v_Spinner_Top, SWT.BORDER);
		button_v_Spinner.setMaximum(3);
		button_v_Spinner.addModifyListener(modifyListener);
		button_v_Spinner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_v_Spinner_Label.setToolTipText(
				Messages.getString("SystemTapOptionsTab.2") + //$NON-NLS-1$
				Messages.getString("SystemTapOptionsTab.4")); //$NON-NLS-1$

		button_graphicsMode = new Button(buttonsTop, SWT.CHECK);
		button_graphicsMode.setText(Messages.getString("SystemTapOptionsTab.3")); //$NON-NLS-1$
		button_graphicsMode.addSelectionListener(graphicsModeListener);
		button_graphicsMode.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		button_graphicsMode.setToolTipText(
				Messages.getString("SystemTapOptionsTab.41")); //$NON-NLS-1$

		Label button_D_label = new Label(buttonsTop, SWT.NONE);
		button_D_label.setText(Messages.getString("SystemTapOptionsTab.PreprocessorDirective")); //$NON-NLS-1$
		button_D_text = new Text(buttonsTop, SWT.BORDER);
		button_D_text.setLayoutData(new GridData(200,15));
		button_D_text.addModifyListener(modifyListener);
		button_D_label.setToolTipText(
					  Messages.getString("SystemTapOptionsTab.42") + //$NON-NLS-1$
					  Messages.getString("SystemTapOptionsTab.43")); //$NON-NLS-1$



	}


	@Override
	public String getName() {
		return Messages.getString("SystemTapOptionsTab.MainTabName"); //$NON-NLS-1$
	}

	protected Shell getActiveWorkbenchShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}


	private IBinary chooseBinary(IBinary[] binaries) {
		ILabelProvider programLabelProvider = new CElementLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IBinary) {
					IBinary bin = (IBinary)element;
					StringBuffer name = new StringBuffer();
					name.append(bin.getPath().lastSegment());
					return name.toString();
				}
				return super.getText(element);
			}
		};

		ILabelProvider qualifierLabelProvider = new CElementLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IBinary) {
					IBinary bin = (IBinary)element;
					StringBuffer name = new StringBuffer();
					name.append(bin.getCPU() + (bin.isLittleEndian() ? "le" : "be")); //$NON-NLS-1$ //$NON-NLS-2$
					name.append(" - "); //$NON-NLS-1$
					name.append(bin.getPath().toString());
					return name.toString();
				}
				return super.getText(element);
			}
		};

		TwoPaneElementSelector dialog = new TwoPaneElementSelector(getActiveWorkbenchShell(), programLabelProvider, qualifierLabelProvider);
		dialog.setElements(binaries);
		dialog.setTitle(Messages.getString("SystemtTapOptionsTab.Callgraph")); //$NON-NLS-1$
		dialog.setMessage(Messages.getString("SystemtTapOptionsTab.Choose_a_local_application")); //$NON-NLS-1$
		dialog.setUpperListLabel(Messages.getString("SystemtTapOptionsTab.Binaries")); //$NON-NLS-1$
		dialog.setLowerListLabel(Messages.getString("SystemtTapOptionsTab.Qualifier")); //$NON-NLS-1$
		dialog.setMultipleSelection(false);
		if (dialog.open() == Window.OK) {
			return (IBinary) dialog.getFirstResult();
		}

		return null;
	}

	private IBinary getBinary(ILaunchConfiguration config) {
		try {
			ICProject project =  CDebugUtils.verifyCProject(config);
			IBinary[] binaries = project.getBinaryContainer().getBinaries();
			if (binaries != null && binaries.length > 0) {
				if (binaries.length == 1 && binaries[0] != null) {
					return binaries[0];
				} else
					return chooseBinary(binaries);
			}
			return null;
		} catch (CoreException e) {
			return null;
		}
	}



	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {


		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IPath location = root.getLocation();
		workspacePath = location.toString();

		try {
			button_k.setSelection(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_KEEP_TEMPORARY, LaunchConfigurationConstants.DEFAULT_COMMAND_KEEP_TEMPORARY));
			button_u.setSelection(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_NO_CODE_ELISION, LaunchConfigurationConstants.DEFAULT_COMMAND_NO_CODE_ELISION));
			button_w.setSelection(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_DISABLE_WARNINGS, LaunchConfigurationConstants.DEFAULT_COMMAND_DISABLE_WARNINGS));
			button_b.setSelection(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_BULK_MODE, LaunchConfigurationConstants.DEFAULT_COMMAND_BULK_MODE));
			button_g.setSelection(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_GURU, LaunchConfigurationConstants.DEFAULT_COMMAND_GURU));
			button_P.setSelection(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_PROLOGUE_SEARCH, LaunchConfigurationConstants.DEFAULT_COMMAND_PROLOGUE_SEARCH));
			button_t.setSelection(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_TIMING_INFO, LaunchConfigurationConstants.DEFAULT_COMMAND_TIMING_INFO));
			button_skip_badvars.setSelection(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_SKIP_BADVARS, LaunchConfigurationConstants.DEFAULT_COMMAND_SKIP_BADVARS));
			button_ignore_dwarf.setSelection(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_IGNORE_DWARF, LaunchConfigurationConstants.DEFAULT_COMMAND_IGNORE_DWARF));
			button_q.setSelection(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_TAPSET_COVERAGE, LaunchConfigurationConstants.DEFAULT_COMMAND_TAPSET_COVERAGE));
			button_F.setSelection(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_LEAVE_RUNNING, LaunchConfigurationConstants.DEFAULT_COMMAND_LEAVE_RUNNING));
			button_s_Spinner.setSelection(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_BUFFER_BYTES, LaunchConfigurationConstants.DEFAULT_COMMAND_BUFFER_BYTES));
			button_x_Spinner.setSelection(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_TARGET_PID, LaunchConfigurationConstants.DEFAULT_COMMAND_TARGET_PID));
			button_v_Spinner.setSelection(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_VERBOSE, LaunchConfigurationConstants.DEFAULT_COMMAND_VERBOSE));
			button_p_Spinner.setSelection(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_PASS, LaunchConfigurationConstants.DEFAULT_COMMAND_PASS));

			button_D_text.setText(configuration.getAttribute(LaunchConfigurationConstants.COMMAND_C_DIRECTIVES, LaunchConfigurationConstants.DEFAULT_COMMAND_C_DIRECTIVES));
			binaryFile.setText(configuration.getAttribute(LaunchConfigurationConstants.BINARY_PATH, LaunchConfigurationConstants.DEFAULT_BINARY_PATH));
			scriptFile.setText(configuration.getAttribute(LaunchConfigurationConstants.SCRIPT_PATH, LaunchConfigurationConstants.DEFAULT_SCRIPT_PATH));
			outputFile.setText(configuration.getAttribute(LaunchConfigurationConstants.OUTPUT_PATH, LaunchConfigurationConstants.DEFAULT_OUTPUT_PATH));
			arguments.setText(configuration.getAttribute(LaunchConfigurationConstants.ARGUMENTS, LaunchConfigurationConstants.DEFAULT_ARGUMENTS));
			binaryArguments.setText(configuration.getAttribute(LaunchConfigurationConstants.BINARY_ARGUMENTS, LaunchConfigurationConstants.DEFAULT_BINARY_ARGUMENTS));

			parser.setText(configuration.getAttribute(LaunchConfigurationConstants.PARSER_CLASS, LaunchConfigurationConstants.DEFAULT_PARSER_CLASS));
			viewer.setText(configuration.getAttribute(LaunchConfigurationConstants.VIEW_CLASS, LaunchConfigurationConstants.DEFAULT_VIEW_CLASS));

			if (generatedScript != null){
				generatedScript.setText(configuration.getAttribute(LaunchConfigurationConstants.GENERATED_SCRIPT, LaunchConfigurationConstants.DEFAULT_GENERATED_SCRIPT));
				needToGenerateScriptButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.NEED_TO_GENERATE, LaunchConfigurationConstants.DEFAULT_NEED_TO_GENERATE));
			}
			useColourButton.setSelection(configuration.getAttribute(LaunchConfigurationConstants.USE_COLOUR, LaunchConfigurationConstants.DEFAULT_USE_COLOUR));

		} catch (CoreException e) {
			e.printStackTrace();
		}


	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		getControl().setRedraw(false);

		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_KEEP_TEMPORARY, button_k.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_GURU, button_g.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_PROLOGUE_SEARCH, button_P.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_NO_CODE_ELISION, button_u.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_DISABLE_WARNINGS, button_w.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_BULK_MODE, button_b.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_TIMING_INFO, button_t.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_SKIP_BADVARS, button_skip_badvars.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_IGNORE_DWARF, button_ignore_dwarf.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_TAPSET_COVERAGE, button_q.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_LEAVE_RUNNING, button_F.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_PASS, button_p_Spinner.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_BUFFER_BYTES, button_s_Spinner.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_TARGET_PID, button_x_Spinner.getSelection());
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_VERBOSE, button_v_Spinner.getSelection());

		configuration.setAttribute(LaunchConfigurationConstants.PARSER_CLASS, parser.getText());
		configuration.setAttribute(LaunchConfigurationConstants.VIEW_CLASS, viewer.getText());

		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_C_DIRECTIVES, button_D_text.getText());
		configuration.setAttribute(LaunchConfigurationConstants.BINARY_PATH, binaryFile.getText());
		configuration.setAttribute(LaunchConfigurationConstants.SCRIPT_PATH, scriptFile.getText());
		configuration.setAttribute(LaunchConfigurationConstants.ARGUMENTS, arguments.getText());
		configuration.setAttribute(LaunchConfigurationConstants.BINARY_ARGUMENTS, binaryArguments.getText());
		configuration.setAttribute(LaunchConfigurationConstants.OUTPUT_PATH, outputFile.getText());

		if (generatedScript != null){
			configuration.setAttribute(LaunchConfigurationConstants.GENERATED_SCRIPT, generatedScript.getText());
			configuration.setAttribute(LaunchConfigurationConstants.NEED_TO_GENERATE, needToGenerateScriptButton.getSelection());
		}

		configuration.setAttribute(LaunchConfigurationConstants.USE_COLOUR, useColourButton.getSelection());

		if (button_graphicsMode.getSelection()){
			scriptFile.setEnabled(false);
			workspaceBrowseButton.setEnabled(false);
			fileBrowseButton.setEnabled(false);

		}else{
			scriptFile.setEnabled(true);
			workspaceBrowseButton.setEnabled(true);
			fileBrowseButton.setEnabled(true);
		}

		if (changeOverwrite) {
			if (needsOverwritePermission && overwritePermission || !needsOverwritePermission) {
				configuration.setAttribute(LaunchConfigurationConstants.OVERWRITE, true);
			}
			else {
				configuration.setAttribute(LaunchConfigurationConstants.OVERWRITE, false);
			}
			changeOverwrite = false;
		}

		getControl().setRedraw(true);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_VERBOSE,LaunchConfigurationConstants.DEFAULT_COMMAND_VERBOSE);
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_KEEP_TEMPORARY,LaunchConfigurationConstants.DEFAULT_COMMAND_KEEP_TEMPORARY);
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_GURU,LaunchConfigurationConstants.DEFAULT_COMMAND_GURU);
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_PROLOGUE_SEARCH,LaunchConfigurationConstants.DEFAULT_COMMAND_PROLOGUE_SEARCH);
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_NO_CODE_ELISION,LaunchConfigurationConstants.DEFAULT_COMMAND_NO_CODE_ELISION);
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_DISABLE_WARNINGS,LaunchConfigurationConstants.DEFAULT_COMMAND_DISABLE_WARNINGS);
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_BULK_MODE,LaunchConfigurationConstants.DEFAULT_COMMAND_BULK_MODE);
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_TIMING_INFO,LaunchConfigurationConstants.DEFAULT_COMMAND_TIMING_INFO);
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_SKIP_BADVARS,LaunchConfigurationConstants.DEFAULT_COMMAND_SKIP_BADVARS);
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_IGNORE_DWARF,LaunchConfigurationConstants.DEFAULT_COMMAND_IGNORE_DWARF);
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_TAPSET_COVERAGE,LaunchConfigurationConstants.DEFAULT_COMMAND_TAPSET_COVERAGE);
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_LEAVE_RUNNING,LaunchConfigurationConstants.DEFAULT_COMMAND_LEAVE_RUNNING);
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_PASS,LaunchConfigurationConstants.DEFAULT_COMMAND_PASS);
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_BUFFER_BYTES,LaunchConfigurationConstants.DEFAULT_COMMAND_BUFFER_BYTES);
		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_TARGET_PID,LaunchConfigurationConstants.DEFAULT_COMMAND_TARGET_PID);

		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_C_DIRECTIVES,LaunchConfigurationConstants.DEFAULT_COMMAND_C_DIRECTIVES);
		configuration.setAttribute(LaunchConfigurationConstants.BINARY_PATH,LaunchConfigurationConstants.DEFAULT_BINARY_PATH);
		configuration.setAttribute(LaunchConfigurationConstants.SCRIPT_PATH,LaunchConfigurationConstants.DEFAULT_SCRIPT_PATH);
		configuration.setAttribute(LaunchConfigurationConstants.OUTPUT_PATH,LaunchConfigurationConstants.DEFAULT_OUTPUT_PATH);
		configuration.setAttribute(LaunchConfigurationConstants.ARGUMENTS,LaunchConfigurationConstants.DEFAULT_ARGUMENTS);
		configuration.setAttribute(LaunchConfigurationConstants.BINARY_ARGUMENTS,LaunchConfigurationConstants.DEFAULT_BINARY_ARGUMENTS);

		configuration.setAttribute(LaunchConfigurationConstants.GENERATED_SCRIPT, LaunchConfigurationConstants.DEFAULT_GENERATED_SCRIPT);
		configuration.setAttribute(LaunchConfigurationConstants.NEED_TO_GENERATE, LaunchConfigurationConstants.DEFAULT_NEED_TO_GENERATE);
		configuration.setAttribute(LaunchConfigurationConstants.PARSER_CLASS, LaunchConfigurationConstants.DEFAULT_PARSER_CLASS);
		configuration.setAttribute(LaunchConfigurationConstants.VIEW_CLASS, LaunchConfigurationConstants.DEFAULT_VIEW_CLASS);



		configuration.setAttribute(LaunchConfigurationConstants.USE_COLOUR, LaunchConfigurationConstants.DEFAULT_USE_COLOUR);

		configuration.setAttribute(LaunchConfigurationConstants.COMMAND_LIST, ConfigurationOptionsSetter.setOptions(configuration));


		ICElement cElement = null;
		cElement = getContext(configuration, getPlatform(configuration));
		if (cElement != null) {
			initializeCProject(cElement, configuration);
		} else {
			// don't want to remember the interim value from before
			configuration.setMappedResources(null);
		}

		IBinary bin = null;
		bin = getBinary(configuration);
		if (bin != null) {
			String programName = bin.getResource().getProjectRelativePath().toString();
			configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, programName);
			configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);

			LaunchStapGraph launch = new LaunchStapGraph();
			launch.setTestMode(true); //Do not run callgraph
			launch.launch(bin, "", configuration); //$NON-NLS-1$
		}
	}


	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		// Check that the major options are sane
		boolean valid = true;
		try {
			String sPath = launchConfig.getAttribute(
					LaunchConfigurationConstants.SCRIPT_PATH,
					LaunchConfigurationConstants.DEFAULT_SCRIPT_PATH);

			File script = new File(sPath);

			if (sPath.equals(LaunchConfigurationConstants.DEFAULT_SCRIPT_PATH) || !script.exists()) {
				//No script path specified or no such script exists
				valid = false;
			}

		} catch (CoreException e) {
			e.printStackTrace();
		}

		return valid;
	}

	private String getUsefulLabel(Object element) {
		if (element instanceof IConfigurationElement) {
			Object o = ((IConfigurationElement) element).getParent();
			if (o instanceof IExtension) {
				IExtension e = (IExtension) ((IConfigurationElement) element).getParent();
				return e.getUniqueIdentifier();
			}
	}
	return Messages.getString("SystemTapOptionsTab.1"); //$NON-NLS-1$

	}

	private static class ListLabelProvider implements ILabelProvider {

		@Override
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public String getText(Object element) {
			if (element instanceof IConfigurationElement) {
				Object o = ((IConfigurationElement) element).getParent();
				if (o instanceof IExtension) {
					IExtension e = (IExtension) ((IConfigurationElement) element).getParent();
					return e.getLabel();
				}
				else if (o instanceof IConfigurationElement) {
					IConfigurationElement e = (IConfigurationElement) ((IConfigurationElement) element).getParent();
					return e.getName();
				}

		}
		return Messages.getString("SystemTapOptionsTab.46");		} //$NON-NLS-1$

		@Override
		public void addListener(ILabelProviderListener listener) {

		}

		@Override
		public void dispose() {
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

	}
}
