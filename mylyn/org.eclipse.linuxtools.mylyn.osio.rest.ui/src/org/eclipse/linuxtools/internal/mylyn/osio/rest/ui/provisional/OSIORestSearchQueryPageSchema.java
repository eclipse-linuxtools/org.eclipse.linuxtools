/*******************************************************************************
 * Copyright (c) 2015, 2017 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Red Hat Inc. - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional;

import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestTaskSchema;
import org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractQueryPageSchema;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

public class OSIORestSearchQueryPageSchema extends AbstractQueryPageSchema {

	private final OSIORestTaskSchema parent = OSIORestTaskSchema.getDefault();

	private static final OSIORestSearchQueryPageSchema instance = new OSIORestSearchQueryPageSchema();

	public OSIORestSearchQueryPageSchema() {
	}

	public static OSIORestSearchQueryPageSchema getInstance() {
		return instance;
	}

	public final Field space = copyFrom(parent.SPACE).type(TaskAttribute.TYPE_MULTI_SELECT)
			.layoutPriority(11)
			.create();

	public final Field iteration = copyFrom(parent.ITERATION).type(TaskAttribute.TYPE_MULTI_SELECT)
			.dependsOn(parent.SPACE.getKey())
			.layoutPriority(11)
			.create();

	public final Field area = copyFrom(parent.AREA).type(TaskAttribute.TYPE_MULTI_SELECT)
			.dependsOn(parent.SPACE.getKey())
			.layoutPriority(11)
			.create();

	public final Field type = copyFrom(parent.WORKITEM_TYPE).type(TaskAttribute.TYPE_MULTI_SELECT)
			.label(Messages.OSIORestSearchQueryPage_WorkitemTypeLabel)
			.layoutPriority(11)
			.create();
	
	public final Field status = copyFrom(parent.STATUS).type(TaskAttribute.TYPE_MULTI_SELECT)
			.layoutPriority(11)
			.create();
	
	public final Field assignee = copyFrom(parent.ASSIGNEES).type(TaskAttribute.TYPE_MULTI_SELECT)
			.dependsOn(parent.SPACE.getKey())
			.layoutPriority(11)
			.create();
	
}
