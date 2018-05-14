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
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Identity;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.LinkResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.RestResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.SingleRestResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Space;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItem;
import org.eclipse.mylyn.commons.core.operations.IOperationMonitor;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import com.google.gson.reflect.TypeToken;

@SuppressWarnings("restriction")
public class DefaultOSIORestRequestProvider implements IOSIORestRequestProvider {

	@Override
	public <R extends RestResponse<?>> R getRequest(IOperationMonitor monitor, CommonHttpClient client, String urlSuffix, TypeToken<?> responseType) 
			throws OSIORestException {
		return new OSIORestGetRequest<R>(client, urlSuffix, responseType).run(monitor);
	}

	@Override
	public <R extends RestResponse<?>> R getRequest(IOperationMonitor monitor, CommonHttpClient client, String urlSuffix, TypeToken<?> responseType,
			boolean authrequired) throws OSIORestException {
		return new OSIORestGetRequest<R>(client, urlSuffix, responseType, authrequired).run(monitor);
	}

	@Override
	public <R extends RestResponse<?>> R getRequest(IOperationMonitor monitor, CommonHttpClient client, String urlSuffix, TypeToken<?> responseType,
			boolean authrequired, boolean authURLrequired) throws OSIORestException {
		return new OSIORestGetRequest<R>(client, urlSuffix, responseType, authrequired, authURLrequired).run(monitor);
	}
	
	@Override
	public <R extends SingleRestResponse<?>> R getSingleRequest(IOperationMonitor monitor, CommonHttpClient client, String urlSuffix, TypeToken<?> responseType,
			boolean authrequired, boolean authURLrequired) throws OSIORestException {
		return new OSIORestGetRequest<R>(client, urlSuffix, responseType, authrequired, authURLrequired).run(monitor);
	}

	@Override
	public String postNewTask(IOperationMonitor monitor, CommonHttpClient client, TaskData taskData, Space space,
			OSIORestConnector connector, TaskRepository repository) throws CoreException, OSIORestException {
		return new OSIORestPostNewTask(client, taskData, space, connector, repository).run(monitor);
	}

	@Override
	public TaskAttribute postNewCommentTask(IOperationMonitor monitor, CommonHttpClient client, TaskData taskData,
			Set<TaskAttribute> oldAttributes) throws OSIORestException {
		return new OSIORestPostNewCommentTask(client, taskData, oldAttributes).run(monitor);
	}

	@Override
	public TaskData patchUpdateTask(IOperationMonitor monitor, CommonHttpClient client, TaskData taskData,
			Set<TaskAttribute> oldAttributes, Space space) throws OSIORestException {
		return new OSIORestPatchUpdateTask(client, taskData, oldAttributes, space).run(monitor);
	}

	@Override
	public String deleteLink(IOperationMonitor monitor, CommonHttpClient client, String wid, String id)
			throws OSIORestException {
		return new OSIORestDeleteLink(client, wid, id).run(monitor);
	}

	@Override
	public LinkResponse postNewLink(IOperationMonitor monitor, CommonHttpClient client, String linkid,
			String sourceid, String targetid, boolean isForward) throws OSIORestException {
		return new OSIORestPostNewLink(client, linkid, sourceid, targetid, isForward).run(monitor);
	}

	@Override
	public String getWID(IOperationMonitor monitor, CommonHttpClient client, String query,
			TaskRepository taskRepository) throws OSIORestException {
		return new OSIORestGetWID(client, query, taskRepository).run(monitor);
	}

	@Override
	public TaskData getSingleTaskData(IOperationMonitor monitor, CommonHttpClient client,
			OSIORestConnector connector, String workitemquery, TaskRepository taskRepository)
			throws OSIORestException, CoreException {
		return new OSIORestGetSingleTaskData(client, connector, workitemquery, taskRepository).run(monitor);
	}

	@Override
	public List<TaskAttribute> getTaskComments(IOperationMonitor monitor, CommonHttpClient client, Space space,
			TaskData taskData) throws OSIORestException {
		return new OSIORestGetTaskComments(client, space,taskData).run(monitor);
	}

	@Override
	public TaskAttribute getTaskCreator(IOperationMonitor monitor, CommonHttpClient client, TaskData taskData)
			throws OSIORestException {
		return new OSIORestGetTaskCreator(client, taskData).run(monitor);
	}

	@Override
	public TaskAttribute getTaskLabels(IOperationMonitor monitor, CommonHttpClient client, Space space,
			TaskData taskData) throws OSIORestException {
		return new OSIORestGetTaskLabels(client, space, taskData).run(monitor);
	}

	@Override
	public TaskAttribute getTaskLinks(IOperationMonitor monitor, CommonHttpClient client, OSIORestClient restClient,
			Space space, TaskData taskData, OSIORestConfiguration config) throws OSIORestException {
		return new OSIORestGetTaskLinks(client, restClient, space, taskData, config).run(monitor);
	}

	@Override
	public TaskAttribute getTaskAssignee(IOperationMonitor monitor, CommonHttpClient client, String id,
			TaskData taskData) throws OSIORestException {
		return new OSIORestGetTaskAssignee(client, id, taskData).run(monitor);
	}

	@Override
	public List<TaskData> getTaskData(IOperationMonitor monitor, CommonHttpClient client,
			OSIORestConnector connector, String queryUrlSuffix, TaskRepository taskRepository)
			throws OSIORestException, CoreException {
		return new OSIORestGetTaskData(client, connector, queryUrlSuffix, taskRepository).run(monitor);
	}

	@Override
	public <R extends SingleRestResponse<?>> R getSingleRequest(IOperationMonitor monitor, CommonHttpClient client,
			String urlSuffix, TypeToken<?> responseType) throws OSIORestException {
		return new OSIORestGetRequest<R>(client, urlSuffix, responseType).run(monitor);
	}

	@Override
	public <R extends SingleRestResponse<?>> R getSingleRequest(IOperationMonitor monitor, CommonHttpClient client,
			String urlSuffix, TypeToken<?> responseType, boolean authrequired) throws OSIORestException {
		return new OSIORestGetRequest<R>(client, urlSuffix, responseType, authrequired).run(monitor);
	}

	@Override
	public Identity getAuthUser(IOperationMonitor monitor, CommonHttpClient client) throws OSIORestException {
		return new OSIORestGetAuthUser(client).run(monitor);
	}

	@Override
	public WorkItem getWorkItem(IOperationMonitor monitor, CommonHttpClient client, String id)
			throws OSIORestException {
		return new OSIORestGetWorkItem(client, id).run(monitor);
	}
}

