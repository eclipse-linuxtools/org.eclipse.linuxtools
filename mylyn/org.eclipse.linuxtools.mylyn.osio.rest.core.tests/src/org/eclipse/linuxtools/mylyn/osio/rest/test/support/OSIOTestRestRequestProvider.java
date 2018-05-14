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
package org.eclipse.linuxtools.mylyn.osio.rest.test.support;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.IOSIORestRequestProvider;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestClient;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConfiguration;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConnector;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestException;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestResourceMovedPermanentlyException;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestTaskSchema;
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
public class OSIOTestRestRequestProvider implements IOSIORestRequestProvider {
	
	private Map<String, Object> requestMap = new HashMap<>();
	private Map<String, Object> postMap = new HashMap<>();
	private Map<String, String> relocations = new HashMap<>();
	
	public void addGetRequest (String url, Object object) {
		requestMap.put(url, object);
	}
	
	public void addPostRequest (String url, Object object) {
		postMap.put(url, object);
	}

	public void addRelocation (String url, String relocation) {
		relocations.put(url, relocation);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <R extends RestResponse<?>> R getRequest(IOperationMonitor monitor, CommonHttpClient client,
			String urlSuffix, TypeToken<?> responseType) throws OSIORestException {
		return (R)requestMap.get(urlSuffix);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends SingleRestResponse<?>> R getSingleRequest(IOperationMonitor monitor, CommonHttpClient client,
			String urlSuffix, TypeToken<?> responseType) throws OSIORestException {
		return (R)requestMap.get(urlSuffix);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends RestResponse<?>> R getRequest(IOperationMonitor monitor, CommonHttpClient client,
			String urlSuffix, TypeToken<?> responseType, boolean authrequired) throws OSIORestException {
		return (R)requestMap.get(urlSuffix);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends SingleRestResponse<?>> R getSingleRequest(IOperationMonitor monitor, CommonHttpClient client,
			String urlSuffix, TypeToken<?> responseType, boolean authrequired) throws OSIORestException {
		return (R)requestMap.get(urlSuffix);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends RestResponse<?>> R getRequest(IOperationMonitor monitor, CommonHttpClient client,
			String urlSuffix, TypeToken<?> responseType, boolean authrequired, boolean authURLrequired)
			throws OSIORestException {
		return (R)requestMap.get(urlSuffix);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <R extends SingleRestResponse<?>> R getSingleRequest(IOperationMonitor monitor, CommonHttpClient client,
			String urlSuffix, TypeToken<?> responseType, boolean authrequired, boolean authURLrequired)
			throws OSIORestException {
		return (R)requestMap.get(urlSuffix);
	}

	@Override
	public String postNewTask(IOperationMonitor monitor, CommonHttpClient client, TaskData taskData, Space space,
			OSIORestConnector connector, TaskRepository repository) throws OSIORestException, CoreException {
		TaskAttribute spaceAttr = taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().SPACE.getKey());
		return null;
	}

	@Override
	public TaskAttribute postNewCommentTask(IOperationMonitor monitor, CommonHttpClient client, TaskData taskData,
			Set<TaskAttribute> oldAttributes) throws OSIORestException {
		return null;
	}

	@Override
	public TaskData patchUpdateTask(IOperationMonitor monitor, CommonHttpClient client, TaskData taskData,
			Set<TaskAttribute> oldAttributes, Space space) throws OSIORestException {
		return null;
	}

	@Override
	public String deleteLink(IOperationMonitor monitor, CommonHttpClient client, String wid, String id)
			throws OSIORestException {
		String query = "/workitems/" + //$NON-NLS-1$ 
				wid + 
				"/relationships/links";
		requestMap.remove(query);
		return "ok";
	}

	@Override
	public LinkResponse postNewLink(IOperationMonitor monitor, CommonHttpClient client, String linkid, String sourceid,
			String targetid, boolean isForward) throws OSIORestException {
		String query = "/workitems/" + //$NON-NLS-1$ 
				(isForward ? sourceid : targetid) + 
				"/relationships/links";
		LinkResponse response = (LinkResponse)postMap.get(query);
		WorkItem source = (WorkItem)requestMap.get("/workitems/" + sourceid);
		WorkItem target = (WorkItem)requestMap.get("/workitems/" + targetid);
		String sourceNum = (String)source.getNumber();
		String targetNum = (String)target.getNumber();
		requestMap.put(query, response);
		
//		String link += " " + workitem.getTitle() + " [" //$NON-NLS-1$ //$NON-NLS-2$ 
//				+ otherWorkItem + "]"; //$NON-NLS-1$

		return response;
	}

	public class TestHeaderElement implements HeaderElement {
		
		private String name;
		private String value;
		
		public TestHeaderElement (String name, String value) {
			this.name = name;
			this.value = value;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public NameValuePair getParameter(int arg0) {
			return null;
		}

		@Override
		public NameValuePair getParameterByName(String arg0) {
			return null;
		}

		@Override
		public int getParameterCount() {
			return 0;
		}

		@Override
		public NameValuePair[] getParameters() {
			return null;
		}

		@Override
		public String getValue() {
			return value;
		}
	}
	
	public class TestHeader implements Header {
		
		private String location;
		
		public TestHeader (String location) {
			this.location = location;
		}
		
		@Override
		public HeaderElement[] getElements() {
			HeaderElement h = new TestHeaderElement("Location", location);
			HeaderElement[] elements = new HeaderElement[] {h};
			return elements; 
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public String getValue() {
			return null;
		}
		
	}
	
	
	@Override
	public String getWID(IOperationMonitor monitor, CommonHttpClient client, String query,
			TaskRepository taskRepository) throws OSIORestException {
		String location = relocations.get(query);
		TestHeader t = new TestHeader(location);
		throw new OSIORestResourceMovedPermanentlyException(t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TaskData> getTaskData(IOperationMonitor monitor, CommonHttpClient client, OSIORestConnector connector,
			String queryUrlSuffix, TaskRepository taskRepository) throws OSIORestException, CoreException {
		return (List<TaskData>)requestMap.get(queryUrlSuffix);
	}

	@Override
	public TaskData getSingleTaskData(IOperationMonitor monitor, CommonHttpClient client, OSIORestConnector connector,
			String workitemquery, TaskRepository taskRepository) throws OSIORestException, CoreException {
		return (TaskData)requestMap.get(workitemquery);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TaskAttribute> getTaskComments(IOperationMonitor monitor, CommonHttpClient client, Space space,
			TaskData taskData) throws OSIORestException {
		List<TaskAttribute> comments = (List<TaskAttribute>)requestMap.get("/workitems/" + taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().UUID.getKey()).getValue() + "/comments");
		TaskAttribute root = taskData.getRoot();
		for (TaskAttribute comment : comments) {
			root.deepAddCopy(comment);
		}
		return comments; 
	}

	@Override
	public WorkItem getWorkItem(IOperationMonitor monitor, CommonHttpClient client, String id) throws OSIORestException {
		WorkItem workitem = (WorkItem)requestMap.get("/workitems/" + id);
		return workitem; 
	}
	
	@Override
	public TaskAttribute getTaskCreator(IOperationMonitor monitor, CommonHttpClient client, TaskData taskData)
			throws OSIORestException {
		TaskAttribute creator = (TaskAttribute)requestMap.get("/users/" + taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().CREATOR_ID.getKey()).getValue());
		taskData.getRoot().deepAddCopy(creator);
		return creator;
	}

	@Override
	public TaskAttribute getTaskLabels(IOperationMonitor monitor, CommonHttpClient client, Space space,
			TaskData taskData) throws OSIORestException {
		TaskAttribute labels = (TaskAttribute)requestMap.get("/workitems/" + taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().UUID.getKey()).getValue() + "/labels");
		taskData.getRoot().deepAddCopy(labels);
		return labels;
	}

	@Override
	public TaskAttribute getTaskLinks(IOperationMonitor monitor, CommonHttpClient client, OSIORestClient restClient,
			Space space, TaskData taskData, OSIORestConfiguration config) throws OSIORestException {
		TaskAttribute links = (TaskAttribute)requestMap.get("/workitems/" + taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().UUID.getKey()).getValue() + "/relationships/links");
		taskData.getRoot().deepAddCopy(links);
		return links;
	}

	@Override
	public TaskAttribute getTaskAssignee(IOperationMonitor monitor, CommonHttpClient client, String id,
			TaskData taskData) throws OSIORestException {
		TaskAttribute assignee = (TaskAttribute)requestMap.get("/users/" + id);
		TaskAttribute originalAttr = taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().ASSIGNEES.getKey());
		originalAttr.addValue(assignee.getValue());
		return assignee;
	}

	@Override
	public Identity getAuthUser(IOperationMonitor monitor, CommonHttpClient client) throws OSIORestException {
		return (Identity)requestMap.get("/user");
	}

}
