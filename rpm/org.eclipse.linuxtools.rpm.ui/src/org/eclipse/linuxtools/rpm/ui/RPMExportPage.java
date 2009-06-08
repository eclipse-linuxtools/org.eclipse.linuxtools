/*
 ** (c) 2004, 2005 Red Hat, Inc.
 *
 * This program is open source software licensed under the 
 * Eclipse Public License ver. 1
*/

/**
 * @author pmuldoon
 * @version 1.0
 *
 * S/RPM  export  Defines the page that is shown to the user when they choose
 * to export to an S/RPM. Defines the UI elements shown, and the basic validation 
 * 
 */
package org.eclipse.linuxtools.rpm.ui;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.linuxtools.rpm.core.IRPMProject;
import org.eclipse.linuxtools.rpm.core.RPMProjectFactory;
import org.eclipse.linuxtools.rpm.core.RPMProjectNature;
import org.eclipse.linuxtools.rpm.ui.util.ExceptionHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;


/**
 *
 * RPMExportPage. Called by RPMExportWizard.  Class can not be subclassed
 *extends WizardPage for the RPM export wizard, implements Listener
 *
 */
public class RPMExportPage extends WizardPage implements Listener {
	private static final String VALID_CHARS = "0123456789.";  //$NON-NLS-1$
	
	// Checkbox Buttons
	private Button exportBinary;
	private Button exportSource;
	
	// Version and release fields
	private Text rpmVersion;
	private Text rpmRelease;
	
	// The current selection
	private IStructuredSelection selection;

	//Composite Project Listbox control	
	private List projectList;
	
	//Spec file field
	private Text specFileField;
	
	//Patch grid
	private Group patchNeedHintGrid;
	
	//The currently selected RPM project
	private IRPMProject rpmProject;
	
	//Is a patch needed?
	private boolean patchNeeded = false;
	
	public RPMExportPage(IStructuredSelection currentSelection) {
		super(Messages.getString("RPMExportPage.Export_SRPM"), //$NON-NLS-1$
				  Messages.getString("RPMExportPage.Export_SRPM_from_project"), null); //$NON-NLS-1$ //$NON-NLS-2$
		setDescription(Messages.getString("RPMExportPage.Select_project_export")); //$NON-NLS-1$
		setPageComplete(true);
		selection = currentSelection;
	}
	
	public IRPMProject getSelectedRPMProject() {
		return rpmProject;
	}
	
	public IFile getSelectedSpecFile() {
		Path newSpecFilePath = 
			new Path(specFileField.getText());
		return rpmProject.getProject().getFile(newSpecFilePath);
	}
	
	public String getSelectedVersion() {
		return rpmVersion.getText();
	}
	
	public String getSelectedRelease() {
		return rpmRelease.getText();
	}

	public int getExportType() {
		int exportType = 0;
		if(exportBinary.getSelection() && exportSource.getSelection()) {
			exportType = IRPMUIConstants.BUILD_ALL;
		} else if(exportBinary.getSelection()) {
			exportType = IRPMUIConstants.BUILD_BINARY; 
		} else if(exportSource.getSelection()) {
			exportType = IRPMUIConstants.BUILD_SOURCE;
		}
		return exportType;
	}
	
	private String getSelectedProjectName() {
		String projSelect = null;
		String[] projDetails = projectList.getSelection();

		if (projDetails.length > 0) {
			projSelect = projDetails[0];
		}
		return projSelect;
	}
	
	private void setPatchNeeded(boolean patchDelta) {
		patchNeeded = patchDelta;
	}
	
	private boolean isPatchNeeded() {
		return patchNeeded;
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(Composite)
	 *
	 * Parent control. Creates the listbox, Destination box, and options box
	 *
	 */
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);

		// Create a layout for the wizard page
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		// Create contols on the page
		createProjectBox(composite);
		createSpecFileFields(composite);
		createSpacer(composite);
		createExportTypeControls(composite);
		createPatchHint(composite);
		
		// Fill in the fields
		setSpecFileField();

		// Check if the project has changed
		// and therefore the project needs a patch
			setPatchNeeded(false);
			setPageComplete(false);
	}

	private void createExportTypeControls(Composite parent) { 
		//Create a group for the control and set up the layout.
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setText(Messages.getString("RPMExportPage.Composite_Export_Type")); //$NON-NLS-1$
		group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));

		// Create the export binary checkbox
		exportBinary = new Button(group, SWT.CHECK);
		exportBinary.setText(Messages.getString("RPMExportPage.Export_Binary")); //$NON-NLS-1$
		exportBinary.setSelection(true);
		exportBinary.setToolTipText(Messages.getString(
				"RPMExportPage.toolTip_Export_Binary")); //$NON-NLS-1$
		
		// Create the export source checkbox
		exportSource = new Button(group, SWT.CHECK);
		exportSource.setText(Messages.getString("RPMExportPage.Export_Source")); //$NON-NLS-1$
		exportSource.setSelection(true);
		exportSource.setToolTipText(Messages.getString(
				"RPMExportPage.toolTip_Export_Source")); //$NON-NLS-1$
		
		SelectionListener listener = new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				handleEvent(null);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				handleEvent(null);
			}
		};
		exportBinary.addSelectionListener(listener);
		exportSource.addSelectionListener(listener);
	}

	/**
	 * Method createProjectBox.
	 * @param parent - parent widget
	 *
	 * Create a list box and populate it with
	 * the list of current projects in the workspace
	 */
	private void createProjectBox(Composite parent) {
		// Creates a control that enumerates all the projects in the current 
		// Workspace and places them in a listbox. 
		IProject[] internalProjectList;

		//Get the current workspace root.
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
															  .getRoot();

		//Create a group for the control and set up the layout. Even though it is a single control, 
		// we want to seperate it from the other widgets on the wizard dialog box
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setText(Messages.getString("RPMPage.Select_a_project")); //$NON-NLS-1$
		group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));

		// Creata a new SWT listbox. Only allow single selection of items	 
		// Set up the layout data
		projectList = new List(group, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		projectList.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));
		projectList.setToolTipText(Messages.getString(
				"SRPMImportPage.toolTip_project_destination")); //$NON-NLS-1$

		// Set the height to 4 elements high
		GridData projectLayout = new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL);
		projectLayout.heightHint = projectList.getItemHeight() * 4;
		projectList.setLayoutData(projectLayout);

		// From the current Workspace root, get a list of all current projects
		// This should come back to us as an array of IProject.
		internalProjectList = workspaceRoot.getProjects();

		if (internalProjectList.length < 1) {
			projectList.add(Messages.getString(
					"RPMPage.No_RPM_projects_found")); //$NON-NLS-1$
			return;
		}

		// Find the first selected project in the workspace
		Iterator iter = selection.iterator();
		Object selectedObject= null;
		IProject selectedProject = null;
		boolean isSelection = false;
		if (iter.hasNext()) {
			selectedObject = iter.next();
			if (selectedObject instanceof IResource) {
				selectedProject = ((IResource) selectedObject).getProject();
				isSelection = true;
			}
		}

		// Stuff the listbox with the text names of the projects
		// Highlight the currently selected project in the workspace
		int selectedProjectIndex = 0;
		for (int a = 0; a < internalProjectList.length; a++) {
			try {
				if(internalProjectList[a].hasNature(RPMProjectNature.RPM_NATURE_ID)) {
					projectList.add(internalProjectList[a].getName());
					if (isSelection && internalProjectList[a].equals(selectedProject)) {
						selectedProjectIndex = a;
					}
				}
			} catch(CoreException e) {
				ExceptionHandler.handle(e, getShell(),
						Messages.getString("ErrorDialog.title"), e.getMessage());
			}
		}
		projectList.setSelection(selectedProjectIndex);
		try {
			rpmProject = RPMProjectFactory.getRPMProject(internalProjectList[selectedProjectIndex]);
		} catch(CoreException e) {
			ExceptionHandler.handle(e, getShell(),
					Messages.getString("ErrorDialog.title"), e.getMessage());
		}
		
		// Add a listener to the project box
		projectList.addListener(SWT.Selection,
			new Listener() {
				public void handleEvent(Event event) {
					// Reset the RPM project
					int i = projectList.getSelectionIndex();
					if(i != -1) {
								setPatchNeeded(false);
								patchNeedHintGrid.setVisible(false);
					} else {
						rpmProject = null;
						setPatchNeeded(false);
						patchNeedHintGrid.setVisible(false);
					}
					setSpecFileField();
				}
		});
		projectList.addListener(SWT.Modify, this);
	}

	/**
	 * Method setSpecFileComboData
	 * 
	 * Populates the specFile Combo Box
	 * 
	 */
	private void setSpecFileField() {
		if(rpmProject != null) {
			String specFile = 
				rpmProject.getSpecFile().getProjectRelativePath().toOSString();
			specFileField.setText(specFile);
		}
	}

	/**
	 * Method createSpecFileField
	 * @param parent
	 *
	 * Creates the Spec file combo box
	 */
	private void createSpecFileFields(Composite parent) {
		Group specGrid = new Group(parent, SWT.NONE);
		specGrid.setLayout(new GridLayout());
		specGrid.setText(Messages.getString("RPMExportPage.SPEC_file")); //$NON-NLS-1$
		specGrid.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));

		Composite  composite = new Composite(specGrid, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL));

		specFileField = new Text(composite, SWT.BORDER);
		specFileField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleEvent(null);
			}
		});
		specFileField.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				handleEvent(null);
			}
			public void widgetSelected(SelectionEvent e) {
				handleEvent(null);
			}
		});

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		
		specFileField.setLayoutData(gridData);
		specFileField.setToolTipText(Messages.getString(
				"RPMExportPage.toolTip_SpecFile")); //$NON-NLS-1$

		Button rpmBrowseButton = new Button(composite, SWT.PUSH);
		rpmBrowseButton.setToolTipText(Messages.getString(
				"RPMExportPage.toolTip_file_navigator")); //$NON-NLS-1$
		rpmBrowseButton.setText(Messages.getString("RPMPage.Browse")); //$NON-NLS-1$
		rpmBrowseButton.addListener(SWT.Selection,
			new Listener() {
				public void handleEvent(Event event) {
					ResourceSelectionDialog specFileDialog = 
						new ResourceSelectionDialog(getContainer().getShell(),
								rpmProject.getConfiguration().getSpecsFolder(), 
								Messages.getString("RPMExportPage.Select_spec_file"));
					specFileDialog.setBlockOnOpen(true);
					int result = specFileDialog.open();
					if(result == Window.OK) {
						Object[] foo = specFileDialog.getResult();
						if(foo[0] instanceof IFile) {
							IFile newSpecFile = (IFile) foo[0];
							specFileField.setText(newSpecFile.getProjectRelativePath().toOSString());
						}
					}
				}
			});


		Composite versionReleaseComposite = new Composite(specGrid, SWT.NONE);
		GridLayout versionReleaseLayout = new GridLayout();
		versionReleaseLayout.numColumns = 5;
		versionReleaseComposite.setLayout(versionReleaseLayout);
		versionReleaseComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL));

		GridData lineGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL);
		Label line = new Label(versionReleaseComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
		lineGridData.widthHint = 5;
		line.setLayoutData(lineGridData);

		GridData versionGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL);
		new Label(versionReleaseComposite, SWT.NONE).setText(Messages.getString(
				"RPMExportPage.Version")); //$NON-NLS-1$
		rpmVersion = new Text(versionReleaseComposite, SWT.BORDER);
		rpmVersion.setToolTipText(Messages.getString(
				"RPMExportPage.toolTip_Version")); //$NON-NLS-1$
		rpmVersion.setLayoutData(versionGridData);

		GridData releaseGridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL);

		new Label(versionReleaseComposite, SWT.NONE).setText(Messages.getString(
				"RPMExportPage.Release")); //$NON-NLS-1$
		rpmRelease = new Text(versionReleaseComposite, SWT.BORDER);
		rpmRelease.setToolTipText(Messages.getString(
				"RPMExportPage.toolTip_Release")); //$NON-NLS-1$
		rpmRelease.setLayoutData(releaseGridData);
		
		// Set listeners
		ModifyListener modListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				handleEvent(null);
			}
		};
		KeyListener keyListener = new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {
				handleEvent(null);
			}
		};
		rpmVersion.addModifyListener(modListener);
		rpmRelease.addModifyListener(modListener);
		rpmVersion.addKeyListener(keyListener);
		rpmRelease.addKeyListener(keyListener);
	}

	private void createPatchHint(Composite parent) {
		Display display = null;
		patchNeedHintGrid = new Group(parent, SWT.NONE);
		patchNeedHintGrid.setVisible(false);
		patchNeedHintGrid.setLayout(new GridLayout());
		patchNeedHintGrid.setText(Messages.getString("RPMExportPage.groupPatchTitle")); //$NON-NLS-1$
		patchNeedHintGrid.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));

		Composite patchHintComposite = new Composite(patchNeedHintGrid, SWT.NONE);

		GridLayout patchHintLayout = new GridLayout();
		patchHintLayout.numColumns = 2;
		patchHintComposite.setLayout(patchHintLayout);
		patchHintComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL));
		final Image patchHintImage = new Image(display,getClass().getResourceAsStream("redhat-system_tools.png"));
		
		Canvas canvas = new Canvas(patchHintComposite,SWT.NO_REDRAW_RESIZE);
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
			 e.gc.drawImage(patchHintImage,0,0);
			}
		}); 
		new Label(patchHintComposite, SWT.NONE).setText(Messages.getString(
				"RPMExportPage.needPatch_Project")  + //$NON-NLS-1$
			Messages.getString("RPMExportPage.needPatch_desc")); //$NON-NLS-1$
	}

	
	/**
	 * Method createSpacer.
	 * @param parent - parent widget
	 *
	 * Create a generic filler control so that we can dump
	 * controls in a better layout
	 */
	private void createSpacer(Composite parent) {
		Label spacer = new Label(parent, SWT.NONE);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.BEGINNING;
		spacer.setLayoutData(data);
	}

	/**
	 * canFinish()
	 * 
	 * Hot validation. Called to determine whether Finish
	 * button can be set to true
	 * @return boolean. true if finish can be activated
	 */
	public boolean canFinish() {
		// Make sure project has been selected
		if (getSelectedProjectName() == null && rpmProject == null) {
			setDescription(Messages.getString("RPMExportPage.Select_project")); //$NON-NLS-1$
			return false;
		}
		
		// Make sure spec file is selected
		if(specFileField.getText() == "") { //$NON-NLS-1$
			setDescription(Messages.getString("RPMExportPage.Select_spec_file"));
			return false;
		}
		// Make sure spec file exists and path is valid
		else {
			IPath path = rpmProject.getProject().getFullPath().addTrailingSeparator().append(specFileField.getText());
			IFile newSpecFile = rpmProject.getProject().getParent().getFile(path);
			IPath newSpecFilePath = newSpecFile.getFullPath();
			IPath specFolderPath = rpmProject.getConfiguration().getSpecsFolder().getFullPath();
			if(!specFolderPath.isPrefixOf(newSpecFilePath)) {
				setErrorMessage(Messages.getString("RPMExportPage.Not_in_specs"));
				return false;
			}
			if(!newSpecFile.exists()) {
				setErrorMessage(Messages.getString("RPMExportPage.Spec_file_does_not_exist"));
				return false;
			}
		}
			
		// Make sure version/release fields are filled in
		if (!checkVersionReleaseFields()) {
			setDescription(Messages.getString("RPMExportPage.Fill_in_ver_rel")); //$NON-NLS-1$
			return false;
		}
		// Make sure version/release fields are valid
		if(!checkInvalidChars(rpmVersion.getText()) || !checkInvalidChars(rpmRelease.getText())) {
			setErrorMessage(Messages.getString("RPMExportPage.Invalid_ver_rel"));
			return false;
		}
		
		// Make sure either export binary/source is checked
		if (!exportBinary.getSelection() && !exportSource.getSelection()) {
			setErrorMessage(Messages.getString("RPMExportPage.Select_export_type")); //$NON-NLS-1$
			return false;
		}
		
		setDescription(null);
		setErrorMessage(null);
		return true;
	}

	/**
	 * Method checkVersionReleaseFields
	 * 
	 * Check that Version and Release
	 * are not empty
	 * @return  boolean - true if valid
	 */
	private boolean checkVersionReleaseFields() {
		if (!rpmVersion.getText().equals("")) { //$NON-NLS-1$
			if (!rpmRelease.getText().equals("")) { //$NON-NLS-1$
				return true;
			}
		}
		return false;
	}
	
	private boolean checkInvalidChars(String input) {
		char[] inputChars = input.toCharArray();
		for(int i=0; i < inputChars.length; i++) {
			if(VALID_CHARS.indexOf(inputChars[i]) == -1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Method canGoNext()
	 * 
	 * Method to enable Next button
	 * @return
	 */
	public boolean canGoNext() {
		// if a patch is needed, the next button should
		// be enabled
		if (canFinish() && isPatchNeeded()) {
			return true;
		} else {
			return false;
		}
	}

	public void handleEvent(Event e) {
		setPageComplete(canFinish());
	}
}
