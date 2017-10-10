/*******************************************************************************
 * Copyright (c) 2013, 2017 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified for use in OSIO connector
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.SingleTaskDataCollector;
import org.eclipse.mylyn.commons.core.operations.IOperationMonitor;
import org.eclipse.mylyn.commons.core.operations.IOperationMonitor.OperationFlag;
import org.eclipse.mylyn.commons.core.operations.OperationUtil;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;

public class OSIORestTaskDataHandler extends AbstractTaskDataHandler {
	protected final OSIORestConnector connector;

	public OSIORestTaskDataHandler(OSIORestConnector connector) {
		this.connector = connector;
	}

	@Override
	public RepositoryResponse postTaskData(TaskRepository repository, TaskData taskData,
			Set<TaskAttribute> oldAttributes, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask("Submitting_task", IProgressMonitor.UNKNOWN);
			OSIORestClient client = connector.getClient(repository);
			try {
				IOperationMonitor progress = OperationUtil.convert(monitor, "post taskdata", 3);
				return client.postTaskData(taskData, oldAttributes, repository, progress);
			} catch (OSIORestException e) {
				throw new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN, 2,
						"Error post taskdata.\n\n" + e.getMessage(), e));
			}
		} finally {
			monitor.done();
		}
	}

	@Override
	public boolean initializeTaskData(TaskRepository repository, TaskData data, ITaskMapping initializationData,
			IProgressMonitor monitor) throws CoreException {
		// Note: setting current version to latest assumes the data arriving
		// here is either for a new task or is
		// fresh from the repository (not locally stored data that may not have
		// been migrated).
		data.setVersion("0"); //$NON-NLS-1$
		if (data.isNew()) {
			OSIORestCreateTaskSchema.getDefault().initialize(data);
		} else {
			OSIORestTaskSchema.getDefault().initialize(data);
		}
		if (initializationData != null) {
			connector.getTaskMapping(data).merge(initializationData);
		}
		OSIORestConfiguration config = connector.getRepositoryConfiguration(repository);
		if (config != null) {
			config.updateInitialTaskData(data);
		}

		return true;
	}

	@Override
	public TaskAttributeMapper getAttributeMapper(TaskRepository repository) {
		return new OSIORestTaskAttributeMapper(repository, connector);
	}

	public TaskData getTaskData(TaskRepository repository, String taskId, IProgressMonitor monitor)
			throws CoreException {

		Set<String> taskIds = new HashSet<String>();
		taskIds.add(taskId);
		SingleTaskDataCollector singleTaskDataCollector = new SingleTaskDataCollector();
		getMultiTaskData(repository, taskIds, singleTaskDataCollector, monitor);

		if (singleTaskDataCollector.getTaskData() == null) {
			throw new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN,
					"Task data could not be retrieved. Please re-synchronize task")); //$NON-NLS-1$
		}
		return singleTaskDataCollector.getTaskData();
	}

	@Override
	public void getMultiTaskData(final TaskRepository repository, Set<String> taskIds,
			final TaskDataCollector collector, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask("retrive_task", IProgressMonitor.UNKNOWN);
			OSIORestClient client = connector.getClient(repository);
			try {
				IOperationMonitor progress = OperationUtil.convert(monitor, "post taskdata", 3);
				progress.addFlag(OperationFlag.BACKGROUND);
				client.getTaskData(taskIds, repository, collector, progress);
			} catch (OSIORestException e) {
				throw new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN, 2,
						"Error get taskdata.\n\n" + e.getMessage(), e));
			}
		} finally {
			monitor.done();
		}
	}
}
