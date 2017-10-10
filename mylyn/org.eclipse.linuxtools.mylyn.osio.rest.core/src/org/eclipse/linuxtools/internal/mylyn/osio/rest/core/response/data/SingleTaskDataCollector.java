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
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;

public class SingleTaskDataCollector extends TaskDataCollector {
	final TaskData[] retrievedData = new TaskData[1];

	@Override
	public void accept(TaskData taskData) {
		retrievedData[0] = taskData;
	}

	public TaskData getTaskData() {
		return retrievedData[0];
	}

}