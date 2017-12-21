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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.TimeZone;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.IOSIORestConstants;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.NullOperationMonitor;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestClient;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConfiguration;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConnector;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestGetTaskComments;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestTaskSchema;
import org.eclipse.linuxtools.mylyn.osio.rest.test.support.OSIOTestRestRequestProvider;
import org.eclipse.linuxtools.mylyn.osio.rest.test.support.TestData;
import org.eclipse.linuxtools.mylyn.osio.rest.test.support.TestUtils;
import org.eclipse.mylyn.commons.repositories.core.RepositoryLocation;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("restriction")
public class TestOSIORestGetTaskComments {
	
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

	private java.util.Date getDate (String date) throws ParseException {
		SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", //$NON-NLS-1$
				Locale.US);
		iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
		java.util.Date tempDate = iso8601Format.parse(date);
		return tempDate;
	}


	@Test
	public void testGetTaskComments() throws Exception {
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

		OSIORestGetTaskComments data = new OSIORestGetTaskComments(client.getClient(), testData.spaceMap.get("mywork"), taskData);
		
		String bundleLocation = Activator.getContext().getBundle().getLocation();
		int index = bundleLocation.indexOf('/');
		String fileName = bundleLocation.substring(index) + "/testjson/comments.data";
		FileReader in = new FileReader(fileName);
		TaskAttribute original = taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().ASSIGNEES.getKey());
		assertTrue(original == null || original.getValues().isEmpty());
		ArrayList<TaskAttribute> attributes = data.testParseFromJson(in);
		assertEquals(4, attributes.size());
		TaskAttribute attr1 = attributes.get(0);
		
		TaskCommentMapper commentMapper = TaskCommentMapper.createFrom(attr1);
		assertEquals("COMMENT-0004", commentMapper.getCommentId());
		assertEquals("This is the fourth comment.", commentMapper.getText());
		assertEquals(getDate("2017-11-21T20:46:47.383745Z"), commentMapper.getCreationDate());
		IRepositoryPerson person = commentMapper.getAuthor();
		assertEquals("User", person.getName());
		assertEquals("user@user.org", person.getPersonId());
		assertEquals(new Integer(4), commentMapper.getNumber());
		
		TaskAttribute attr2 = attributes.get(1);
		commentMapper = TaskCommentMapper.createFrom(attr2);
		assertEquals("COMMENT-0003", commentMapper.getCommentId());
		assertEquals("This is the third comment.", commentMapper.getText());
		assertEquals(getDate("2017-10-05T23:01:54.15784Z"), commentMapper.getCreationDate());
		IRepositoryPerson person2 = commentMapper.getAuthor();
		assertEquals("User", person2.getName());
		assertEquals("user@user.org", person2.getPersonId());
		assertEquals(new Integer(3), commentMapper.getNumber());
		
		TaskAttribute attr3 = attributes.get(2);
		commentMapper = TaskCommentMapper.createFrom(attr3);
		assertEquals("COMMENT-0002", commentMapper.getCommentId());
		assertEquals("This is the second comment.", commentMapper.getText());
		assertEquals(getDate("2017-10-05T21:09:19.673546Z"), commentMapper.getCreationDate());
		IRepositoryPerson person3 = commentMapper.getAuthor();
		assertEquals("User", person3.getName());
		assertEquals("user@user.org", person3.getPersonId());
		assertEquals(new Integer(2), commentMapper.getNumber());
		
		TaskAttribute attr4 = attributes.get(3);
		commentMapper = TaskCommentMapper.createFrom(attr4);
		assertEquals("COMMENT-0001", commentMapper.getCommentId());
		assertEquals("Comment on Task 1.", commentMapper.getText());
		assertEquals(getDate("2017-08-14T21:40:29.434379Z"), commentMapper.getCreationDate());
		IRepositoryPerson person4 = commentMapper.getAuthor();
		assertEquals("User", person4.getName());
		assertEquals("user@user.org", person4.getPersonId());
		assertEquals(new Integer(1), commentMapper.getNumber());
	}

}
