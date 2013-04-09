/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse, Anithra P J
 *******************************************************************************/

package org.eclipse.linuxtools.internal.systemtap.ui.dashboardextension.dialogs;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.internal.systemtap.ui.dashboardextension.Localization;
import org.eclipse.linuxtools.systemtap.graphingapi.core.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.graphingapi.core.filters.IDataSetFilter;
import org.eclipse.linuxtools.systemtap.graphingapi.core.structures.GraphData;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.filter.AvailableFilterTypes;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.filter.SelectFilterWizard;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.graph.GraphFactory;
import org.eclipse.linuxtools.systemtap.graphingapi.ui.wizards.graph.SelectGraphWizard;
import org.eclipse.linuxtools.systemtap.structures.TreeNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class handles creating a dialog that the user is able to select features
 * that they want to be part of their new dashboard module. Once the user is
 * done configuring the module, it will create the new module for the dashboard
 * from the active script in the editor.
 * 
 * @author Ryan Morse
 */
public class ExportScriptDialog extends TitleAreaDialog {
	public ExportScriptDialog(Shell parentShell, IDataSet data) {
		super(parentShell);
		this.data = data;
	}

	/**
	 * This method will setup the size of the dialog window and set its title.
	 * 
	 * @param shell
	 *            The shell that will contain this dialog box
	 */
	@Override
	public void create() {
		super.create();
		setTitle(Localization.getString("ExportScriptDialog.ExportScript")); //$NON-NLS-1$
	}

	/**
	 * This method adds all of the components to the dialog and positions them.
	 * Actions are added to all of the buttons to deal with user interaction.
	 * 
	 * @param parent
	 *            The Composite that will contain all components created in this
	 *            method
	 * @return The main Control created by this method.
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		comp.setLayout(layout);

		GridData layoutData = new GridData();
		// Dialog reference labels
		Label lblDisplay = new Label(comp, SWT.NONE);
		lblDisplay
				.setText(Localization.getString("ExportScriptDialog.Display")); //$NON-NLS-1$
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		txtDisplay = new Text(comp, SWT.BORDER);
		txtDisplay.setLayoutData(layoutData);

		Label lblCategory = new Label(comp, SWT.NONE);
		lblCategory.setText(Localization
				.getString("ExportScriptDialog.Category")); //$NON-NLS-1$
		txtCategory = new Text(comp, SWT.BORDER);
		txtCategory.setLayoutData(layoutData);

		Label lblDescription = new Label(comp, SWT.NONE);
		lblDescription.setText(Localization
				.getString("ExportScriptDialog.Description")); //$NON-NLS-1$
		txtDescription = new Text(comp, SWT.MULTI | SWT.WRAP | SWT.BORDER);
		txtDescription.setLayoutData(layoutData);

		Label lblGraphs = new Label(comp, SWT.NONE);
		lblGraphs.setText(Localization.getString("ExportScriptDialog.Graphs")); //$NON-NLS-1$
		layoutData = new GridData();
		layoutData.horizontalSpan = 2;
		lblGraphs.setLayoutData(layoutData);

		Composite treeComposite = new Composite(comp, SWT.NONE);
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 2;
		treeComposite.setLayoutData(layoutData);
		GridLayout treeLayout = new GridLayout();
		treeLayout.numColumns = 2;
		treeLayout.makeColumnsEqualWidth = false;
		treeComposite.setLayout(treeLayout);

		treeGraphs = new Tree(treeComposite, SWT.SINGLE | SWT.BORDER);
		layoutData = new GridData();
		layoutData.verticalSpan = 3;
		layoutData.grabExcessHorizontalSpace = true;
		layoutData.horizontalAlignment = SWT.FILL;
		treeGraphs.setLayoutData(layoutData);

		// Button to add another graph
		Composite buttonComposite = new Composite(treeComposite, SWT.None);
		RowLayout buttonLayout = new RowLayout(SWT.VERTICAL);
		buttonLayout.pack = false;
		buttonLayout.marginHeight = 5;
		buttonComposite.setLayout(buttonLayout);
		btnAdd = new Button(buttonComposite, SWT.PUSH);
		btnAdd.setText(Localization.getString("ExportScriptDialog.Add")); //$NON-NLS-1$

		// Button to filter the script output data
		btnAddFilter = new Button(buttonComposite, SWT.PUSH);
		btnAddFilter.setText(Localization
				.getString("ExportScriptDialog.AddFilter")); //$NON-NLS-1$
		btnAddFilter.setEnabled(false);

		// Button to remove the selected graph/filter
		btnRemove = new Button(buttonComposite, SWT.PUSH);
		btnRemove.setText(Localization.getString("ExportScriptDialog.Remove")); //$NON-NLS-1$
		btnRemove.setEnabled(false);

		// Action to notify the buttons when to enable/disable themselves based
		// on list selection
		treeGraphs.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedTreeItem = (TreeItem) e.item;
				if (null == selectedTreeItem.getParentItem())
					btnAddFilter.setEnabled(true);
				else
					btnAddFilter.setEnabled(false);
				btnRemove.setEnabled(true);
			}
		});

		// Brings up a new dialog box when user clicks the add button. Allows
		// selecting a new graph to display.
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SelectGraphWizard wizard = new SelectGraphWizard(data);
				IWorkbench workbench = PlatformUI.getWorkbench();
				wizard.init(workbench, null);
				WizardDialog dialog = new WizardDialog(workbench
						.getActiveWorkbenchWindow().getShell(), wizard);
				dialog.create();
				dialog.open();

				GraphData gd = wizard.getGraphData();
				if (null != gd) {
					TreeItem item = new TreeItem(treeGraphs, SWT.NONE);
					item.setText(GraphFactory.getGraphName(gd.graphID) + ":" //$NON-NLS-1$
							+ gd.title);
					item.setData(gd);
				}
			}
		});

		// Brings up a new dialog for selecting filter options when the user
		// clicks the filter button.
		btnAddFilter.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				SelectFilterWizard wizard = new SelectFilterWizard(data
						.getTitles());
				IWorkbench workbench = PlatformUI.getWorkbench();
				wizard.init(workbench, null);
				WizardDialog dialog = new WizardDialog(workbench
						.getActiveWorkbenchWindow().getShell(), wizard);
				dialog.create();
				dialog.open();

				IDataSetFilter f = wizard.getFilter();
				if (null != f) {
					TreeItem item = new TreeItem(treeGraphs.getSelection()[0],
							SWT.NONE);
					item.setText(AvailableFilterTypes.getFilterName(f.getID()));
					item.setData(f);
				}
			}
		});

		// Removes the selected graph/filter from the tree
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectedTreeItem.dispose();
				btnRemove.setEnabled(false);
			}
		});

		return comp;
	}

	/**
	 * This method haddles what to do when the user clicks the ok or cancel
	 * button. If canceled it will just close without doing anything. If the
	 * user clicked ok and the data entered is valid it will set up variables
	 * that can be accessed later to build the actual module. This method should
	 * not be called explicitly.
	 * 
	 * @param buttonID
	 *            A reference to the button that was pressed. 0 - ID, 1- for
	 *            others
	 */
	@Override
	protected void okPressed() {
		if (txtDisplay.getText().length() <= 0
				|| txtCategory.getText().length() <= 0
				|| txtDescription.getText().length() <= 0
				|| treeGraphs.getItemCount() <= 0) {
			String msg = MessageFormat.format(
					Localization.getString("ExportScriptDialog.FillFields"), //$NON-NLS-1$
					(Object[]) null);
			MessageDialog.openWarning(this.getShell(),
					Localization.getString("ExportScriptDialog.Error"), msg); //$NON-NLS-1$
			return;
		}

		display = txtDisplay.getText();
		category = txtCategory.getText();
		description = txtDescription.getText();
		buildGraphData();
		buildFilterData();

		super.okPressed();
	}

	/**
	 * This allows an outside class to determin what was chosen to be the
	 * Category.
	 * 
	 * @return String representing the selected Category name.
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * This allows an outside class to determin what was chosen to be the
	 * Description.
	 * 
	 * @return String representing the selected Description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * This allows an outside class to determin what was chosen to be the
	 * Display name.
	 * 
	 * @return String representing the selected Display name.
	 */
	public String getDisplay() {
		return display;
	}

	/**
	 * This allows an outside class to determin what graph types were selected.
	 * 
	 * @return GraphData[] for each selected graph.
	 */
	public GraphData[] getGraphs() {
		return graphData;
	}

	/**
	 * This allows an outside class to determin what filter types were chosen.
	 * 
	 * @return TreeNode organized as follows: Root->Graphs->Filters.
	 */
	public TreeNode getGraphFilters() {
		return filters;
	}

	/**
	 * This cleans up all internal references to objects. No other method should
	 * be called after the dispose method.
	 */
	public void dispose() {
		if (null != txtDisplay)
			txtDisplay.dispose();
		if (null != txtCategory)
			txtCategory.dispose();
		if (null != txtDescription)
			txtDescription.dispose();
		if (null != treeGraphs)
			treeGraphs.dispose();
		if (null != btnAdd)
			btnAdd.dispose();
		if (null != btnRemove)
			btnRemove.dispose();
		if (null != btnAddFilter)
			btnAddFilter.dispose();
		if (null != selectedTreeItem)
			selectedTreeItem.dispose();
		txtDisplay = null;
		txtCategory = null;
		txtDescription = null;
		treeGraphs = null;
		btnAdd = null;
		btnRemove = null;
		btnAddFilter = null;
		selectedTreeItem = null;
		data = null;
	}

	/**
	 * This method converts what was selected in the tree into a simple array of
	 * all of the selected graphs and their data.
	 */
	private void buildGraphData() {
		TreeItem[] children = treeGraphs.getItems();
		graphData = new GraphData[children.length];
		for (int i = 0; i < graphData.length; i++)
			graphData[i] = (GraphData) children[i].getData();
	}

	/**
	 * This mothod takes the data from the tree and builds another tree that has
	 * all the information about the graph and Filters in an easily accessable
	 * structure.
	 */
	private void buildFilterData() {
		TreeItem[] items = treeGraphs.getItems();
		TreeItem[] filterItems;

		filters = new TreeNode("", false); //$NON-NLS-1$
		TreeNode graphLevel;
		for (int i = 0; i < items.length; i++) {
			filterItems = items[i].getItems();

			graphLevel = new TreeNode("graph", false); //$NON-NLS-1$
			filters.add(graphLevel);

			for (int j = 0; j < filterItems.length; j++)
				graphLevel.add(new TreeNode(filterItems[j].getData(), false));
		}
	}

	private IDataSet data;
	private Tree treeGraphs;
	private Text txtDisplay, txtCategory, txtDescription;
	private Button btnAdd, btnRemove, btnAddFilter;
	private String display, category, description;
	private GraphData[] graphData;
	private TreeNode filters;
	private TreeItem selectedTreeItem;
}
