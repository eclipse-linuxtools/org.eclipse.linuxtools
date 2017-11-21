/*******************************************************************************
 * Copyright (c) 2015 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui;

import java.util.Arrays;
import java.util.List;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;

public class OSIOLabelsAttributeEditor extends OSIOLongTextAttributeEditor {

	private static final String VALUE_SEPARATOR = ","; //$NON-NLS-1$

	private final boolean isMultiSelect;

	public OSIOLabelsAttributeEditor(TaskDataModel manager, TaskAttribute taskAttribute) {
		super(manager, taskAttribute);
		this.isMultiSelect = true;
		if (!isReadOnly() && isMultiSelect) {
			setLayoutHint(new LayoutHint(RowSpan.MULTIPLE, ColumnSpan.MULTIPLE));
		}
	}

	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		super.createControl(parent, toolkit,
				(getLayoutHint() != null && getLayoutHint().rowSpan == RowSpan.MULTIPLE ? SWT.WRAP : SWT.NONE));
		if (!isReadOnly() && isMultiSelect) {
			getText().setToolTipText("Separate multiple values with a comma"); //$NON-NLS-1$
		}
	}

	@Override
	public String getValue() {
		if (isMultiSelect) {
			List<String> values = getAttributeMapper().getValues(getTaskAttribute());
			return Joiner.on(VALUE_SEPARATOR + " ").skipNulls().join(values); //$NON-NLS-1$
		} else {
			return getAttributeMapper().getValue(getTaskAttribute());
		}
	}

	@Override
	public void setValue(String text) {
		if (isMultiSelect) {
			String[] values = text.split(VALUE_SEPARATOR);
			getAttributeMapper().setValues(getTaskAttribute(), getTrimmedValues(values));
		} else {
			getAttributeMapper().setValue(getTaskAttribute(), text);
		}
		attributeChanged();
	}

	public static List<String> getTrimmedValues(String[] values) {
		return FluentIterable.from(Arrays.asList(values)).transform(new Function<String, String>() {
			@Override
			public String apply(String input) {
				return Strings.nullToEmpty(input).trim();
			}
		}).filter(new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				return !Strings.isNullOrEmpty(input);
			}
		}).toList();
	}
}
