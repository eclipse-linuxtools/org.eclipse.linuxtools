/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.linuxtools.docker.core.IRegistryAccount;
import org.eclipse.linuxtools.internal.docker.core.RegistryAccountManager;
import org.eclipse.linuxtools.internal.docker.ui.wizards.RegistryAccountDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @since 2.0
 */
public class DockerRegistryAccountPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage, Listener {

	/**
	 * Inner class to keep track of password modifications (without committing
	 * them to the keyring) while the user modifies the preferences.
	 */
	protected class PasswordModification {
		protected static final int ADD = 1;
		protected static final int DELETE = 2;

		protected int changeFlag;
		protected IRegistryAccount info;

		protected PasswordModification(int changeFlag,
				IRegistryAccount info) {
			this.changeFlag = changeFlag;
			this.info = info;
		}
	}

	private final class PasswordContentProvider
			implements IStructuredContentProvider, ITableLabelProvider {

		/**
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) {
			return passwords.toArray();
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		@Override
		public void dispose() {
		}

		/**
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(Viewer,
		 *      Object, Object)
		 */
		@Override
		public void inputChanged(Viewer viewer, Object oldInput,
				Object newInput) {
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(Object,
		 *      int)
		 */
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(Object,
		 *      int)
		 */
		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof IRegistryAccount) {
				IRegistryAccount info = (IRegistryAccount) element;
				switch (columnIndex) {
				case 0:
					return info.getServerAddress();
				case 1:
					return info.getUsername();
				case 2:
					return info.getEmail();
				}
			}

			// Should never get here
			return null;
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(ILabelProviderListener)
		 */
		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(Object,
		 *      String)
		 */
		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		/**
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(ILabelProviderListener)
		 */
		@Override
		public void removeListener(ILabelProviderListener listener) {
		}

	}

	public DockerRegistryAccountPreferencePage() {
		noDefaultAndApplyButton();
		provider = new PasswordContentProvider();
		modifications = new ArrayList<>();
	}

	public DockerRegistryAccountPreferencePage(String title) {
		super(title);
	}

	public DockerRegistryAccountPreferencePage(String title,
			ImageDescriptor image) {
		super(title, image);
	}

	// SWT Widgets and content providers
	private Table pwdTable;
	private TableViewer pwdTableViewer;
	private PasswordContentProvider provider;
	private Button addButton, changeButton, removeButton;

	// List of information for table
	private List<IRegistryAccount> passwords;

	// List to keep track of additions / deletions / changes. We need to
	// keep track of these until the user decides whether to cancel the
	// preference
	// page (and we forget about the changes) or press ok (and we commit the
	// changes)
	private List<PasswordModification> modifications;

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {

		Composite page = createComposite(parent, 1, 2, false, null, -1, -1, GridData.FILL);
		GridData gd = (GridData) page.getLayoutData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;

		// SystemWidgetHelpers.createLabel(page,
		// SystemResources.RESID_PREF_SIGNON_DESCRIPTION, 2);

		// Password table
		pwdTable = new Table(page, SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL
				| SWT.H_SCROLL | SWT.BORDER);
		pwdTable.setLinesVisible(true);
		pwdTable.setHeaderVisible(true);
		pwdTable.addListener(SWT.Selection, this);

		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(100, true));
		tableLayout.addColumnData(new ColumnWeightData(100, true));
		tableLayout.addColumnData(new ColumnWeightData(100, true));
		pwdTable.setLayout(tableLayout);

		gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;

		pwdTable.setLayoutData(gd);

		// Server Address column
		TableColumn hostnameColumn = new TableColumn(pwdTable, SWT.NONE);
		hostnameColumn
				.setText("Server Address");

		// Username column
		TableColumn sysTypeColumn = new TableColumn(pwdTable, SWT.NONE);
		sysTypeColumn.setText("Username");

		// Email column
		TableColumn useridColumn = new TableColumn(pwdTable, SWT.NONE);
		useridColumn.setText("Email");

		pwdTableViewer = new TableViewer(pwdTable);
		pwdTableViewer.setContentProvider(provider);
		pwdTableViewer.setLabelProvider(provider);
		pwdTableViewer.setInput(passwords);

		// Create the Button bar for add, change and remove
		Composite buttonBar = createComposite(page, 1, 1, false, null, -1, -1, GridData.FILL);
		gd = (GridData) buttonBar.getLayoutData();
		gd.grabExcessHorizontalSpace = false;
		gd.grabExcessVerticalSpace = true;

		addButton = createPushButton(buttonBar, this,
				"Add",
				"Add a new Docker registry account.");
		changeButton = createPushButton(buttonBar, this,
				"Edit",
				"Edit an existing Docker registry account.");
		removeButton = createPushButton(buttonBar, this,
				"Remove",
				"Remove an existing Docker registry account.");

		changeButton.setEnabled(false);
		removeButton.setEnabled(false);
		return parent;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		// reinit passwords list
		passwords = RegistryAccountManager.getInstance().getAccounts();

		// refresh password table
		if (pwdTableViewer != null) {
			pwdTableViewer.refresh();
		}
	}

	/**
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(Event)
	 */
	@Override
	public void handleEvent(Event event) {
		if (event.type == SWT.Selection) {
			if (event.widget == addButton) {
				RegistryAccountDialog dialog = new RegistryAccountDialog(
						getShell(),
						"New Registry Account");
				if (dialog.open() == Window.OK) {
					IRegistryAccount info = dialog
							.getSignonInformation();
					passwords.add(info);
					modifications.add(new PasswordModification(
							PasswordModification.ADD, info));

					pwdTableViewer.refresh();
					pwdTable.select(passwords.size() - 1); // select the new
															// entry
				}

			} else if (event.widget == changeButton) {
				RegistryAccountDialog dialog = new RegistryAccountDialog(
						getShell(),
						"Edit Registry Account");
				int index = pwdTable.getSelectionIndex();
				IRegistryAccount info = passwords
						.get(index);
				dialog.setInputData(info.getServerAddress(), info.getUsername(),
						info.getEmail());
				if (dialog.open() == Window.OK) {
					// Remove old and add new
					info = dialog.getSignonInformation();
					IRegistryAccount oldInfo = passwords
							.remove(index);
					passwords.add(index, info);

					modifications.add(new PasswordModification(
							PasswordModification.DELETE, oldInfo));
					modifications.add(new PasswordModification(
							PasswordModification.ADD, info));

					pwdTableViewer.refresh();
					pwdTable.select(index);
				}

			} else if (event.widget == removeButton) {
				int[] indicies = pwdTable.getSelectionIndices();
				for (int idx = indicies.length - 1; idx >= 0; idx--) {
					RegistryAccountManager.getInstance()
							.remove(passwords.get(indicies[idx]));
					modifications.add(new PasswordModification(
							PasswordModification.DELETE,
							passwords
									.remove(indicies[idx])));
				}

				pwdTableViewer.refresh();
			}

			// Update table buttons based on changes
			switch (pwdTable.getSelectionCount()) {
			case 0:
				changeButton.setEnabled(false);
				removeButton.setEnabled(false);
				break;

			case 1:
				changeButton.setEnabled(true);
				removeButton.setEnabled(true);
				break;

			default:
				changeButton.setEnabled(false);
				removeButton.setEnabled(true);
				break;
			}
		}
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {

		if (modifications.size() > 0) {
			PasswordModification mod;
			RegistryAccountManager manager = RegistryAccountManager
					.getInstance();

			for (int i = 0; i < modifications.size(); i++) {
				mod = modifications.get(i);

				if (mod.changeFlag == PasswordModification.ADD) {
					manager.add(mod.info);
				} else if (mod.changeFlag == PasswordModification.DELETE) {
					manager.remove(mod.info);
				}
			}

			modifications.clear();
		}

		return super.performOk();
	}

	/**
	 * @see org.eclipse.jface.preference.IPreferencePage#performCancel()
	 */
	@Override
	public boolean performCancel() {
		modifications.clear();
		return super.performCancel();
	}

	private static Composite createComposite(Composite parent, int parentSpan, int numColumns, boolean border, String label, int marginSize, int spacingSize, int verticalAlignment) {
		//border = true;
		boolean borderNeeded = border;
		if (label != null)
			borderNeeded = true; // force the case
		int style = SWT.NULL;
		if (borderNeeded)
			style |= SWT.SHADOW_ETCHED_IN;
		Composite composite = null;
		if (borderNeeded) {
			composite = new Group(parent, style);
			if (label != null)
				 ((Group) composite).setText(label);
		} else {
			composite = new Composite(parent, style);
		}
		//GridLayout
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		if (marginSize != -1) {
			layout.marginWidth = 0;
			layout.marginHeight = 0;
		}
		if (spacingSize != -1) {
			layout.horizontalSpacing = 0;
			layout.verticalSpacing = 0;
		}
		composite.setLayout(layout);
		//GridData
		GridData data = new GridData();
		data.horizontalSpan = parentSpan;
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;

		data.verticalAlignment = verticalAlignment;
		data.grabExcessVerticalSpace = false;

		composite.setLayoutData(data);
		return composite;
	}

	public static Button createPushButton(Composite group, Listener listener, String label, String tooltip) {
		Button button = new Button(group, SWT.PUSH);
		button.setText(label);
		if (listener != null)
			button.addListener(SWT.Selection, listener);
		GridData data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		button.setLayoutData(data);
		if (tooltip != null)
			button.setToolTipText(tooltip);
		return button;
	}

}
