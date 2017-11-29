/*******************************************************************************
 * Copyright (c) 2008, 2011 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Steffen Pingel
 */
public class RepositoryQueryAttributeEditor extends AbstractAttributeEditor {

	private String[] allValues;

	private Text text;

	private List list;

	protected boolean suppressRefresh;

	public RepositoryQueryAttributeEditor(TaskDataModel manager, TaskAttribute taskAttribute) {
		super(manager, taskAttribute);
		if (isReadOnly()) {
			setLayoutHint(new LayoutHint(RowSpan.SINGLE, ColumnSpan.SINGLE));
		} else {
			setLayoutHint(new LayoutHint(RowSpan.MULTIPLE, ColumnSpan.SINGLE));
		}
	}

	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		if (isReadOnly()) {
			text = new Text(parent, SWT.FLAT | SWT.READ_ONLY | SWT.WRAP);
			text.setFont(JFaceResources.getDefaultFont());
			toolkit.adapt(text, false, false);
			text.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
			select(getValues(), getValuesLabels());
			text.setToolTipText(getDescription());
			setControl(text);
		} else {
			list = new List(parent, SWT.FLAT | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
			toolkit.adapt(list, false, false);
			list.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
			list.setFont(JFaceResources.getDefaultFont());
			list.setToolTipText(getDescription());

			updateListWithOptions();

			select(getValues(), getValuesLabels());

			if (allValues != null) {
				list.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Assert.isNotNull(allValues);
						int[] indices = list.getSelectionIndices();
						String[] selectedValues = new String[indices.length];
						for (int i = 0; i < indices.length; i++) {
							int index = indices[i];
							Assert.isLegal(index >= 0 && index <= allValues.length - 1);
							selectedValues[i] = allValues[index];
						}
						try {
							suppressRefresh = true;
							setValues(selectedValues);
						} finally {
							suppressRefresh = false;
						}
					}
				});
				list.showSelection();
			}

			setControl(list);
		}
	}

	private void updateListWithOptions() {
		if (list != null && !list.isDisposed()) {
			Map<String, String> labelByValue = getAttributeMapper().getOptions(getTaskAttribute());
			if (labelByValue != null) {
				int topIndex = list.getTopIndex();
				list.removeAll();
				allValues = labelByValue.keySet().toArray(new String[0]);
				for (String value : allValues) {
					list.add(labelByValue.get(value));
				}
				if (topIndex > 0) {
					list.setTopIndex(topIndex);
				}
			}
		}
	}

	public String[] getValues() {
		return getAttributeMapper().getValues(getTaskAttribute()).toArray(new String[0]);
	}

	public String[] getValuesLabels() {
		return getAttributeMapper().getValueLabels(getTaskAttribute()).toArray(new String[0]);
	}

	private void select(String[] values, String[] labels) {
		if (text != null && !text.isDisposed()) {
			StringBuilder valueString = new StringBuilder();
			if (labels != null) {
				for (int i = 0; i < labels.length; i++) {
					valueString.append(labels[i]);
					if (i != labels.length - 1) {
						valueString.append(", "); //$NON-NLS-1$
					}
				}
			}
			text.setText(valueString.toString());

		} else if (list != null && !list.isDisposed()) {
			if (values != null) {
				list.deselectAll();
				Set<String> selectedValues = new HashSet<String>(Arrays.asList(values));
				for (int i = 0; i < allValues.length; i++) {
					if (selectedValues.contains(allValues[i])) {
						list.select(i);
					}
				}
			} else {
				list.setItems(labels);
				list.setSelection(labels);
			}
		}
	}

	public void setValues(String[] values) {
		getAttributeMapper().setValues(getTaskAttribute(), Arrays.asList(values));
		attributeChanged();
	}

	@Override
	public void refresh() {
		updateListWithOptions();
		select(getValues(), getValuesLabels());
	}

	@Override
	public boolean shouldAutoRefresh() {
		return !suppressRefresh;
	}
}
