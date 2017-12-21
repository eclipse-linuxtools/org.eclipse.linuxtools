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
package org.eclipse.linuxtools.mylyn.osio.rest.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileReader;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.IOSIORestConstants;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.NullOperationMonitor;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestClient;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConfiguration;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConnector;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestGetTaskLinks;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestTaskSchema;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItem;
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

@SuppressWarnings("restriction")
public class TestOSIORestGetTaskLinks {
	
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
	
	private WorkItem initWorkItem(String number, String id, String spaceId, String title) throws Exception {
		WorkItem workitem = new WorkItem();
		workitem.setNumber(number);
		workitem.setId(id);
		workitem.setSpaceId(spaceId);
		workitem.setTitle(title);
		return workitem;
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
	public void testGetTaskLinks() throws Exception {
		TestData testData = new TestData();
		TestUtils.initSpaces(requestProvider, testData);
		OSIORestClient client = connector.getClient(repository, requestProvider);
		AbstractTaskDataHandler taskDataHandler = connector.getTaskDataHandler();
		TaskAttributeMapper mapper = taskDataHandler.getAttributeMapper(repository);
		TaskData taskData = new TaskData(mapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
		OSIORestTaskSchema.getDefault().initialize(taskData);
		OSIORestConfiguration config = client.getConfiguration(repository, new NullOperationMonitor());
		config.setSpaces(testData.spaceMap);
		connector.setConfiguration(config);
		RepositoryLocation location = client.getClient().getLocation();
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_ID, "user");
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN, "xxxxxxTokenxxxxxx");
		WorkItem workitem = initWorkItem("1", "WORKITEM-0001", "SPACE-0001", "Task 1");
		WorkItem workitem2 = initWorkItem("2", "WORKITEM-0002", "SPACE-0001", "Task 2");
		WorkItem workitem3 = initWorkItem("3", "WORKITEM-0003", "SPACE-0001", "Task 3");
		requestProvider.addGetRequest("/workitems/WORKITEM-0001", workitem);
		requestProvider.addGetRequest("/workitems/WORKITEM-0002", workitem2);
		requestProvider.addGetRequest("/workitems/WORKITEM-0003", workitem3);
		taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().UUID.getKey()).setValue("WORKITEM-0001");
		
		OSIORestGetTaskLinks data = new OSIORestGetTaskLinks(client.getClient(), client, testData.spaceMap.get("mywork"), taskData, config);
		
		String bundleLocation = Activator.getContext().getBundle().getLocation();
		int index = bundleLocation.indexOf('/');
		String fileName = bundleLocation.substring(index) + "/testjson/links.data";
		FileReader in = new FileReader(fileName);
		TaskAttribute original = taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().LINKS.getKey());
		assertTrue(original == null || original.getValues().isEmpty());
		TaskAttribute attribute = data.testParseFromJson(in);
		assertEquals(2, attribute.getValues().size());
		
		List<String> values = attribute.getValues();
		assertEquals("blocks Task 2 [user/mywork#2]", values.get(0));
		assertEquals("is blocked by Task 3 [user/mywork#3]", values.get(1));
	}

}
