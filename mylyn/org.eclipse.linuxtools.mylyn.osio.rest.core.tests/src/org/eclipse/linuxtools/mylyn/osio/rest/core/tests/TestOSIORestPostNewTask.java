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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.IOSIORestConstants;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.NullOperationMonitor;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestClient;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConfiguration;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConnector;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestPostNewTask;
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
public class TestOSIORestPostNewTask {
	
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
	public void testPostNewTask() throws Exception {
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
		
		TaskAttribute root = taskData.getRoot();
		OSIORestTaskSchema taskSchema = OSIORestTaskSchema.getDefault();
		
		TaskAttribute summary = root.getAttribute(taskSchema.SUMMARY.getKey());
		summary.setValue("Bug0001");
		TaskAttribute description = root.getAttribute(taskSchema.DESCRIPTION.getKey());
		description.setValue("This is a bug");
		TaskAttribute workitemtype = root.getAttribute(taskSchema.WORKITEM_TYPE.getKey());
		workitemtype.setValue("bug");

		OSIORestPostNewTask data = new OSIORestPostNewTask(client.getClient(), taskData, testData.spaceMap.get("mywork"), connector, repository);
		
		OSIORestPostNewTask.TaskAttributeTypeAdapter adapter = data.new TaskAttributeTypeAdapter(location);
		StringWriter s = new StringWriter();
		JsonWriter writer = new JsonWriter(s);
		
		adapter.write(writer, taskData);
		
		assertEquals("{\"data\":{\"attributes\":{\"system.state\":\"new\",\"system.title\":\"Bug0001\",\"version\":\"1\",\"system.description\":\"This is a bug\"},\"relationships\":{\"baseType\":{\"data\":{\"id\":\"WORKITEMTYPE-0001\",\"type\":\"workitemtypes\"}},\"space\":{\"data\":{\"id\":\"SPACE-0001\",\"type\":\"spaces\"}}},\"type\":\"workitems\"},\"included\":[true]}", s.toString());
	}

}
