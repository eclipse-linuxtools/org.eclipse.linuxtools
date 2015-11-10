/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.vagrant.ui.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

public class CreateVMAdvancedComposite extends ExpandableComposite {
	private CreateVMPageModel model;
	private final TableViewer tv;
	private static final String KEY = WizardMessages
			.getString("CreateVMAdvancedComposite.Key");
	private static final String VALUE = WizardMessages
			.getString("CreateVMAdvancedComposite.Value");

	private static final String DEFAULT_KEY = WizardMessages
			.getString("CreateVMAdvancedComposite.DefaultKey");
	private static final String DEFAULT_VALUE = WizardMessages
			.getString("CreateVMAdvancedComposite.DefaultValue");

	private String[] KEY_VALUE = new String[] { KEY, VALUE };

	public CreateVMAdvancedComposite(Composite parent,
			CreateVMPageModel model) {
		super(parent, ExpandableComposite.COMPACT | ExpandableComposite.TWISTIE
				| ExpandableComposite.EXPANDED | SWT.BORDER);
		this.model = model;

		setText(WizardMessages
				.getString("CreateVMAdvancedComposite.EnvironmentVariables"));

		final Composite advancedComposite = new Composite(this, SWT.NONE);
		setClient(advancedComposite);
		addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				Shell shell = parent.getShell();
				Point minSize = shell.getMinimumSize();
				shell.setMinimumSize(shell.getSize().x, minSize.y);
				shell.pack();
				parent.layout();
				shell.setMinimumSize(minSize);
			}
		});

		advancedComposite.setLayout(new GridLayout());

		tv = new TableViewer(advancedComposite,
				SWT.FULL_SELECTION | SWT.BORDER);
		tv.setContentProvider(new EnvironmentContentProvider());
		tv.setLabelProvider(new EnvironmentLabelProvider());

		// Set up the table
		Table table = tv.getTable();
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 150;
		table.setLayoutData(gd);

		TableColumn tc1 = new TableColumn(table, SWT.CENTER);
		TableColumn tc2 = new TableColumn(table, SWT.CENTER);

		tc1.setText(KEY);
		tc2.setText(VALUE);

		tc1.setWidth(250);
		tc2.setWidth(150);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// Create the cell editors
		CellEditor[] editors = new CellEditor[2];
		editors[0] = new TextCellEditor(table);
		editors[1] = new TextCellEditor(table);

		// Set the editors, cell modifier, and column properties
		tv.setColumnProperties(KEY_VALUE);
		tv.setCellModifier(new EnvironmentCellModifier());
		tv.setCellEditors(editors);

		tv.setInput(ResourcesPlugin.getWorkspace());

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				String varToAdd = DEFAULT_KEY;
				int i = 1;
				if (model.getEnvironment().containsKey(varToAdd)) {
					boolean done = false;
					while (!done) {
						String toTest = varToAdd + " (" + i++ + ")";
						if (!model.getEnvironment().containsKey(toTest)) {
							model.getEnvironment().put(toTest, DEFAULT_VALUE);
							done = true;
						}
					}
				} else {
					model.getEnvironment().put(varToAdd, DEFAULT_VALUE);
				}

				tv.refresh();
			}
		});

	}

	public void refresh() {
		if (tv != null && !tv.getTable().isDisposed())
			tv.refresh();
	}

	private class EnvironmentContentProvider
			implements IStructuredContentProvider {
		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput,
				Object newInput) {
		}

		@Override
		public Object[] getElements(Object inputElement) {
			final Map<String, String> env = model.getEnvironment();
			if (env != null) {
				ArrayList<String> keys = new ArrayList<>(env.keySet());
				Collections.sort(keys, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						// anything with default value should sit at the bottom
						boolean o1IsDefaultValue = env.get(o1)
								.equals(DEFAULT_VALUE);
						boolean o2IsDefaultValue = env.get(o2)
								.equals(DEFAULT_VALUE);
						if (o1IsDefaultValue) {
							if (o2IsDefaultValue)
								return o1.compareTo(o2);
							return 1;
						}
						if (o2IsDefaultValue)
							return -1;
						if (o1.startsWith(DEFAULT_KEY)) {
							if (o2.startsWith(DEFAULT_KEY))
								return env.get(o1).compareTo(env.get(o2));
							return 1;
						}
						if (o2.startsWith(DEFAULT_KEY))
							return -1;
						return o1.compareTo(o2);
					}
				});
				return keys.toArray(new String[keys.size()]);
			}
			return new String[0];
		}
	}

	private class EnvironmentLabelProvider extends LabelProvider
			implements ITableLabelProvider {
		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (columnIndex == 0)
				return (String) element;
			if (columnIndex == 1)
				return model.getEnvironment().get(element);
			return null;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}
	}

	private class EnvironmentCellModifier implements ICellModifier {
		@Override
		public boolean canModify(Object element, String property) {
			return true;
		}

		@Override
		public Object getValue(Object element, String property) {
			if (property == KEY)
				return element;
			if (property == VALUE) {
				return model.getEnvironment().get(element);
			}
			return "";
		}

		@Override
		public void modify(Object element, String property, Object value) {
			String data = (String) ((TableItem) element).getData();
			if (property == KEY) {
				String v = (String) value;
				if (v == null || v.isEmpty()) {
					model.getEnvironment().remove(data);
				} else {
					String currentVal = model.getEnvironment().get(data);
					model.getEnvironment().remove(data);
					model.getEnvironment().put((String) value, currentVal);
				}
			} else if (property == VALUE) {
				String v = (String) value;
				if (v == null || v.isEmpty()) {
					model.getEnvironment().remove(data);
				} else {
					model.getEnvironment().put(data, (String) value);
				}
			}
			tv.refresh();
		}
	}
}
