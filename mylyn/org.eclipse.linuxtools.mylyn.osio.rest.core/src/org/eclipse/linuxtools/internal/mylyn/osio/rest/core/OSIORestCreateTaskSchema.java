/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import org.eclipse.mylyn.tasks.core.data.AbstractTaskSchema;
import org.eclipse.mylyn.tasks.core.data.DefaultTaskSchema;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

public class OSIORestCreateTaskSchema extends AbstractTaskSchema {

	private static final OSIORestCreateTaskSchema instance = new OSIORestCreateTaskSchema();
	
	public static OSIORestCreateTaskSchema getDefault() {
		return instance;
	}

	private final DefaultTaskSchema parent = DefaultTaskSchema.getInstance();

	public final Field SPACE = createField("space", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaSpace.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_SINGLE_SELECT, Flag.ATTRIBUTE, Flag.REQUIRED);
	
	public final Field DESCRIPTION = inheritFrom(parent.DESCRIPTION).addFlags(Flag.REQUIRED).create();

	public final Field WORKITEM_TYPE = createField("baseType", //$NON-NLS-1$
			OSIORestMessages.getString("SchemaWorkitemType.label"), //$NON-NLS-1$
			TaskAttribute.TYPE_SINGLE_SELECT, null, SPACE.getKey(), Flag.ATTRIBUTE, Flag.REQUIRED);
	
	public final Field TITLE = inheritFrom(parent.SUMMARY).addFlags(Flag.REQUIRED).create();
	
}
