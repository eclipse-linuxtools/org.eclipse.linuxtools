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
package org.eclipse.linuxtools.mylyn.osio.rest.core.tests;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.IOSIORestConstants;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.NullOperationMonitor;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestClient;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConfiguration;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConnector;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestPatchUpdateTask;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestTaskSchema;
import org.eclipse.linuxtools.mylyn.osio.rest.test.support.OSIOTestRestRequestProvider;
import org.eclipse.linuxtools.mylyn.osio.rest.test.support.TestData;
import org.eclipse.linuxtools.mylyn.osio.rest.test.support.TestUtils;
import org.eclipse.mylyn.commons.repositories.core.RepositoryLocation;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.stream.JsonWriter;

@SuppressWarnings("restriction")
public class TestOSIORestPatchUpdateTask {
	
	private TestOSIORestConnector connector;

	private TaskRepository repository;

	private OSIOTestRestRequestProvider requestProvider;
	
	private class TestOSIORestConnector extends OSIORestConnector {
		
		private OSIORestConfiguration config;
		
		public void setConfiguration (OSIORestConfiguration config) {
			this.config = config;
		}
		
		@Override
		public OSIORestConfiguration getRepositoryConfiguration(TaskRepository repository) throws CoreException {
			return config;
		}
	}
	
	@Before
	public void setUp() {
		connector = new TestOSIORestConnector();
		repository = new TaskRepository(connector.getConnectorKind(), "http://openshift.io/api");
		repository.setProperty(IOSIORestConstants.REPOSITORY_AUTH_ID, "user");
		repository.setProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN, "xxxxxxTokenxxxxxx");
		requestProvider = new OSIOTestRestRequestProvider();
	}

	@Test
	public void testPatchUpdateTask() throws Exception {
		TestData testData = new TestData();
		TestUtils.initSpaces(requestProvider, testData);
		OSIORestClient client = connector.getClient(repository, requestProvider);
		OSIORestConfiguration config = client.getConfiguration(repository, new NullOperationMonitor());
		config.setSpaces(testData.spaceMap);
		connector.setConfiguration(config);
		RepositoryLocation location = client.getClient().getLocation();
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_ID, "user");
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN, "xxxxxxTokenxxxxxx");
		
		AbstractTaskDataHandler taskDataHandler = connector.getTaskDataHandler();
		TaskAttributeMapper mapper = taskDataHandler.getAttributeMapper(repository);
		TaskData taskData = new TaskData(mapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
		OSIORestTaskSchema.getDefault().initialize(taskData);

		Set<TaskAttribute> attributes = new LinkedHashSet<>();
		TaskAttribute newComment = taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().NEW_COMMENT.getKey());
		newComment.setValue("This is a test comment");
		attributes.add(newComment);
		
		OSIORestTaskSchema taskSchema = OSIORestTaskSchema.getDefault();
		TaskAttribute root = taskData.getRoot();
		
		TaskAttribute description = root.getAttribute(taskSchema.DESCRIPTION.getKey());
		description.setValue("This is a test bug");
		TaskAttribute operation = root.createAttribute(TaskAttribute.OPERATION);
		operation.setValue("in progress");
		TaskAttribute status = root.getAttribute(taskSchema.STATUS.getKey());
		status.setValue("open");
		TaskAttribute uuid = root.getAttribute(taskSchema.UUID.getKey());
		uuid.setValue("WORKITEM-0001");
		TaskAttribute summary = root.getAttribute(taskSchema.SUMMARY.getKey());
		summary.setValue("Bug0001");
		TaskAttribute version = root.getAttribute(taskSchema.VERSION.getKey());
		version.setValue("11");
		TaskAttribute area = root.getAttribute(taskSchema.AREA.getKey());
		area.setValue("mywork");
		TaskAttribute iteration = root.getAttribute(taskSchema.ITERATION.getKey());
		iteration.setValue("mywork");
		TaskAttribute labels = root.getAttribute(taskSchema.LABELS.getKey());
		labels.addValue("label1");
		labels.addValue("label3");
		TaskAttribute addLabels = root.getAttribute(taskSchema.ADD_LABEL.getKey());
		addLabels.addValue("label2");
		TaskAttribute removeLabels = root.getAttribute(taskSchema.REMOVE_LABEL.getKey());
		removeLabels.addValue("label3");
		TaskAttribute assignees = root.getAttribute(taskSchema.ASSIGNEES.getKey());
		assignees.addValue("user3");
		TaskAttribute addAssignees = root.getAttribute(taskSchema.ADD_ASSIGNEE.getKey());
		addAssignees.addValue("user");
		TaskAttribute removeAssignees = root.getAttribute(taskSchema.REMOVE_ASSIGNEE.getKey());
		removeAssignees.addValue("user3");
		
		OSIORestPatchUpdateTask data = new OSIORestPatchUpdateTask(client.getClient(), taskData, attributes, testData.spaceMap.get("mywork"));
		
		OSIORestPatchUpdateTask.TaskAttributeTypeAdapter adapter = data.new TaskAttributeTypeAdapter(location);
		OSIORestPatchUpdateTask.OldAttributes oldAttributes = data.new OldAttributes(attributes);
		StringWriter s = new StringWriter();
		JsonWriter writer = new JsonWriter(s);
		
		adapter.write(writer, oldAttributes);
		
		assertEquals("{\"data\":{\"attributes\":{\"system.description\":\"This is a test bug\",\"system.state\":\"in progress\",\"system.title\":\"Bug0001\",\"version\":\"11\"}," + 
				"\"id\":\"WORKITEM-0001\",\"relationships\":{\"space\":{\"data\":{\"id\":\"SPACE-0001\",\"type\":\"spaces\"}},\"area\":{\"data\":{\"id\":\"AREA-0001\",\"type\":\"areas\"}}," + 
				"\"iteration\":{\"data\":{\"id\":\"ITERATION-0001\",\"type\":\"iterations\"}},\"assignees\":{\"data\":[{\"id\":\"USER-0001\",\"type\":\"users\"}]}," + 
				"\"labels\":{\"data\":[{\"id\":\"LABEL-0001\",\"type\":\"labels\"},{\"id\":\"LABEL-0002\",\"type\":\"labels\"}]}},\"type\":\"workitems\"},\"included\":[true]}",
				s.getBuffer().toString());
		
		StringWriter s2 = new StringWriter();
		JsonWriter writer2 = new JsonWriter(s2);

		root.removeAttribute(TaskAttribute.OPERATION);
		
		adapter.write(writer2, oldAttributes);
		
		assertEquals("{\"data\":{\"attributes\":{\"system.description\":\"This is a test bug\",\"system.state\":\"open\",\"system.title\":\"Bug0001\",\"version\":\"11\"}," + 
				"\"id\":\"WORKITEM-0001\",\"relationships\":{\"space\":{\"data\":{\"id\":\"SPACE-0001\",\"type\":\"spaces\"}},\"area\":{\"data\":{\"id\":\"AREA-0001\",\"type\":\"areas\"}}," + 
				"\"iteration\":{\"data\":{\"id\":\"ITERATION-0001\",\"type\":\"iterations\"}},\"assignees\":{\"data\":[{\"id\":\"USER-0001\",\"type\":\"users\"}]}," + 
				"\"labels\":{\"data\":[{\"id\":\"LABEL-0001\",\"type\":\"labels\"},{\"id\":\"LABEL-0002\",\"type\":\"labels\"}]}},\"type\":\"workitems\"},\"included\":[true]}",
				s2.getBuffer().toString());
	}

}
