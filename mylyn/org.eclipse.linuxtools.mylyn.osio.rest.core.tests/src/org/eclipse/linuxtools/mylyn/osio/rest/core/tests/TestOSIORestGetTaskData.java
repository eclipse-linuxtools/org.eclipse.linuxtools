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

import java.io.FileReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.IOSIORestConstants;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.NullOperationMonitor;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestClient;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConfiguration;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConnector;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestGetTaskData;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestTaskSchema;
import org.eclipse.linuxtools.mylyn.osio.rest.test.support.OSIOTestRestRequestProvider;
import org.eclipse.linuxtools.mylyn.osio.rest.test.support.TestData;
import org.eclipse.linuxtools.mylyn.osio.rest.test.support.TestUtils;
import org.eclipse.mylyn.commons.repositories.core.RepositoryLocation;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class TestOSIORestGetTaskData {
	
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

	private String getDate (String date) throws ParseException {
		SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", //$NON-NLS-1$
				Locale.US);
		iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
		java.util.Date tempDate = iso8601Format.parse(date);
		return Long.toString(tempDate.getTime());
	}

	@Test
	public void testGetTaskData() throws Exception {
		TestData testData = new TestData();
		TestUtils.initSpaces(requestProvider, testData);
		OSIORestClient client = connector.getClient(repository, requestProvider);
		OSIORestConfiguration config = client.getConfiguration(repository, new NullOperationMonitor());
		config.setSpaces(testData.spaceMap);
		connector.setConfiguration(config);
		RepositoryLocation location = client.getClient().getLocation();
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_ID, "user");
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN, "xxxxxxTokenxxxxxx");

		OSIORestGetTaskData data = new OSIORestGetTaskData(client.getClient(), connector, "/workitems/QUERY", repository);
		
		String bundleLocation = Activator.getContext().getBundle().getLocation();
		int index = bundleLocation.indexOf('/');
		String fileName = bundleLocation.substring(index) + "/testjson/workitems.data";
		FileReader in = new FileReader(fileName);
		List<TaskData> taskDataList = data.testParseFromJson(in);
		
		assertEquals(2, taskDataList.size());
		
		TaskData taskData = taskDataList.get(0);
		
		TaskAttribute root = taskData.getRoot();
		OSIORestTaskSchema schema = OSIORestTaskSchema.getDefault();
		
		TaskAttribute space = root.getAttribute(schema.SPACE.getKey());
		assertEquals("mywork", space.getValue());
		
		TaskAttribute spaceid = root.getAttribute(schema.SPACE_ID.getKey());
		assertEquals("SPACE-0001", spaceid.getValue());
		
		TaskAttribute uuid = root.getAttribute(schema.UUID.getKey());
		assertEquals("WORKITEM-0001", uuid.getValue());
		
		TaskAttribute labelsLink = root.getAttribute(schema.LABELS_LINK.getKey());
		assertEquals("https://openshift.io/api/workitems/WORKITEM-0001/labels", labelsLink.getValue());
		
		TaskAttribute creatorId = root.getAttribute(schema.CREATOR_ID.getKey());
		assertEquals("USER-0001", creatorId.getValue());
		
		TaskAttribute taskUrl = root.getAttribute(schema.TASK_URL.getKey());
		assertEquals("https://openshift.io/api/workitems/WORKITEM-0001", taskUrl.getValue());
	
		TaskAttribute assigneeIds = root.getAttribute(schema.ASSIGNEE_IDS.getKey());
		assertEquals("USER-0001", assigneeIds.getValue());
		
		TaskAttribute id = root.getAttribute(schema.ID.getKey());
		assertEquals("user/mywork#1", id.getValue());
		
		TaskAttribute status = root.getAttribute(schema.STATUS.getKey());
		assertEquals("resolved", status.getValue());
		
		TaskAttribute title = root.getAttribute(schema.SUMMARY.getKey());
		assertEquals("00001", title.getValue());
		
		TaskAttribute description = root.getAttribute(schema.DESCRIPTION.getKey());
		assertEquals("Test bug", description.getValue());
		
		TaskAttribute version = root.getAttribute(schema.VERSION.getKey());
		assertEquals("11", version.getValue());
		
		TaskAttribute order = root.getAttribute(schema.ORDER.getKey());
		assertEquals("1000", order.getValue());
		
		TaskAttribute created_at = root.getAttribute(schema.DATE_CREATION.getKey());
		assertEquals(getDate("2017-08-14T21:37:15.863435Z"), created_at.getValue());
		
		TaskAttribute updated_at = root.getAttribute(schema.DATE_MODIFICATION.getKey());
		assertEquals(getDate("2017-09-15T15:54:43.08915Z"), updated_at.getValue());
		
		taskData = taskDataList.get(1);
		root = taskData.getRoot();
		
		space = root.getAttribute(schema.SPACE.getKey());
		assertEquals("mywork", space.getValue());
		
		spaceid = root.getAttribute(schema.SPACE_ID.getKey());
		assertEquals("SPACE-0001", spaceid.getValue());
		
		uuid = root.getAttribute(schema.UUID.getKey());
		assertEquals("WORKITEM-0002", uuid.getValue());
		
		labelsLink = root.getAttribute(schema.LABELS_LINK.getKey());
		assertEquals("https://openshift.io/api/workitems/WORKITEM-0002/labels", labelsLink.getValue());
		
		creatorId = root.getAttribute(schema.CREATOR_ID.getKey());
		assertEquals("USER-0001", creatorId.getValue());
		
		taskUrl = root.getAttribute(schema.TASK_URL.getKey());
		assertEquals("https://openshift.io/api/workitems/WORKITEM-0002", taskUrl.getValue());
	
		assigneeIds = root.getAttribute(schema.ASSIGNEE_IDS.getKey());
		assertEquals("USER-0001", assigneeIds.getValue());
		
		id = root.getAttribute(schema.ID.getKey());
		assertEquals("user/mywork#2", id.getValue());
		
		status = root.getAttribute(schema.STATUS.getKey());
		assertEquals("open", status.getValue());
		
		title = root.getAttribute(schema.SUMMARY.getKey());
		assertEquals("00002", title.getValue());
		
		description = root.getAttribute(schema.DESCRIPTION.getKey());
		assertEquals("Test feature", description.getValue());
		
		version = root.getAttribute(schema.VERSION.getKey());
		assertEquals("22", version.getValue());
		
		order = root.getAttribute(schema.ORDER.getKey());
		assertEquals("2000", order.getValue());
		
		created_at = root.getAttribute(schema.DATE_CREATION.getKey());
		assertEquals(getDate("2017-08-15T21:37:15.863435Z"), created_at.getValue());
		
		updated_at = root.getAttribute(schema.DATE_MODIFICATION.getKey());
		assertEquals(getDate("2017-09-16T15:54:43.08915Z"), updated_at.getValue());
	}
}
