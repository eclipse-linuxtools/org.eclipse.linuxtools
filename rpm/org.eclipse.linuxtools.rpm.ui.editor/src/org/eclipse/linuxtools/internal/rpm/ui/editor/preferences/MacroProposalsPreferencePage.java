/*******************************************************************************
 * Copyright (c) 2007 Alphonse Van Assche.
 * Copyright (c) 2008, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alphonse Van Assche - initial API and implementation
 *    Wind River Systems, Inc - compile fixes
 *******************************************************************************/

package org.eclipse.linuxtools.internal.rpm.ui.editor.preferences;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.linuxtools.internal.rpm.ui.editor.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * RPM macro proposals and hover preference page class.
 * 
 */
public class MacroProposalsPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public MacroProposalsPreferencePage() {
		super(FLAT);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		ListEditor macroListEditor = new MacroListEditor(
				PreferenceConstants.P_MACRO_PROPOSALS_FILESPATH,
				Messages.MacroProposalsPreferencePage_0, getFieldEditorParent());
		addField(macroListEditor);
		RadioGroupFieldEditor macroHoverListEditor = new RadioGroupFieldEditor(
				PreferenceConstants.P_MACRO_HOVER_CONTENT,
				Messages.MacroProposalsPreferencePage_1,
				1,
				new String[][] {
						{
								Messages.MacroProposalsPreferencePage_2,
								PreferenceConstants.P_MACRO_HOVER_CONTENT_VIEWDESCRIPTION },
						{
								Messages.MacroProposalsPreferencePage_3,
								PreferenceConstants.P_MACRO_HOVER_CONTENT_VIEWCONTENT } },
				getFieldEditorParent(), true);
		addField(macroHoverListEditor);
	}

	static class MacroListEditor extends ListEditor {

		/**
		 * The list widget; <code>null</code> if none (before creation or
		 * after disposal).
		 */
		private List list;

		/**
		 * The button box containing the Add, Remove, Up, and Down buttons;
		 * <code>null</code> if none (before creation or after disposal).
		 */
		private Composite buttonBox;

		/**
		 * The Add file button.
		 */
		private Button addFileButton;
		
		/**
		 * The Add button.
		 */
		private Button addDirButton;

		/**
		 * The Remove button.
		 */
		private Button removeButton;

		/**
		 * The Up button.
		 */
		private Button upButton;

		/**
		 * The Down button.
		 */
		private Button downButton;

		/**
		 * The selection listener.
		 */
		private SelectionListener selectionListener;

		public MacroListEditor(String name, String labelText, Composite parent) {
			super();
			init(name, labelText);
			createControl(parent);
			list = getListControl(parent);
		}

		@Override
		protected String createList(String[] items) {
			StringBuilder path = new StringBuilder(""); //$NON-NLS-1$
			for (String item:items) {
				path.append(item);
				path.append(";"); //$NON-NLS-1$
			}
			return path.toString();
		}

		@Override
		protected String getNewInputObject() {
			FileDialog dialog = new FileDialog(getShell());
			return dialog.open();
		}
		
		protected String getNewDirInputObject() {
			DirectoryDialog dialog = new DirectoryDialog(getShell());
			return dialog.open();
		}

		@Override
		protected String[] parseString(String stringList) {
			StringTokenizer st = new StringTokenizer(stringList, ";\n\r"); //$NON-NLS-1$
			ArrayList<String> v = new ArrayList<String>();
			while (st.hasMoreTokens()) {
				v.add(st.nextToken());
			}
			return v.toArray(new String[v.size()]);
		}

		/**
		 * Notifies that the Add button has been pressed.
		 */
		private void addFilePressed() {
			setPresentsDefaultValue(false);
			String input = getNewInputObject();

			if (input != null) {
				int index = list.getSelectionIndex();
				if (index >= 0) {
					list.add(input, index + 1);
				} else {
					list.add(input, 0);
				}
				selectionChanged();
			}
		}
		
		/**
		 * Notifies that the Add button has been pressed.
		 */
		private void addDirPressed() {
			setPresentsDefaultValue(false);
			String input = getNewDirInputObject();

			if (input != null) {
				int index = list.getSelectionIndex();
				if (index >= 0) {
					list.add(input, index + 1);
				} else {
					list.add(input, 0);
				}
				selectionChanged();
			}
		}

		/**
		 * Creates the Add, Remove, Up, and Down button in the given button box.
		 * 
		 * @param box
		 *            the box for the buttons
		 */
		private void createButtons(Composite box) {
			addFileButton = createPushButton(box, Messages.MacroProposalsPreferencePage_4);
			addDirButton = createPushButton(box, Messages.MacroProposalsPreferencePage_5);
			removeButton = createPushButton(box, "ListEditor.remove");//$NON-NLS-1$
			upButton = createPushButton(box, "ListEditor.up");//$NON-NLS-1$
			downButton = createPushButton(box, "ListEditor.down");//$NON-NLS-1$
		}

		/**
		 * Helper method to create a push button.
		 * 
		 * @param parent
		 *            the parent control
		 * @param key
		 *            the resource name used to supply the button's label text
		 * @return Button
		 */
		private Button createPushButton(Composite parent, String key) {
			Button button = new Button(parent, SWT.PUSH);
			button.setText(JFaceResources.getString(key));
			button.setFont(parent.getFont());
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			int widthHint = convertHorizontalDLUsToPixels(button,
					IDialogConstants.BUTTON_WIDTH);
			data.widthHint = Math.max(widthHint, button.computeSize(
					SWT.DEFAULT, SWT.DEFAULT, true).x);
			button.setLayoutData(data);
			button.addSelectionListener(getSelectionListener());
			return button;
		}

		/**
		 * Creates a selection listener.
		 */
		@Override
		public void createSelectionListener() {
			selectionListener = new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					Widget widget = event.widget;
					if (widget == addFileButton) {
						addFilePressed();
					} else if (widget == addDirButton) {
						addDirPressed();
					}else if (widget == removeButton) {
						removePressed();
					} else if (widget == upButton) {
						upPressed();
					} else if (widget == downButton) {
						downPressed();
					} else if (widget == list) {
						selectionChanged();
					}
				}
			};
		}

		/**
		 * Notifies that the Down button has been pressed.
		 */
		private void downPressed() {
			swap(false);
		}

		/**
		 * Returns this field editor's button box containing the Add, Remove,
		 * Up, and Down button.
		 * 
		 * @param parent The parent control
		 * @return the button box
		 */
		@Override
		public Composite getButtonBoxControl(Composite parent) {
			if (buttonBox == null) {
				buttonBox = new Composite(parent, SWT.NULL);
				GridLayout layout = new GridLayout();
				layout.marginWidth = 0;
				buttonBox.setLayout(layout);
				createButtons(buttonBox);
				buttonBox.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent event) {
						addFileButton = null;
						addDirButton = null;
						removeButton = null;
						upButton = null;
						downButton = null;
						buttonBox = null;
					}
				});

			} else {
				checkParent(buttonBox, parent);
			}

			selectionChanged();
			return buttonBox;
		}

		/**
		 * Returns this field editor's list control.
		 * 
		 * @param parent The parent control
		 * @return the list control
		 */
		@Override
		public final List getListControl(Composite parent) {
			if (list == null) {
				list = new List(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL
						| SWT.H_SCROLL);
				list.setFont(parent.getFont());
				list.addSelectionListener(getSelectionListener());
				list.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent event) {
						list = null;
					}
				});
			} else {
				checkParent(list, parent);
			}
			return list;
		}

		/**
		 * Returns this field editor's selection listener. The listener is
		 * created if nessessary.
		 * 
		 * @return the selection listener
		 */
		private SelectionListener getSelectionListener() {
			if (selectionListener == null) {
				createSelectionListener();
			}
			return selectionListener;
		}

		/**
		 * Returns this field editor's shell.
		 * <p>
		 * This method is internal to the framework; subclassers should not call
		 * this method.
		 * </p>
		 * 
		 * @return the shell
		 */
		@Override
		protected Shell getShell() {
			if (addFileButton == null) {
				return null;
			}
			return addFileButton.getShell();
		}

		/**
		 * Notifies that the Remove button has been pressed.
		 */
		private void removePressed() {
			setPresentsDefaultValue(false);
			int index = list.getSelectionIndex();
			if (index >= 0) {
				list.remove(index);
				selectionChanged();
			}
		}

		/**
		 * Notifies that the list selection has changed.
		 */
		@Override
		protected void selectionChanged() {

			int index = list.getSelectionIndex();
			int size = list.getItemCount();

			removeButton.setEnabled(index >= 0);
			upButton.setEnabled(size > 1 && index > 0);
			downButton.setEnabled(size > 1 && index >= 0 && index < size - 1);
		}

		/**
		 * Moves the currently selected item up or down.
		 * 
		 * @param up
		 *            <code>true</code> if the item should move up, and
		 *            <code>false</code> if it should move down
		 */
		private void swap(boolean up) {
			setPresentsDefaultValue(false);
			int index = list.getSelectionIndex();
			int target = up ? index - 1 : index + 1;

			if (index >= 0) {
				String[] selection = list.getSelection();
				Assert.isTrue(selection.length == 1);
				list.remove(index);
				list.add(selection[0], target);
				list.setSelection(target);
			}
			selectionChanged();
		}

		/**
		 * Notifies that the Up button has been pressed.
		 */
		private void upPressed() {
			swap(true);
		}

		/*
		 * @see FieldEditor.setEnabled(boolean,Composite).
		 */
		@Override
		public void setEnabled(boolean enabled, Composite parent) {
			super.setEnabled(enabled, parent);
			getListControl(parent).setEnabled(enabled);
			addFileButton.setEnabled(enabled);
			addDirButton.setEnabled(enabled);
			removeButton.setEnabled(enabled);
			upButton.setEnabled(enabled);
			downButton.setEnabled(enabled);
		}

	}

}
