/*******************************************************************************
 * Copyright (c) 2011, 2013 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Kurtakov - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.linuxtools.rpm.core.RPMProjectLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.dialogs.WorkingSetConfigurationBlock;

/**
 * Standard page for project creation adding some rpm specific controls.
 *
 */
public class NewProjectCreationPage extends WizardNewProjectCreationPage {

	private ComboViewer typeCombo;
	private final WorkingSetGroup wsGroup;

	private static final IWorkingSet[] EMPTY_WORKING_SET_ARRAY = new IWorkingSet[0];

	/**
	 * Instantiate the page.
	 * @param pageName The name of the page.
	 */
	public NewProjectCreationPage(String pageName) {
		super(pageName);
		wsGroup = new WorkingSetGroup();
		setWorkingSets(EMPTY_WORKING_SET_ARRAY);
	}

	/**
	 * The wizard owning this page can call this method to initialise fields using the
	 * current selection.
	 *
	 * @param selection the current object selection
	 */
	public void init(IStructuredSelection selection) {
		setWorkingSets(getSelectedWorkingSet(selection));
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite control = (Composite) getControl();
		Composite projectTypeGroup = new Composite(control, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		projectTypeGroup.setLayout(layout);
		projectTypeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label typeLabel = new Label(projectTypeGroup, SWT.NONE);
		typeLabel.setText(Messages.getString("SRPMImportPage.4")); //$NON-NLS-1$
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		typeCombo = new ComboViewer(projectTypeGroup, SWT.READ_ONLY);
		typeCombo.getControl().setLayoutData(data);
		typeCombo.setContentProvider(ArrayContentProvider.getInstance());
		typeCombo.setInput(RPMProjectLayout.values());
		typeCombo.getCombo().select(0);
		// Working set controls
		Control workingSetControl = wsGroup.createControl(control);
		workingSetControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}

	/**
	 * Returns the selected layout if any or the default one - RPMBUILD.
	 *
	 * @return The selected project layout.
	 */
	public RPMProjectLayout getSelectedLayout() {
		return RPMProjectLayout.valueOf(typeCombo.getCombo().getItem(
				typeCombo.getCombo().getSelectionIndex()));

	}

	/**
	 * Returns the working sets to which the new project should be added.
	 *
	 * @return the selected working sets to which the new project should be added
	 */
	public IWorkingSet[] getWorkingSets() {
		return wsGroup.getSelectedWorkingSets();
	}

	/**
	 * Sets the working sets to which the new project should be added.
	 *
	 * @param workingSets the initial selected working sets
	 */
	public void setWorkingSets(IWorkingSet[] workingSets) {
		if (workingSets == null) {
			wsGroup.setWorkingSets(EMPTY_WORKING_SET_ARRAY);
		}
		wsGroup.setWorkingSets(workingSets);
	}

	/**
	 * Try our best to set the working sets field to something sensible based on the
	 * current selection.
	 */
	private IWorkingSet[] getSelectedWorkingSet(IStructuredSelection selection) {
		if (!(selection instanceof ITreeSelection)) {
			return EMPTY_WORKING_SET_ARRAY;
		}

		ITreeSelection treeSelection = (ITreeSelection) selection;
		if (treeSelection.isEmpty()) {
			return EMPTY_WORKING_SET_ARRAY;
		}

		List<?> elements= treeSelection.toList();
		if (elements.size() == 1) {
			Object element = elements.get(0);
			TreePath[] paths = treeSelection.getPathsFor(element);
			if (paths.length != 1 || paths[0].getSegmentCount() == 0) {
				return EMPTY_WORKING_SET_ARRAY;
			}

			Object candidate = paths[0].getSegment(0);
			if (!(candidate instanceof IWorkingSet)) {
				return EMPTY_WORKING_SET_ARRAY;
			}

			IWorkingSet workingSetCandidate = (IWorkingSet) candidate;
			if (!workingSetCandidate.isAggregateWorkingSet()) {
				return new IWorkingSet[] { workingSetCandidate };
			}

			return EMPTY_WORKING_SET_ARRAY;
		}

		ArrayList<IWorkingSet> result = new ArrayList<>();
		for (Object element : elements) {
			if (element instanceof IWorkingSet && !((IWorkingSet) element).isAggregateWorkingSet()) {
				result.add((IWorkingSet)element);
			}
		}

		if (!result.isEmpty()) {
			return result.toArray(new IWorkingSet[result.size()]);
		} else {
			return EMPTY_WORKING_SET_ARRAY;
		}
	}

	/**
	 * Little class to encapsulate the working set group of controls.
	 */
	private static final class WorkingSetGroup {

		private WorkingSetConfigurationBlock workingSetBlock;

		public WorkingSetGroup() {
			String[] workingSetIds = new String[] { "org.eclipse.ui.resourceWorkingSetPage" }; //$NON-NLS-1$
			workingSetBlock = new WorkingSetConfigurationBlock(workingSetIds,
					Activator.getDefault().getDialogSettings());
		}

		public Control createControl(Composite composite) {
			Group workingSetGroup = new Group(composite, SWT.NONE);
			workingSetGroup.setFont(composite.getFont());
			workingSetGroup.setText(Messages.getString("NewProjectCreationPage.0")); //$NON-NLS-1$
			workingSetGroup.setLayout(new GridLayout(1, false));

			workingSetBlock.createContent(workingSetGroup);

			return workingSetGroup;
		}

		public void setWorkingSets(IWorkingSet[] workingSets) {
			workingSetBlock.setWorkingSets(workingSets);
		}

		public IWorkingSet[] getSelectedWorkingSets() {
			return workingSetBlock.getSelectedWorkingSets();
		}
	}
}
