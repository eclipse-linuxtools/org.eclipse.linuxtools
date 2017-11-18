/*******************************************************************************
 * Copyright (c) 2014, 2017 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.IOSIORestConstants;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestCore;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestTaskSchema;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;

public class OSIORestTaskEditorPage extends AbstractTaskEditorPage {

	public OSIORestTaskEditorPage(TaskEditor editor) {
		this(editor, OSIORestCore.CONNECTOR_KIND);
	}

	public OSIORestTaskEditorPage(TaskEditor editor, String connectorKind) {
		super(editor, connectorKind);
		setNeedsPrivateSection(true);
		setNeedsSubmitButton(true);
	}

	@Override
	protected AttributeEditorFactory createAttributeEditorFactory() {
		AttributeEditorFactory factory = new AttributeEditorFactory(getModel(), getTaskRepository(), getEditorSite()) {

			@Override
			public AbstractAttributeEditor createEditor(String type, TaskAttribute taskAttribute) {
				AbstractAttributeEditor editor;
				if (IOSIORestConstants.EDITOR_TYPE_ASSIGNEES.equals(type)) {
					editor = new OSIOAssigneeAttributeEditor(getModel(), taskAttribute);
				} else if (IOSIORestConstants.EDITOR_TYPE_LABELS.equals(type)) {
					editor = new OSIOLabelAttributeEditor(getModel(), taskAttribute);
				} else if (IOSIORestConstants.EDITOR_TYPE_LINKS.equals(type)) {
					editor = new OSIOLinksAttributeEditor(getModel(), taskAttribute);
				} else if (IOSIORestConstants.EDITOR_TYPE_ADD_LINKS.equals(type)) {
					editor = new OSIOAddLinksAttributeEditor(getModel(), getConnector(), taskAttribute);
				} else if (IOSIORestConstants.EDITOR_TYPE_KEYWORD.equals(type)) {
					editor = new OSIOKeywordAttributeEditor(getModel(), taskAttribute);
				} else {
					editor = super.createEditor(type, taskAttribute);
				}
				if (editor != null
						&& OSIORestTaskSchema.getDefault().ADD_ASSIGNEE.getKey().equals(taskAttribute.getId())) {
					editor.setLayoutHint(new LayoutHint(RowSpan.SINGLE, ColumnSpan.SINGLE));
				}
				if (editor != null
						&& OSIORestTaskSchema.getDefault().ADD_LABEL.getKey().equals(taskAttribute.getId())) {
					editor.setLayoutHint(new LayoutHint(RowSpan.SINGLE, ColumnSpan.SINGLE));
				}
				if (editor != null
						&& OSIORestTaskSchema.getDefault().ADD_LINK.getKey().equals(taskAttribute.getId())) {
					editor.setLayoutHint(new LayoutHint(RowSpan.SINGLE, ColumnSpan.SINGLE));
				}

				return editor;
			}
		};
		return factory;
	}

	@Override
	protected Set<TaskEditorPartDescriptor> createPartDescriptors() {
		Set<TaskEditorPartDescriptor> descriptors = super.createPartDescriptors();
		// remove unnecessary default editor parts
		ArrayList<TaskEditorPartDescriptor> descriptorsToRemove = new ArrayList<TaskEditorPartDescriptor>(2);
		for (TaskEditorPartDescriptor taskEditorPartDescriptor : descriptors) {
			if (taskEditorPartDescriptor.getId().equals(ID_PART_PEOPLE)) {
				descriptorsToRemove.add(taskEditorPartDescriptor);
				continue;
			}
		}
		descriptors.removeAll(descriptorsToRemove);

		// Add the updated OSIO people part
		descriptors.add(new TaskEditorPartDescriptor(ID_PART_PEOPLE) {
			@Override
			public AbstractTaskEditorPart createPart() {
				return new OSIORestTaskEditorPeoplePart();
			}
		}.setPath(PATH_PEOPLE));

		return descriptors;
	}

}
