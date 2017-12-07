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

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Identity;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.LinkResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.RestResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.SingleRestResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Space;
import org.eclipse.mylyn.commons.core.operations.IOperationMonitor;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import com.google.gson.reflect.TypeToken;

@SuppressWarnings("restriction")
public interface IOSIORestRequestProvider {

	public <R extends RestResponse<?>> R getRequest(IOperationMonitor monitor, CommonHttpClient client,
			String urlSuffix, TypeToken<?> responseType) throws OSIORestException;

	public <R extends SingleRestResponse<?>> R getSingleRequest(IOperationMonitor monitor, CommonHttpClient client,
			String urlSuffix, TypeToken<?> responseType) throws OSIORestException;

	public <R extends RestResponse<?>> R getRequest(IOperationMonitor monitor, CommonHttpClient client,
			String urlSuffix, TypeToken<?> responseType, boolean authrequired) throws OSIORestException;

	public <R extends SingleRestResponse<?>> R getSingleRequest(IOperationMonitor monitor, CommonHttpClient client,
			String urlSuffix, TypeToken<?> responseType, boolean authrequired) throws OSIORestException;

	public <R extends RestResponse<?>> R getRequest(IOperationMonitor monitor, CommonHttpClient client,
			String urlSuffix, TypeToken<?> responseType, boolean authrequired, boolean authURLrequired)
			throws OSIORestException;

	public <R extends SingleRestResponse<?>> R getSingleRequest(IOperationMonitor monitor, CommonHttpClient client,
			String urlSuffix, TypeToken<?> responseType, boolean authrequired, boolean authURLrequired)
			throws OSIORestException;

	public String postNewTask(IOperationMonitor monitor, CommonHttpClient client, TaskData taskData, Space space,
			OSIORestConnector connector, TaskRepository repository) throws OSIORestException, CoreException;

	public TaskAttribute postNewCommentTask(IOperationMonitor monitor, CommonHttpClient client, TaskData taskData,
			Set<TaskAttribute> oldAttributes) throws OSIORestException;

	public TaskData patchUpdateTask(IOperationMonitor monitor, CommonHttpClient client, TaskData taskData,
			Set<TaskAttribute> oldAttributes, Space space) throws OSIORestException;

	public String deleteLink(IOperationMonitor monitor, CommonHttpClient client, String wid, String id)
			throws OSIORestException;

	public LinkResponse postNewLink(IOperationMonitor monitor, CommonHttpClient client, String linkid, String sourceid,
			String targetid, boolean isForward) throws OSIORestException;

	public String getWID(IOperationMonitor monitor, CommonHttpClient client, String query,
			TaskRepository taskRepository) throws OSIORestException;

	public List<TaskData> getTaskData(IOperationMonitor monitor, CommonHttpClient client, OSIORestConnector connector,
			String queryUrlSuffix, TaskRepository taskRepository) throws OSIORestException, CoreException;

	public TaskData getSingleTaskData(IOperationMonitor monitor, CommonHttpClient client, OSIORestConnector connector,
			String workitemquery, TaskRepository taskRepository) throws OSIORestException, CoreException;

	public List<TaskAttribute> getTaskComments(IOperationMonitor monitor, CommonHttpClient client, Space space,
			TaskData taskData) throws OSIORestException;

	public TaskAttribute getTaskCreator(IOperationMonitor monitor, CommonHttpClient client, TaskData taskData)
			throws OSIORestException;

	public TaskAttribute getTaskLabels(IOperationMonitor monitor, CommonHttpClient client, Space space,
			TaskData taskData) throws OSIORestException;

	public TaskAttribute getTaskLinks(IOperationMonitor monitor, CommonHttpClient client, OSIORestClient restClient,
			Space space, TaskData taskData, OSIORestConfiguration config) throws OSIORestException;

	public TaskAttribute getTaskAssignee(IOperationMonitor monitor, CommonHttpClient client, String id,
			TaskData taskData) throws OSIORestException;
	
	public Identity getAuthUser(IOperationMonitor monitor, CommonHttpClient client) throws OSIORestException;

}
