/*******************************************************************************
 * Copyright (c) 2004, 2009, 2017 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Red Hat Inc - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mylyn.commons.workbench.InPlaceCheckBoxTreeDialog;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.CheckboxMultiSelectAttributeEditor;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.osgi.util.NLS;

/**
 * @author Rob Elves
 */
public class OSIOKeywordAttributeEditor extends CheckboxMultiSelectAttributeEditor {

	public OSIOKeywordAttributeEditor(TaskDataModel manager, TaskAttribute taskAttribute) {
		super(manager, taskAttribute);
	}

	@Override
	public List<String> getValues() {
		List<String> values = new ArrayList<String>();
		values = getTaskAttribute().getValues();
		return values;
	}

	@Override
	public void setValues(List<String> newValues) {
		Collections.sort(newValues);
		getTaskAttribute().clearValues();
		getTaskAttribute().setValues(newValues);
		attributeChanged();
	}

	@Override
	protected List<String> getValueList() {
		return getValues();
	}

	@Override
	public List<String> getValuesLabels() {
		List<String> tmp = getTaskAttribute().getValues();
		List<String> newStrs = new ArrayList<>(tmp);
		return newStrs;
	}

	@Override
	protected InPlaceCheckBoxTreeDialog createInPlaceCheckBoxTreeDialog(List<String> values) {
		Map<String, String> validDescriptions = getTaskAttribute().getOptions();
		LinkedHashMap<String, String> validValues = new LinkedHashMap<String, String>(validDescriptions.size());
		for (String value : validDescriptions.keySet()) {
			validValues.put(value, value);
		}
		return new InPlaceCheckBoxTreeDialog(WorkbenchUtil.getShell(), getButton(), values, validValues,
				NLS.bind(Messages.OSIOKeywordAttributeEditor_Select_X, getLabel()), validDescriptions);
	}

}