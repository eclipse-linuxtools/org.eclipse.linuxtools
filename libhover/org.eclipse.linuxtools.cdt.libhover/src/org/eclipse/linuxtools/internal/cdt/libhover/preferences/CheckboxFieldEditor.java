/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover.preferences;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHoverMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

public class CheckboxFieldEditor extends FieldEditor {

	private String extensionID;
	private String preferenceName;
	private HashMap<String, String> mapItems = new HashMap<String, String>();
	private Table table;
	private CheckboxTableViewer tv;
	private Button[] buttons;
	private Composite usercomp; // space where user can create widgets 
	private Composite buttoncomp; // space for buttons on the right

	
	public static final String EMPTY_STR = ""; //$NON-NLS-1$
	public static final String MOVEUP_STR = LibHoverMessages.getString("FileListControl.moveup"); //$NON-NLS-1$
	public static final String MOVEDOWN_STR = LibHoverMessages.getString("FileListControl.movedown"); //$NON-NLS-1$

	public CheckboxFieldEditor(String extensionID, String preferenceName, String label, Composite parent) {
		this.extensionID = extensionID;
		this.preferenceName = preferenceName;
		init(preferenceName, label);
		createControl(parent);
	}
	
	
	@Override
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	protected void adjustForNumColumns(int numColumns) {
		if (numColumns > 1) {
			Control control = getLabelControl();
			if (control != null) {
				((GridData)control.getLayoutData()).horizontalSpan = numColumns;
			}
			((GridData)table.getLayoutData()).horizontalSpan = numColumns - 1;
			((GridData)buttoncomp.getLayoutData()).horizontalSpan = 1;
		} else {
			Control control = getLabelControl();
			if (control != null) {
				((GridData)control.getLayoutData()).horizontalSpan = 1;
			}
			((GridData)usercomp.getLayoutData()).horizontalSpan = 1;
			((GridData)buttoncomp.getLayoutData()).horizontalSpan = 1;
		}
	}


	@Override
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		int checkC = 1;
		if (numColumns > 1) {
			checkC = numColumns - 1;
		}
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);
		control = getCheckboxControl(parent);
		gd = new GridData();
		gd.horizontalSpan = checkC;
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		control.setLayoutData(gd);
		control.setFont(parent.getFont());
		control = getButtonControl(parent);
		gd = new GridData();
		gd.horizontalSpan = 1;
		gd.horizontalAlignment = GridData.FILL;
		control.setLayoutData(gd);
		control.setFont(parent.getFont());
	}

	private void getExtensions() {
		mapItems.clear();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				extensionID);
		if (point != null) {
			IExtension[] exts = point.getExtensions();
			for (IExtension ext : exts) {
				if (ext.getConfigurationElements().length > 0) {
					mapItems.put(ext.getUniqueIdentifier(), ext.getLabel());
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void doLoad() {
		// Combine user preference string with existing libhover extensions.
		getExtensions();
		HashMap<String,String> copyMap = (HashMap<String,String>)mapItems.clone();
		ArrayList<TableData> data = new ArrayList<TableData>();
		ArrayList<TableData> checkedData = new ArrayList<TableData>();
		// See what the user has set in preferences and make sure that
		// any libhover referred to is a valid current extension.
		// Drop any preference for a non-existent extension.
		String pref = getPreferenceStore().getString(preferenceName);
		String[] tokens = pref.split(":");
		if (tokens.length > 1) {
			for (int i = 0; i < tokens.length; i+=2) {
				String id = tokens[i];
				Boolean checked = Boolean.valueOf(tokens[i+1]);
				String value = copyMap.get(id);
				if (value != null) {
					TableData d = new TableData(id, value);
					data.add(d);
					if (checked)
						checkedData.add(d);
					copyMap.remove(id);
				}
			}
		}
		// Add remaining libhover extensions to end of list as checked by default
		String[] leftovers = new String[copyMap.size()];
		leftovers = copyMap.keySet().toArray(leftovers);
		for (int i = 0; i < leftovers.length; ++i) {
			TableData d = new TableData(leftovers[i], copyMap.get(leftovers[i]));
			data.add(d);
			checkedData.add(d);
		}
		tv.setInput(data.toArray());
		tv.setCheckedElements(checkedData.toArray());
		updateButtons();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doLoadDefault() {
	    // Default is to find all current extensions and turn them on by default.
		getExtensions();
		HashMap<String,String> copyMap = (HashMap<String,String>)mapItems.clone();
		ArrayList<TableData> data = new ArrayList<TableData>();
		ArrayList<TableData> checkedData = new ArrayList<TableData>();
		String[] ids = new String[copyMap.size()];
		ids = copyMap.keySet().toArray(ids);
		for (int i = 0; i < ids.length; ++i) {
			TableData d = new TableData(ids[i], copyMap.get(ids[i]));
			data.add(d);
			checkedData.add(d);
		}
		tv.setInput(data.toArray());
		tv.setCheckedElements(checkedData.toArray());
		updateButtons();
	}

	@Override
	protected void doStore() {
		save();
	}

	@Override
	public int getNumberOfControls() {
		return 3;
	}

	public Control getButtonControl(Composite parent) {
		if (buttoncomp == null) {
			buttoncomp = new Composite(parent, SWT.NONE);
			GridData d = new GridData(GridData.END);
			d.widthHint = 1;
			buttoncomp.setLayoutData(d);
			buttoncomp.setFont(parent.getFont());
			initButtons(buttoncomp, new String[] {
					MOVEUP_STR, MOVEDOWN_STR, null, 
					UIMessages.getString("ErrorParsTab.0"), //$NON-NLS-1$
					UIMessages.getString("ErrorParsTab.1")  //$NON-NLS-1$
			});
		}
		return buttoncomp;
	}
	
	public Control getCheckboxControl(Composite parent) {
		if (table == null) {
//			parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			table = new Table(parent, SWT.BORDER | SWT.CHECK | SWT.SINGLE);
			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			table.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateButtons();
				}});
			tv = new CheckboxTableViewer(table);
			tv.setContentProvider(new IStructuredContentProvider() {
				public Object[] getElements(Object inputElement) {
					return (Object[])inputElement;
				}
				public void dispose() {}
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
			});

			tv.addCheckStateListener(new ICheckStateListener() {
				public void checkStateChanged(CheckStateChangedEvent e) {
				}
			});

		}
		return table;
	}
	/**
	 * Ability to create standard button on any composite.
	 * @param c
	 * @param names
	 */
	protected void initButtons(Composite c, String[] names) {
		initButtons(c, names, 80);
	}
	protected void initButtons(Composite c, String[] names, int width) {
		if (names == null || names.length == 0) return;
		c.setLayoutData(new GridData(GridData.FILL_VERTICAL));
	    c.setLayout(new GridLayout(1, false));
		buttons = new Button[names.length];
		for (int i=0; i<names.length; i++) {
			buttons[i] = new Button(c, SWT.PUSH);
			buttons[i].setFont(c.getFont());
			GridData gdb = new GridData(GridData.VERTICAL_ALIGN_CENTER);
			gdb.grabExcessHorizontalSpace = false;
			gdb.horizontalAlignment = SWT.FILL;
			gdb.minimumWidth = width;
			
			if (names[i] != null)
				buttons[i].setText(names[i]);
			else { // no button, but placeholder ! 
				buttons[i].setVisible(false);
				buttons[i].setEnabled(false);
				gdb.heightHint = 10;
			}
			
			buttons[i].setLayoutData(gdb);
			buttons[i].addSelectionListener(new SelectionAdapter() {
		        @Override
				public void widgetSelected(SelectionEvent event) {
		        	buttonPressed(event);
		        }});
		}
	}
	
	/**
	 * 
	 * @param e - event to be handled
	 */
	private void buttonPressed(SelectionEvent e) {
		for (int i=0; i<buttons.length; i++) {
			if (buttons[i].equals(e.widget)) {
				buttonPressed(i);
				return;
			}
		}
	}

	public void buttonPressed (int n) {
		switch (n) {
		case 0: // up
			moveItem(true);
			break;
		case 1: // down
			moveItem(false);
			break;
		case 2: // do nothing - it's not a button
			break;
			
		case 3: // check all
			tv.setAllChecked(true);
			break;
		case 4: // uncheck all	
			tv.setAllChecked(false);
			break;
		default:
			break;
		}
	}

	// Move item up / down
	private void moveItem(boolean up) {
		int n = table.getSelectionIndex();
		if (n < 0 || 
				(up && n == 0) || 
				(!up && n+1 == table.getItemCount()))
			return;
		TableData d = (TableData)tv.getElementAt(n);
		boolean checked = tv.getChecked(d);
		tv.remove(d);
		n = up ? n - 1 : n + 1;
		tv.insert(d, n);
		tv.setChecked(d, checked);
		table.setSelection(n);
	}
	/**
	 * Changes state of existing button.
	 * Does nothing if index is invalid
	 * 
	 * @param i - button index
	 * @param state - required state
	 */
	protected void buttonSetEnabled(int i, boolean state) {
		if (buttons == null || buttons.length <= i ) return;
		buttons[i].setEnabled(state);
	}
	
	public void updateButtons() {
		int cnt = table.getItemCount();
		int pos = table.getSelectionIndex();
		buttonSetEnabled(0, pos > 0);
		buttonSetEnabled(1, pos != -1 && pos < (cnt - 1));
		buttonSetEnabled(3, cnt > 0);
		buttonSetEnabled(4, cnt > 0);
	}

	class TableData {
		String key;
		String value;
		public TableData (String _key, String _value) {
			key   = _key;
			value = _value;
		}
		@Override
		public String toString() { return value; } 
	}
	
	private void save() {
		boolean inRange = true;
		int i = 0;
		String outString = "";
		String del = "";
		while (inRange) {
			TableData t;
			Object obj = tv.getElementAt(i);
			if (obj == null)
				inRange = false;
			else {
				++i;
				t = (TableData)obj;
				outString += del + t.key + ":" + tv.getChecked(obj);
				del = ":";
			}
		}
		if (outString.length() > 1)
			getPreferenceStore().setValue(preferenceName, outString);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#setEnabled(boolean,
	 *      org.eclipse.swt.widgets.Composite)
	 */
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		getCheckboxControl(parent).setEnabled(enabled);
		getButtonControl(parent).setEnabled(enabled);
	}
}
