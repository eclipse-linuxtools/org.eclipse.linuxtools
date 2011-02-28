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
package org.eclipse.linuxtools.internal.cdt.libhover.properties;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.cdt.ui.newui.UIMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.linuxtools.cdt.libhover.LibhoverPlugin;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHover;
import org.eclipse.linuxtools.internal.cdt.libhover.LibHoverMessages;
import org.eclipse.linuxtools.internal.cdt.libhover.preferences.PreferenceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.dialogs.PropertyPage;

public class LibHoverPropertyPage extends PropertyPage {

	private static final String PROJECT_SPECIFIC_MSG = "Libhover.projectSpecific.msg"; //$NON-NLS-1$
	private static final String LIBHOVER_PROPERTY = "LIBHOVER"; //$NON-NLS-1$
	
	private QualifiedName libhoverProperty = new QualifiedName(LibhoverPlugin.getID(), LIBHOVER_PROPERTY);

	private Button projectSpecific;

	private HashMap<String, String> mapItems = new HashMap<String, String>();
	
	private Table table;
	private CheckboxTableViewer tv;
	private Button[] buttons; 
	private Composite buttoncomp; // space for buttons on the right
	
	public static final String EMPTY_STR = ""; //$NON-NLS-1$
	public static final String MOVEUP_STR = LibHoverMessages.getString("FileListControl.moveup"); //$NON-NLS-1$
	public static final String MOVEDOWN_STR = LibHoverMessages.getString("FileListControl.movedown"); //$NON-NLS-1$

	/**
	 * Constructor for LibhoverPropertyPage.
	 */
	public LibHoverPropertyPage() {
		super();
		setPreferenceStore(LibhoverPlugin.getDefault().getPreferenceStore());
	}

	private IProject getProject() {
		IResource r = (IResource)getElement().getAdapter(IResource.class);
		return r.getProject();
	}
	
	private String getPropertyString() {
		String p;
		try {
			p = getProject().getPersistentProperty(libhoverProperty);
		} catch (CoreException c) {
			p = null;
		}
		return p;
	}
	
	private void addFirstSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		projectSpecific = new Button(composite, SWT.CHECK);
		projectSpecific.setText(LibHoverMessages.getString(PROJECT_SPECIFIC_MSG));
		String p = getPropertyString();
		projectSpecific.setSelection(p != null);
		projectSpecific.addSelectionListener(new SelectionAdapter() {
	        @Override
			public void widgetSelected(SelectionEvent event) {
	        	projectSpecificPressed(event);
	        }});
	}

	private void projectSpecificPressed(SelectionEvent event) {
		if (projectSpecific.getSelection()) {
			buttoncomp.setEnabled(true);
			table.setEnabled(true);
			updateCheckboxData(true);
		} else {
			buttoncomp.setEnabled(false);
			table.setEnabled(false);
			updateCheckboxData(false);
		}
	}
	
	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}
	
	private void addLabel(Composite parent) {
		Label label = new Label(parent, SWT.HORIZONTAL);
		label.setText(LibHoverMessages.getString("LibhoverPreferences.title")); //$NON-NLS-1$
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		label.setLayoutData(gridData);
	}
	
	private Control getCheckboxControl(Composite parent) {
		if (table == null) {
//			parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			table = new Table(parent, SWT.BORDER | SWT.CHECK | SWT.SINGLE);
			GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			gridData.horizontalSpan = 2;
			table.setLayoutData(gridData);
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
	
	private Control getButtonControl(Composite parent) {
		if (buttoncomp == null) {
			buttoncomp = new Composite(parent, SWT.NONE);
			GridData d = new GridData(GridData.END);
			d.widthHint = 1;
			d.horizontalSpan = 1;
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

	private void addSecondSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(data);

		getCheckboxControl(composite);
		getButtonControl(composite);
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(data);

		addFirstSection(composite);
		addSeparator(composite);
		addLabel(composite);
		addSecondSection(composite);
		String p = getPropertyString();
		updateCheckboxData(p != null);
		buttoncomp.setEnabled(p != null);
		table.setEnabled(p != null);

		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		updateCheckboxData(projectSpecific.getSelection());
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk() {
		try {
			save();
		} catch (CoreException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Ability to create standard button on any composite.
	 * @param c
	 * @param names
	 */
	private void initButtons(Composite c, String[] names) {
		initButtons(c, names, 80);
	}
	
	private void initButtons(Composite c, String[] names, int width) {
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

	private void buttonPressed (int n) {
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
	private void buttonSetEnabled(int i, boolean state) {
		if (buttons == null || buttons.length <= i ) return;
		buttons[i].setEnabled(state);
	}
	
	private void updateButtons() {
		int cnt = table.getItemCount();
		int pos = table.getSelectionIndex();
		buttonSetEnabled(0, pos > 0);
		buttonSetEnabled(1, pos != -1 && pos < (cnt - 1));
		buttonSetEnabled(3, cnt > 0);
		buttonSetEnabled(4, cnt > 0);
	}

	private class TableData {
		String key;
		String value;
		public TableData (String _key, String _value) {
			key   = _key;
			value = _value;
		}
		@Override
		public String toString() { return value; } 
	}
	
	private void getExtensions() {
		mapItems.clear();
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(
				LibHover.LIBHOVER_DOC_EXTENSION);
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
	private void updateCheckboxData(boolean useProject) {
		// Combine user preference string with existing libhover extensions.
		getExtensions();
		HashMap<String,String> copyMap = (HashMap<String,String>)mapItems.clone();
		ArrayList<TableData> data = new ArrayList<TableData>();
		ArrayList<TableData> checkedData = new ArrayList<TableData>();
		// See what the user has set in preferences/properties and make sure that
		// any libhover referred to is a valid current extension.
		// Drop any preference for a non-existent extension.
		String prop = null;
		String pref = null;
		// User wants to use project settings.  If they already exist, honor them.
		if (useProject)
			prop = getPropertyString();
		// If no property settings exist or the user has not asked for project
		// specific settings, then use the preferences as default.
		if (prop == null)
		    pref = getPreferenceStore().getString(PreferenceConstants.P_LIBHOVER);
		else // otherwise use the property settings
			pref = prop;
		// The property string has the same format as the preference string which is
		// id1:boolean1:id2:boolean2:id3:boolean3 (where booleanx is checked or not)
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
		// Add any remaining libhover extensions to end of list.  If there is an
		// existing property string, we treat them as unchecked.  If we are
		// defaulting to the preferences, then we treat them as checked.
		String[] leftovers = new String[copyMap.size()];
		leftovers = copyMap.keySet().toArray(leftovers);
		for (int i = 0; i < leftovers.length; ++i) {
			TableData d = new TableData(leftovers[i], copyMap.get(leftovers[i]));
			data.add(d);
			if (!useProject || prop == null)
				checkedData.add(d);
		}
		tv.setInput(data.toArray());
		tv.setCheckedElements(checkedData.toArray());
		updateButtons();
	}
	
	private void save() throws CoreException {
		// if we are not using project-specific property, then we
		// set it to be null
		if (!projectSpecific.getSelection())
			getProject().setPersistentProperty(libhoverProperty, null);
		
		// Otherwise form a string which has all of the libhovers in order
		// with their boolean checked value.  Use ":" to separate all entries
		boolean inRange = true;
		int i = 0;
		String outString = new String();
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
		// Save formatted string to project.
		if (outString.length() > 1)
			getProject().setPersistentProperty(libhoverProperty, outString);
	}
}