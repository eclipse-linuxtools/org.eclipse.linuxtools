/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.ui.services.IServiceLocator;

public class RepositoryQueryAttributeEditorFactory extends AttributeEditorFactory {
	
	private final TaskDataModel taskModel;

	public RepositoryQueryAttributeEditorFactory(@NonNull TaskDataModel model, @NonNull TaskRepository taskRepository) {
		super(model, taskRepository);
		this.taskModel = model;
	}

	public RepositoryQueryAttributeEditorFactory(@NonNull TaskDataModel model, @NonNull TaskRepository taskRepository,
			@Nullable IServiceLocator serviceLocator) {
		super(model, taskRepository, serviceLocator);
		this.taskModel = model;
	}
	
	@NonNull
	public AbstractAttributeEditor createEditor(@NonNull String type, @NonNull TaskAttribute taskAttribute) {
		if (TaskAttribute.TYPE_MULTI_SELECT.equals(type)) {
			return new RepositoryQueryAttributeEditor(taskModel, taskAttribute);
		}
		return super.createEditor(type, taskAttribute);
	}

}
