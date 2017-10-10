/*******************************************************************************
 * Copyright (c) 2015, 2017 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.IdNamed;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.SortableActiveEntry;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Space;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;

public class OSIORestTaskAttributeMapper extends TaskAttributeMapper {

	private final OSIORestConnector connector;

	public OSIORestTaskAttributeMapper(TaskRepository taskRepository, OSIORestConnector connector) {
		super(taskRepository);
		this.connector = connector;
	}

	@Override
	public Map<String, String> getOptions(@NonNull TaskAttribute attribute) {
		OSIORestTaskSchema taskSchema = OSIORestTaskSchema.getDefault();
		if (attribute.getId().equals(taskSchema.WORKITEM_TYPE.getKey())
				|| attribute.getId().equals(taskSchema.AREA.getKey())
				|| attribute.getId().equals(taskSchema.ASSIGNEES.getKey())
				|| attribute.getId().equals(taskSchema.ITERATION.getKey())) {
			TaskAttribute spaceAttribute = attribute.getParentAttribute()
					.getAttribute(OSIORestCreateTaskSchema.getDefault().SPACE.getKey());
			OSIORestConfiguration repositoryConfiguration;
			try {
				repositoryConfiguration = connector.getRepositoryConfiguration(this.getTaskRepository());
				// TODO: change this when we have offline cache for the repository configuration so we build the options in an temp var
				if (repositoryConfiguration != null) {
					if (!spaceAttribute.getValue().equals("")) { //$NON-NLS-1$
						boolean found = false;
						attribute.clearOptions();
						for (String spaceName : spaceAttribute.getValues()) {
							Space actualSpace = repositoryConfiguration.getSpaceWithName(spaceName);
							String key = attribute.getId();
							internalSetAttributeOptions4Space(attribute, actualSpace.getMapFor(attribute.getId()));
						}
					}
				}
			} catch (CoreException e) {
				StatusHandler.log(new RepositoryStatus(getTaskRepository(), IStatus.ERROR, OSIORestCore.ID_PLUGIN,
						0, "Failed to obtain repository configuration", e)); //$NON-NLS-1$
			}
		}
		return super.getOptions(attribute);
	}

	private void internalSetAttributeOptions4Space(TaskAttribute taskAttribute,
			Map<String, IdNamed> optionMap) {
		boolean found = false;
		String actualValue = taskAttribute.getValue();
		for (IdNamed entry : optionMap.values()) {
			taskAttribute.putOption(entry.getName(), entry.getName());
			found |= actualValue.equals(entry.getName());
		}
		if (!found) {
			taskAttribute.setValue(""); //$NON-NLS-1$
		}
	}

	@Override
	public String mapToRepositoryKey(@NonNull TaskAttribute parent, @NonNull String key) {
		if (key.equals(TaskAttribute.TASK_KEY)) {
			return OSIORestTaskSchema.getDefault().ID.getKey();
		} else if (key.equals(TaskAttribute.STATUS)) {
			return OSIORestTaskSchema.getDefault().STATUS.getKey();
		}
		else {
			return super.mapToRepositoryKey(parent, key);
		}
	}

}