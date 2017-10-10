/*******************************************************************************
 * Copyright (c) 2016, 2017 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Red Hat Inc - modified to use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestTaskSchema;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorPeoplePart;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;

public class OSIORestTaskEditorPeoplePart extends TaskEditorPeoplePart {

	@Override
	protected Collection<TaskAttribute> getAttributes() {
		Map<String, TaskAttribute> allAttributes = getTaskData().getRoot().getAttributes();
		List<TaskAttribute> attributes = new ArrayList<TaskAttribute>(allAttributes.size());
		attributes.add(getTaskData().getRoot().getMappedAttribute(OSIORestTaskSchema.getDefault().ASSIGNEES.getKey()));
		for (TaskAttribute attribute : allAttributes.values()) {
			TaskAttributeMetaData properties = attribute.getMetaData();
			if (TaskAttribute.KIND_PEOPLE.equals(properties.getKind())) {
				if (!attributes.contains(attribute)) {
					attributes.add(attribute);
				}
			}
		}
		return attributes;
	}

	@Override
	protected GridDataFactory createLayoutData(AbstractAttributeEditor editor) {
		LayoutHint layoutHint = editor.getLayoutHint();
		GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().indent(3, 0);// prevent clipping of decorators on Mac
		if (layoutHint != null && layoutHint.rowSpan == RowSpan.MULTIPLE) {
			gridDataFactory.grab(true, true).align(SWT.FILL, SWT.FILL).hint(130, 95);
		} else {
			gridDataFactory.grab(true, false).align(SWT.FILL, SWT.TOP);

		}
		return gridDataFactory;
	}

}
