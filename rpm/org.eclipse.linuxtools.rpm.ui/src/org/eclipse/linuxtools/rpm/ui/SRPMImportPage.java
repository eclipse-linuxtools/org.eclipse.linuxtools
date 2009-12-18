/*******************************************************************************
 * Copyright (c) 2004-2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.rpm.ui;

import java.io.File;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * RPM GUI import  page. Defines the page the is shown to the user when they choose
 * to export to and RPM. Defines the UI elements shown, and the basic validation (need to add to
 * this)
 * SRPMImportPage. Called by SRPMImportwizard.  Class can not be subclassed
 * extends WizardPage and implements Listener (for events)
 *
 */
public class SRPMImportPage extends WizardPage implements Listener {
	
	/** This is a copy of 
	 * org.eclipse.team.internal.ccvs.ui.wizards.CheckoutAsWizard.NewProjectListener**/
	class NewProjectListener implements IResourceChangeListener {
		private IProject newProject = null;

		/**
		 * @see IResourceChangeListener#resourceChanged(IResourceChangeEvent)
		 */
		public void resourceChanged(IResourceChangeEvent event) {
			IResourceDelta root = event.getDelta();
			IResourceDelta[] projectDeltas = root.getAffectedChildren();
			for (int i = 0; i < projectDeltas.length; i++) {
				IResourceDelta delta = projectDeltas[i];
				IResource resource = delta.getResource();
				if (delta.getKind() == IResourceDelta.ADDED) {
					newProject = (IProject) resource;
				}
			}
		}

		/**
		 * Gets the newProject.
		 * 
		 * @return Returns a IProject
		 */
		public IProject getNewProject() {
			return newProject;
		}
	}

	// GUI Control variables	
	private Combo sourceSRPM;
	private Button intoConfigured;
	private Button intoExisting;
	private List projectList;
	private IStructuredSelection selection;

	static private Vector<String> srpmVector;
	
	/**
	 * @see java.lang.Object#Object()
	 *
	 * Constructor for SRPMImportPage class
	 * @param aWorkbench - Workbench
	 * @param selection - IStructuredSelection
	 */
	public SRPMImportPage(IWorkbench aWorkbench, IStructuredSelection currentSelection) {
		super(Messages.getString("SRPMImportPage.Import_SRPM"), //$NON-NLS-1$
			Messages.getString("SRPMImportPage.Select_project_to_import"), null); //$NON-NLS-1$

		setPageComplete(false);
		setDescription(Messages.getString(
				"SRPMImportPage.Select_project_to_import")); //$NON-NLS-1$
		selection = currentSelection;
	}

	
	private String getSelectedProjectName() {
		String[] selections = projectList.getSelection();
		if (selections.length > 0) {
			return selections[0];
		}
		return null;
	}
	
	private File getSelectedSRPM() {
		String srpmName = sourceSRPM.getText();
		if(srpmName == null || srpmName.equals("")) { //$NON-NLS-1$
			return null;
		}
		return new File(sourceSRPM.getText());
	}

	public void createControl(Composite parent) {
		// Set Page complete to false. Don't allow the user to execute wizard
		// until we have all the required data
		setPageComplete(false);

		// Create a generic composite to hold ui variable
		Composite composite = new Composite(parent, SWT.NULL);

		// Create a layout for the wizard page
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		setControl(composite);

		// Create contols on the page
		createSourceRPMCombo(composite);
		createProjectBox(composite);
	}

	private void createSourceRPMCombo(Composite parent) {
		Group specGrid = new Group(parent, SWT.NONE);
		specGrid.setLayout(new GridLayout());
		specGrid.setText(Messages.getString("SRPMImportPage.SRPM_Name")); //$NON-NLS-1$
		specGrid.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));

		Composite sourceSpecComposite = new Composite(specGrid, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		sourceSpecComposite.setLayout(layout);
		sourceSpecComposite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL |
				GridData.GRAB_HORIZONTAL));

		sourceSRPM = new Combo(sourceSpecComposite, SWT.BORDER);
		sourceSRPM.setToolTipText(Messages.getString(
				"SRPMImportPage.toolTip_SRPM_Name")); //$NON-NLS-1$

		if (srpmVector == null)
			srpmVector = new Vector<String>();
		for (int i = srpmVector.size(); i > 0; i--)
			sourceSRPM.add((srpmVector.elementAt(i - 1)));
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		sourceSRPM.setLayoutData(gridData);
		sourceSRPM.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					handleEvent(null);
				}
			});

		Button srpmBrowse = new Button(sourceSpecComposite, SWT.PUSH);
		srpmBrowse.setToolTipText(Messages.getString(
				"SRPMImportPage.toolTip_Open_file_navigator")); //$NON-NLS-1$
		srpmBrowse.setText(Messages.getString("RPMPage.Browse")); //$NON-NLS-1$
		srpmBrowse.addListener(SWT.Selection,
			new Listener() {
				public void handleEvent(Event event) {
					FileDialog srpmBrowseDialog = new FileDialog(getContainer()
							.getShell(), SWT.OPEN);
					String selectedSRPM_name = srpmBrowseDialog.open();
					if (selectedSRPM_name != null)
					{
						File testSRPMfilename = new File(selectedSRPM_name);
						if (testSRPMfilename.isFile())
							sourceSRPM.setText(selectedSRPM_name);
					}
				}
			});
		srpmBrowse.addListener(SWT.FocusOut, this);
	}

	/**
	 * Method createProjectBox.
	 * @param parent - parent widget
	 *
	 * Create a list box and populate it with
	 * the list of current projects in the workspace
	 * along with adding the option for a configured project
	 */
	private void createProjectBox(Composite parent) {
		// Creates a control that enumerates all the projects in the current 
		// Workspace and places them in a listbox. 
		// Give the option of importing into an existing project or creating a new one
		
		// Declare an array of IProject;
		IProject[] internalProjectList;

		//Get the current workspace root.
		final IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace()
															  .getRoot();

		// Create a group and set up the layout, we want to seperate 
		// project selection from the other widgets on the wizard dialog box
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout());
		group.setText(Messages.getString("SRPMImportPage.import_srpm_into")); //$NON-NLS-1$
		group.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));
		intoExisting = new Button(group, SWT.RADIO);
		intoExisting.setText(Messages.getString("RPMPage.Select_a_project")); //$NON-NLS-1$
		
		// Create a new SWT listbox. Only allow single selection of items	 
		// Set up the layout data
		projectList = new List(group, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		projectList.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL));
		projectList.setToolTipText(Messages.getString(
				"SRPMImportPage.toolTip_project_destination")); //$NON-NLS-1$

		intoConfigured = new Button(group, SWT.RADIO);
		intoConfigured.setText(Messages.getString("SRPMImportPage.Configured_New_Project")); //$NON-NLS-1$

		// Set the height to 4 elements high
		GridData projectLayout = new GridData(GridData.GRAB_HORIZONTAL |
				GridData.HORIZONTAL_ALIGN_FILL);
		projectLayout.heightHint = projectList.getItemHeight() * 4;
		projectList.setLayoutData(projectLayout);

		// From the current Workspace root, get a list of all current projects
		// This should come back to us as an array of IProject.
		internalProjectList = workspaceRoot.getProjects();

		// Stuff the listbox with the text name of the projects 
		// using the getName() method
		// Find the first selected project in the workspace

		Iterator iter = selection.iterator();
		Object selectedObject= null;
		IProject selectedProject = null;
		boolean isSelection = false;
		if (iter.hasNext())
		{
			selectedObject = iter.next();
			if (selectedObject instanceof IResource)
			{
				selectedProject = ((IResource) selectedObject).getProject();
				isSelection = true;
			}
		}

		// Stuff the listbox with the text names of the projects 
		// using the getName() method and select the selected 
		// project if available
		
		for (int a = 0; a < internalProjectList.length; a++) 
		{
			projectList.add(internalProjectList[a].getName());
			if (isSelection && internalProjectList[a].equals(selectedProject)) {
				projectList.setSelection(a);
			}
		}
		
		if (projectList.getItemCount() == 0) //there were no projects
		{
			projectList.add(Messages.getString(
			"SRPMImportPage.No_projects_found")); //$NON-NLS-1$
			intoExisting.setEnabled(false); // Can't very well import into an existing
			projectList.setEnabled(false);  // project now can we?
			intoConfigured.setSelection(true);
			isSelection = true; // we don't want select the "RPMPage.No_c/c++_projects_found_2"
		}
		else {
			intoExisting.setSelection(true);	
		}
		
		if (!isSelection) { //if none is selected select first project
			projectList.setSelection(0);
		}
		else {
			projectList.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					handleEvent(null);
				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		}

		intoExisting.addListener(SWT.Selection, this);
		intoConfigured.addListener(SWT.Selection, this);
		projectList.addListener(SWT.FocusOut, this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	public void handleEvent(Event event) {
		if (event != null)
		{
			if (event.widget == intoExisting && intoExisting.getSelection())
			{
				projectList.setEnabled(true);
			}
			else if (event.widget == intoConfigured && intoConfigured.getSelection())
			{
				projectList.setEnabled(false);
			}
		}
		setPageComplete(canFinish());
	}

	
	/**
	 * canFinish()
	 * 
	 * Hot validation. Called to determine whether Finish
	 * button can be set to true
	 * @return boolean. true if finish can be activated
	 */
	public boolean canFinish() {
		// Make sure project has been selected or the user 
		// has decided to configure a new one instead
		if (getSelectedProjectName() == null && !intoConfigured.getSelection()) {
			return false;
		}

		// Make sure an srpm name has been provided
		if (sourceSRPM.getText().equals("")) { //$NON-NLS-1$
			return false;
		}
		File srpm = new File(sourceSRPM.getText());
		if (!srpm.isFile()){
			setErrorMessage(Messages.getString("SRPMImportPage.Source_not_Valid")); //$NON-NLS-1$
			return false;
		}
		if (sourceSRPM.getText().lastIndexOf(".src.rpm") == -1) //$NON-NLS-1$
		{
			setErrorMessage(Messages.getString("SRPMImportPage.No_src_rpm_ext")); //$NON-NLS-1$
			return false;
		}
  
		return true;
	}
	
	/**
	 * finish()
	 * 
	 * Perform finish after finish button is pressed
	 * @return boolean
	 * @throws CoreException
	 * 	 */
	public boolean finish() throws CoreException {
		IProject detailedProject;
			
		// Get the handle to the current activate Workspace	    
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		// User chooses an existing project or make a new project
		if (intoExisting.getSelection()) {
			// Get the current selected member from the list box (projectList)
			String[] selectedProject = projectList.getSelection();
			
			// As we only allow a single selection in the listbox, and the listbox always
			// comes with the first element selected, we can assume the first element
			// in the returned array is valid.
			detailedProject = workspaceRoot.getProject(selectedProject[0]);
		}		
		else {
			detailedProject = getNewProject();
			if(detailedProject == null) {
				return false;
			}
		}
		// Add this SRPM to srpmList
		for (int i = 0; i < srpmVector.size(); i++)
		{	// There can only be one occurance 
			if (srpmVector.elementAt(i).equals(sourceSRPM.getText()))
			{
				srpmVector.remove(i);
				break;
			}
		}
		srpmVector.add((sourceSRPM.getText()));
		
		SRPMImportOperation srpmImportOp = null;
		try {
			srpmImportOp = new SRPMImportOperation(detailedProject,
					getSelectedSRPM());
			getContainer().run(true, true, srpmImportOp);
		} catch (Exception e) {
			setErrorMessage(e.toString());
			return false;
		}

		// Get the status of the operation
		IStatus srpmImportStatus = srpmImportOp.getStatus();

		// If the status does not come back clean, open error dialog
		if (!srpmImportStatus.isOK()) {
			ErrorDialog.openError(getContainer().getShell(),
				Messages.getString("SRPMImportPage.Errors_importing_SRPM"), //$NON-NLS-1$
				null, // no special message
				srpmImportStatus);

			return false;
		}

		return true;
	}

	/**
	 * Get a new project that is configured by the new project wizard. This is
	 * currently the only way to do this.  This is a copy of 
	 * org.eclipse.team.internal.ccvs.ui.wizards.CheckoutAsWizard.getNewProject()
	 */
	private IProject getNewProject() {
		NewProjectListener listener = new NewProjectListener();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
				IResourceChangeEvent.POST_CHANGE);
		RPMNewProject wizard = new RPMNewProject();
		wizard.init(PlatformUI.getWorkbench(), null);
	    // Instantiates the wizard container with the wizard and opens it
	    WizardDialog dialog = new WizardDialog(getShell(), wizard);
	    dialog.create();
	    dialog.open();
	    ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		IProject project = listener.getNewProject();
		return project;
	}
}
