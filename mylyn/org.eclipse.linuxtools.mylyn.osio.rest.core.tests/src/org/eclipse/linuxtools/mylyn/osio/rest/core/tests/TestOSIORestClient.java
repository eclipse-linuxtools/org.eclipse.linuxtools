/*******************************************************************************
 * Copyright (c) 2014 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.mylyn.osio.rest.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.IOSIORestConstants;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestClient;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConfiguration;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConnector;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestTaskSchema;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.URLQueryEncoder;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Identity;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Space;
import org.eclipse.linuxtools.mylyn.osio.rest.test.support.OSIOTestRestRequestProvider;
import org.eclipse.linuxtools.mylyn.osio.rest.test.support.TestData;
import org.eclipse.linuxtools.mylyn.osio.rest.test.support.TestUtils;
import org.eclipse.mylyn.commons.repositories.core.RepositoryLocation;
import org.eclipse.mylyn.internal.commons.core.operations.NullOperationMonitor;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings({ "restriction", "unused" })
public class TestOSIORestClient {
	private OSIORestConnector connector;

	private TaskRepository repository;
	
	private OSIOTestRestRequestProvider requestProvider;
	
	private TaskData task1;
	private TaskData task2;
	
	@SuppressWarnings("deprecation")
	private void initWorkItems(TaskData task1, TaskData task2) throws Exception {
		
		OSIORestClient client = connector.getClient(repository, requestProvider);
		AbstractTaskDataHandler taskDataHandler = connector.getTaskDataHandler();
		TaskAttributeMapper mapper = taskDataHandler.getAttributeMapper(repository);
		TaskData taskData = task1;
		OSIORestTaskSchema.getDefault().initialize(taskData);
		taskDataHandler.initializeTaskData(repository, taskData, null, null);
		
		OSIORestTaskSchema schema = OSIORestTaskSchema.getDefault();
		TaskAttribute root = taskData.getRoot();
		
		root.createAttribute(schema.UUID.getKey()).addValue("WORKITEM-0001");
		root.createAttribute(schema.SPACE.getKey()).addValue("mywork");
		root.createAttribute(schema.SPACE_ID.getKey()).addValue("SPACE-0001");
		root.createAttribute(schema.AREA.getKey()).addValue("otherarea");
		root.createAttribute(schema.ITERATION.getKey()).addValue("mywork");
		root.createAttribute(schema.DESCRIPTION.getKey()).addValue("This is Bug 1");
		root.createAttribute(schema.NUMBER.getKey()).addValue("1");
		root.createAttribute(schema.ORDER.getKey()).addValue("1000");
		root.createAttribute(schema.STATUS.getKey()).addValue("in progress");
		root.createAttribute(schema.SUMMARY.getKey()).addValue("Bug 0001");
		root.createAttribute(schema.TASK_URL.getKey()).addValue("https://api.openshift.io/api/workitems/WORKITEM-0001");
		root.createAttribute(schema.WORKITEM_TYPE.getKey()).addValue("bug");
		root.createAttribute(schema.ID.getKey()).addValue("user/mywork#1");
		root.createAttribute(schema.ASSIGNEE_IDS.getKey()).addValue("USER-0001");
		root.createAttribute(schema.CREATOR_ID.getKey()).addValue("USER-0001");
		
		TaskAttribute attribute = taskData.getRoot()
				.createAttribute(TaskAttribute.PREFIX_COMMENT + "0");
		TaskAttribute numComments = taskData.getRoot().createAttribute(OSIORestTaskSchema.getDefault().COMMENT_COUNT.getKey());
		numComments.setValue("1");
		TaskCommentMapper taskComment = TaskCommentMapper.createFrom(attribute);
		taskComment.setCommentId("COMMENT-0001");
		taskComment.setNumber(1);
		taskComment.setUrl("https://api.openshift.io/api/workitems/comments/COMMENT-0001");
		String email = "user@user.org"; //$NON-NLS-1$
		String fullName = "User 1"; //$NON-NLS-1$
		IRepositoryPerson author = taskData.getAttributeMapper()
				.getTaskRepository()
				.createPerson(email); //$NON-NLS-1$
		author.setName(fullName);
		taskComment.setAuthor(author);
		taskComment.setIsPrivate(null);
		taskComment.setCreationDate(new Date(2017, 01, 01));
		taskComment.setText("This is comment 1");
		taskComment.applyTo(attribute);
		TaskAttribute attribute2 = taskData.getRoot()
				.createAttribute(TaskAttribute.PREFIX_COMMENT + "1");
		taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().COMMENT_COUNT.getKey());
		numComments.setValue("2");
		taskComment = TaskCommentMapper.createFrom(attribute2);
		taskComment.setCommentId("COMMENT-0002");
		taskComment.setNumber(2);
		taskComment.setUrl("https://api.openshift.io/api/workitems/comments/COMMENT-0002");
		email = "user2@user.org"; //$NON-NLS-1$
		fullName = "User 2"; //$NON-NLS-1$
		author = taskData.getAttributeMapper()
				.getTaskRepository()
				.createPerson(email); //$NON-NLS-1$
		author.setName(fullName);
		taskComment.setAuthor(author);
		taskComment.setIsPrivate(null);
		taskComment.setCreationDate(new Date(2017, 01, 02));
		taskComment.setText("This is comment 2");
		taskComment.applyTo(attribute2);
		
		List<TaskAttribute> comments = new ArrayList<>();
		comments.add(attribute);
		comments.add(attribute2);
		
		requestProvider.addGetRequest("/workitems/WORKITEM-0001/comments", comments);
		
		TaskAttribute attribute5 = taskData.getRoot().createAttribute(OSIORestTaskSchema.getDefault().LABELS.getKey());
		attribute5.addValue("LabelA");
		attribute5.addValue("LabelB");
		
		requestProvider.addGetRequest("/workitems/WORKITEM-0001/labels", attribute5);
		
		TaskAttribute attribute6 = taskData.getRoot().createAttribute(OSIORestTaskSchema.getDefault().LINKS.getKey());
		attribute6.addValue("blocks Bug 3 [user/mywork#3]");
		
		requestProvider.addGetRequest("/workitems/WORKITEM-0001/relationships/links", attribute6);
		
		TaskAttribute creator = taskData.getRoot().createAttribute(OSIORestTaskSchema.getDefault().CREATOR.getKey());
		TaskAttributeMetaData creatorMeta = creator.getMetaData();
		creator.setValue("User 1");
		creatorMeta.putValue("email", "user@user.org");
		creatorMeta.putValue("username", "user");
		creatorMeta.putValue("id", "USER-0001");
		
		requestProvider.addGetRequest("/users/USER-0001", creator);


		TaskData taskData2 = task2;
		OSIORestTaskSchema.getDefault().initialize(taskData2);
		taskDataHandler.initializeTaskData(repository, taskData2, null, null);
		root = taskData2.getRoot();
		
		root.createAttribute(schema.UUID.getKey()).addValue("WORKITEM-0002");
		root.createAttribute(schema.SPACE.getKey()).addValue("mywork");
		root.createAttribute(schema.SPACE_ID.getKey()).addValue("SPACE-0003");
		root.createAttribute(schema.AREA.getKey()).addValue("mywork");
		root.createAttribute(schema.ITERATION.getKey()).addValue("mywork");
		root.createAttribute(schema.DESCRIPTION.getKey()).addValue("This is Feature 1");
		root.createAttribute(schema.NUMBER.getKey()).addValue("1");
		root.createAttribute(schema.ORDER.getKey()).addValue("1010");
		root.createAttribute(schema.STATUS.getKey()).addValue("open");
		root.createAttribute(schema.SUMMARY.getKey()).addValue("Feature 0001");
		root.createAttribute(schema.TASK_URL.getKey()).addValue("https://api.openshift.io/api/workitems/WORKITEM-0002");
		root.createAttribute(schema.WORKITEM_TYPE.getKey()).addValue("bug");
		root.createAttribute(schema.ID.getKey()).addValue("user3/mywork#1");
		root.createAttribute(schema.ASSIGNEE_IDS.getKey()).addValue("USER-0001");
		root.createAttribute(schema.CREATOR_ID.getKey()).addValue("USER-0003");
		
		TaskAttribute attribute3 = taskData.getRoot()
				.createAttribute(TaskAttribute.PREFIX_COMMENT + "0");
		TaskAttribute numComments3 = taskData.getRoot().createAttribute(OSIORestTaskSchema.getDefault().COMMENT_COUNT.getKey());
		numComments.setValue("1");
		taskComment = TaskCommentMapper.createFrom(attribute);
		taskComment.setCommentId("COMMENT-0003");
		taskComment.setNumber(1);
		taskComment.setUrl("https://api.openshift.io/api/workitems/comments/COMMENT-0003");
		String email3 = "user@user.org"; //$NON-NLS-1$
		String fullName3 = "User 1"; //$NON-NLS-1$
		IRepositoryPerson author3 = taskData.getAttributeMapper()
				.getTaskRepository()
				.createPerson(email3); //$NON-NLS-1$
		author3.setName(fullName3);
		taskComment.setAuthor(author3);
		taskComment.setIsPrivate(null);
		taskComment.setCreationDate(new Date(2017, 01, 01));
		taskComment.setText("This is first comment");
		taskComment.applyTo(attribute3);
		
		List<TaskAttribute> comments2 = new ArrayList<>();
		comments2.add(attribute3);
		
		requestProvider.addGetRequest("/workitems/WORKITEM-0002/comments", comments2);
		
		TaskAttribute attribute4 = taskData.getRoot().createAttribute(OSIORestTaskSchema.getDefault().LABELS.getKey());
		attribute4.addValue("Label1");
		attribute4.addValue("Label2");
		
		requestProvider.addGetRequest("/workitems/WORKITEM-0002/labels", attribute4);
		
		TaskAttribute attribute7 = taskData.getRoot().createAttribute(OSIORestTaskSchema.getDefault().LINKS.getKey());
		attribute7.clearValues();
		
		requestProvider.addGetRequest("/workitems/WORKITEM-0002/relationships/links", attribute7);
		
		TaskAttribute creator2 = taskData.getRoot().createAttribute(OSIORestTaskSchema.getDefault().CREATOR.getKey());
		TaskAttributeMetaData creatorMeta2 = creator.getMetaData();
		creator2.setValue("User 3");
		creatorMeta2.putValue("email", "user3@user.org");
		creatorMeta2.putValue("username", "user3");
		creatorMeta2.putValue("id", "USER-0003");
		
		requestProvider.addGetRequest("/users/USER-0003", creator2);

	}

	@SuppressWarnings("deprecation")
	@Before
	public void setUp() {
		connector = new OSIORestConnector();
		repository = new TaskRepository(connector.getConnectorKind(), "http://openshift.io/api");
		repository.setProperty(IOSIORestConstants.REPOSITORY_AUTH_ID, "user");
		repository.setProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN, "xxxxxxTokenxxxxxx");
		requestProvider = new OSIOTestRestRequestProvider();
		requestProvider.addGetRequest("/user",
				new Identity("user", "user", new Date(2017, 01, 01), new Date(2017, 01, 01), "User", "//image/user",
						"user", Boolean.TRUE, "user@user.org", "userCo", "", "//users/user", "TestUser"));
	}

	@Test
	public void testConnectorClientCache() throws Exception {
		OSIORestClient client1 = connector.getClient(repository);
		assertNotNull(client1);
	}

	@Test
	public void testValidate() throws Exception {
		OSIORestClient client = connector.getClient(repository, requestProvider);
		RepositoryLocation location = client.getClient().getLocation();
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_ID, "user");
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN, "xxxxxxTokenxxxxxx");
		assertNotNull(client.getClient());
		assertTrue(client.validate(new NullOperationMonitor()));
	}


	@Test
	public void testGetConfiguration() throws Exception {
		TestData testData = new TestData();
		TestUtils.initSpaces(requestProvider, testData);
		requestProvider.addGetRequest("/namedspaces/user", testData.spaces);
		OSIORestClient client = connector.getClient(repository, requestProvider);
		RepositoryLocation location = client.getClient().getLocation();
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_ID, "user");
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN, "xxxxxxTokenxxxxxx");
		OSIORestConfiguration configuration = client.getConfiguration(repository, new NullOperationMonitor());
		Map<String, Space> spaces = configuration.getSpaces();
		assertEquals(spaces.size(), 2);
		assertTrue(spaces.containsKey("mywork"));
		assertTrue(spaces.containsKey("mywork2"));
	}

	@Test
	public void testinitializeTaskData() throws Exception {
		final TaskMapping taskMappingInit = new TaskMapping() {
			@Override
			public String getSummary() {
				return "The Summary";
			}

			@Override
			public String getDescription() {
				return "The Description";
			}
		};
		TestData testData = new TestData();
		TestUtils.initSpaces(requestProvider, testData);
		OSIORestClient client = connector.getClient(repository, requestProvider);
		AbstractTaskDataHandler taskDataHandler = connector.getTaskDataHandler();
		TaskAttributeMapper mapper = taskDataHandler.getAttributeMapper(repository);
		TaskData taskData = new TaskData(mapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
		assertTrue(taskDataHandler.initializeTaskData(repository, taskData, null, null));
		TaskAttribute attribute = taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().SUMMARY.getKey());
		assertTrue(attribute != null);
		assertEquals(attribute.getValue(), "");
		taskData = new TaskData(mapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
		assertTrue(taskDataHandler.initializeTaskData(repository, taskData, taskMappingInit, null));
		attribute = taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().SUMMARY.getKey());
		assertEquals(attribute.getValue(), "The Summary");
		attribute = taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().DESCRIPTION.getKey());
		assertEquals(attribute.getValue(), "The Description");
	}
	
	public class TestTaskDataCollector extends TaskDataCollector {
		final List<TaskData> retrievedData = new ArrayList<>();
		
		@Override
		public void accept(TaskData taskData) {
			retrievedData.add(taskData);
		}
		
		public List<TaskData> getTaskData() {
			return retrievedData;
		}
		
	}
	
	public class RepositoryQuery implements IRepositoryQuery {
		
		private String url;
		
		public RepositoryQuery (String url) {
			this.url = url;
		}

		@Override
		public String getAttribute(String key) {
			// Auto-generated method stub
			return null;
		}

		@Override
		public void setAttribute(String key, String value) {
			// Auto-generated method stub
		}

		@Override
		public Map<String, String> getAttributes() {
			// Auto-generated method stub
			return null;
		}

		@Override
		public String getConnectorKind() {
			// Auto-generated method stub
			return null;
		}

		@Override
		public String getRepositoryUrl() {
			// Auto-generated method stub
			return null;
		}

		@Override
		public String getUrl() {
			return url;
		}

		@Override
		public void setUrl(String url) {
			this.url = url;
		}

		@Override
		public String getSummary() {
			// Auto-generated method stub
			return null;
		}

		@Override
		public void setSummary(String summary) {
			// Auto-generated method stub
		}
		
	}
	
	@Test
	public void testGetSpaceLinkTypes() throws Exception {
		TestData testData = new TestData();
		TestUtils.initSpaces(requestProvider, testData);
		requestProvider.addGetRequest("/namedspaces/user", testData.spaces);
		OSIORestClient client = connector.getClient(repository, requestProvider);
		RepositoryLocation location = client.getClient().getLocation();
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_ID, "user");
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN, "xxxxxxTokenxxxxxx");
		OSIORestConfiguration configuration = client.getConfiguration(repository, new NullOperationMonitor());
		configuration.getSpaces();
		Map<String, String> linktypes = client.getSpaceLinkTypes("SPACE-0001", repository);
		assertTrue(linktypes != null);
		assertTrue(!linktypes.isEmpty());
		assertEquals(linktypes.get("blocks"), "LINKTYPE-0001");
		assertEquals(linktypes.get("is blocked by"), "LINKTYPE-0001");
	}
	
	@Test
	public void testGetSpaceWorkItemsById() throws Exception {
		TestData testData = new TestData();
		TestUtils.initSpaces(requestProvider, testData);
		requestProvider.addGetRequest("/namedspaces/user", testData.spaces);
		OSIORestClient client = connector.getClient(repository, requestProvider);
		RepositoryLocation location = client.getClient().getLocation();
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_ID, "user");
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN, "xxxxxxTokenxxxxxx");
		OSIORestConfiguration configuration = connector.getRepositoryConfiguration(repository);
		configuration.getSpaces();
		Map<String, Space> externSpaces = configuration.getExternalSpaces();
		assertTrue(externSpaces != null);
		assertTrue(externSpaces.isEmpty());
		
		Space s1 = client.getSpaceById("SPACE-0001", repository);
		assertTrue(s1 != null);
		assertEquals(s1.getName(), "mywork");
		assertEquals(s1.getId(), "SPACE-0001");
		
		Space s2 = client.getSpaceById("SPACE-0003", repository);
		assertTrue(s2 != null);
		assertEquals(s2.getName(), "mywork");
		assertEquals(s2.getId(), "SPACE-0003");
		
		externSpaces = configuration.getExternalSpaces();
		assertTrue(externSpaces != null);
		assertEquals(externSpaces.get("SPACE-0003"), s2);
	}
	
	@Test
	public void testFormSearchURL() throws Exception {
		TestData testData = new TestData();
		TestUtils.initSpaces(requestProvider, testData);
		requestProvider.addGetRequest("/namedspaces/user", testData.spaces);
		OSIORestClient client = connector.getClient(repository, requestProvider);
		AbstractTaskDataHandler taskDataHandler = connector.getTaskDataHandler();
		TaskAttributeMapper mapper = taskDataHandler.getAttributeMapper(repository);
		TaskData taskData1 = new TaskData(mapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
		assertTrue(taskDataHandler.initializeTaskData(repository, taskData1, null, null));
		taskData1.getRoot().createAttribute(OSIORestTaskSchema.getDefault().SPACE_ID.getKey()).addValue("SPACE-0001");
		taskData1.getRoot().createAttribute(OSIORestTaskSchema.getDefault().WORKITEM_TYPE.getKey()).addValue("bug");
		TaskData taskData2 = new TaskData(mapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
		assertTrue(taskDataHandler.initializeTaskData(repository, taskData2, null, null));
		taskData2.getRoot().createAttribute(OSIORestTaskSchema.getDefault().SPACE_ID.getKey()).addValue("SPACE-0002");
		taskData2.getRoot().createAttribute(OSIORestTaskSchema.getDefault().WORKITEM_TYPE.getKey()).addValue("bug");
		List<TaskData>taskList = new ArrayList<>();
		taskList.add(taskData1);
		taskList.add(taskData2);
		String query = "filter[expression]={\"$AND\":[{\"$OR\":[{\"space\":\"SPACE-0001\"},{\"space\":\"SPACE-0002\"}]},{\"$OR\":[{\"workitemtype\":\"WORKITEMTYPE-0001\"}]}]}";
		String transformedQuery = URLQueryEncoder.transform(query);
        requestProvider.addGetRequest("/search?" + transformedQuery, taskList);
		RepositoryLocation location = client.getClient().getLocation();
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_ID, "user");
		location.setProperty(IOSIORestConstants.REPOSITORY_AUTH_TOKEN, "xxxxxxTokenxxxxxx");
		location.setUrl("https://api.openshift.io/api");
		OSIORestConfiguration configuration = client.getConfiguration(repository, new NullOperationMonitor());
		Map<String, Space> spaces = configuration.getSpaces();
		TestTaskDataCollector collector = new TestTaskDataCollector();
		RepositoryQuery repoQuery = new RepositoryQuery("https://api.openshift.io/api/query?space=mywork&space=mywork2&baseType=bug");
		IStatus status = client.performQuery(repository, repoQuery, collector, new NullOperationMonitor());
		assertTrue(status.isOK());
		List<TaskData> dataList = collector.getTaskData();
		assertTrue(dataList != null);
		assertEquals(dataList.size(), 2);
		assertEquals(dataList.get(0), taskData1);
		assertEquals(dataList.get(1), taskData2);
	}
	
	@Test
	public void testGetTaskData() throws Exception {
		TestData testData = new TestData();
		TestUtils.initSpaces(requestProvider, testData);
		requestProvider.addGetRequest("/namedspaces/user", testData.spaces);
		OSIORestClient client = connector.getClient(repository, requestProvider);
		AbstractTaskDataHandler taskDataHandler = connector.getTaskDataHandler();
		TaskAttributeMapper mapper = taskDataHandler.getAttributeMapper(repository);
		TaskData taskData = new TaskData(mapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
		TaskData task1 = new TaskData(mapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
		TaskData task2 = new TaskData(mapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");	
		initWorkItems(task1, task2);
		client.getConfiguration(repository, new NullOperationMonitor());
		requestProvider.addGetRequest("/workitems/WORKITEM-0001", task1);
		requestProvider.addRelocation("/namedspaces/user/mywork/workitems/1", "/spaces/SPACE-0001/workitem/WORKITEM-0001");
		requestProvider.addGetRequest("/workitems/WORKITEM-0002", task2);
		requestProvider.addRelocation("/namedspaces/user3/mywork/workitems/1", "/spaces/SPACE-0003/workitem/WORKITEM-0002");
		TestTaskDataCollector collector = new TestTaskDataCollector();
		Set<String> taskIds = new TreeSet<>();
		taskIds.add("user/mywork#1");
		taskIds.add("user3/mywork#1");
		client.getTaskData(taskIds, repository, collector, new NullOperationMonitor());
		List<TaskData> results = collector.getTaskData();
		assertTrue(results != null);
		assertEquals(2, results.size());
		TaskData data = results.get(0);
		assertEquals(data, task1);
		TaskAttribute comment0 = data.getRoot().getAttribute(TaskAttribute.PREFIX_COMMENT + "0");
		assertTrue(comment0 != null);
		TaskCommentMapper commentMapper = TaskCommentMapper.createFrom(comment0);
		assertEquals(commentMapper.getText(), "This is comment 1");
		TaskAttribute comment1 = data.getRoot().getAttribute(TaskAttribute.PREFIX_COMMENT + "1");
		assertTrue(comment1 != null);
		commentMapper = TaskCommentMapper.createFrom(comment1);
		assertEquals(commentMapper.getText(), "This is comment 2");
		data = results.get(1);
		assertEquals(data, task2);
		comment0 = data.getRoot().getAttribute(TaskAttribute.PREFIX_COMMENT + "0");
		assertTrue(comment0 != null);
		commentMapper = TaskCommentMapper.createFrom(comment0);
		assertEquals(commentMapper.getText(), "This is first comment");
	}
//
//	@Test
//	public void testGetTaskData() throws Exception {
//		final TaskMapping taskMappingInit = new TaskMapping() {
//			@Override
//			public String getSummary() {
//				return "The Summary";
//			}
//
//			@Override
//			public String getDescription() {
//				return "The Description";
//			}
//
//			@Override
//			public String getProduct() {
//				return "ManualTest";
//			}
//
//			@Override
//			public String getComponent() {
//				return "ManualC1";
//			}
//
//			@Override
//			public String getVersion() {
//				return "R1";
//			}
//		};
//		TaskData taskData = harness.createTaskData(taskMappingInit, null, null);
//
//		taskData.getRoot().getAttribute("cf_dropdown").setValue("one");
//		taskData.getRoot()
//				.getAttribute(OSIORestCreateTaskSchema.getDefault().TARGET_MILESTONE.getKey())
//				.setValue("M2");
//		String taskId = harness.submitNewTask(taskData);
//		TaskData taskDataGet = harness.getTaskFromServer(taskId);
//
//		// description is only for old tasks readonly and has the two sub attributes
//		// COMMENT_NUMBER and COMMENT_ISPRIVATE
//		TaskAttribute getDesc = taskDataGet.getRoot()
//				.getAttribute(OSIORestTaskSchema.getDefault().DESCRIPTION.getKey());
//		getDesc.getMetaData().setReadOnly(false);
//		getDesc.removeAttribute(TaskAttribute.COMMENT_ISPRIVATE);
//		getDesc.removeAttribute(TaskAttribute.COMMENT_NUMBER);
//
//		// resolution is only for new tasks readonly
//		taskData.getRoot()
//				.getAttribute(OSIORestTaskSchema.getDefault().RESOLUTION.getKey())
//				.getMetaData()
//				.setReadOnly(false);
//
//		// attributes we know that they can not be equal
//		taskData.getRoot().removeAttribute(OSIORestTaskSchema.getDefault().STATUS.getKey());
//		taskDataGet.getRoot().removeAttribute(OSIORestTaskSchema.getDefault().STATUS.getKey());
//		taskData.getRoot().removeAttribute(OSIORestTaskSchema.getDefault().ASSIGNED_TO.getKey());
//		taskDataGet.getRoot().removeAttribute(OSIORestTaskSchema.getDefault().ASSIGNED_TO.getKey());
//		taskData.getRoot().removeAttribute(TaskAttribute.OPERATION);
//		taskDataGet.getRoot().removeAttribute(TaskAttribute.OPERATION);
//		taskDataGet.getRoot().removeAttribute(OSIORestTaskSchema.getDefault().DATE_MODIFICATION.getKey());
//		taskData.getRoot().removeAttribute(OSIORestTaskSchema.getDefault().DATE_MODIFICATION.getKey());
//		// CC attribute has diverences in the meta data between create and update
//		taskData.getRoot().removeAttribute(TaskAttribute.USER_CC);
//		taskDataGet.getRoot().removeAttribute(TaskAttribute.USER_CC);
//
//		// attributes only in old tasks
//		taskData.getRoot().removeAttribute("description_is_private");
//
//		// attributes only in new tasks
//		taskDataGet.getRoot().removeAttribute("bug_id");
//		taskDataGet.getRoot().removeAttribute(TaskAttribute.COMMENT_NEW);
//		taskDataGet.getRoot().removeAttribute("addCC");
//		taskDataGet.getRoot().removeAttribute("removeCC");
//		taskDataGet.getRoot().removeAttribute(OSIORestTaskSchema.getDefault().RESET_QA_CONTACT.getKey());
//		taskDataGet.getRoot().removeAttribute(OSIORestTaskSchema.getDefault().RESET_ASSIGNED_TO.getKey());
//		taskDataGet.getRoot().removeAttribute(OSIORestTaskSchema.getDefault().ADD_SELF_CC.getKey());
//		ArrayList<TaskAttribute> flags = new ArrayList<>();
//		for (TaskAttribute attribute : taskDataGet.getRoot().getAttributes().values()) {
//			if (attribute.getId().startsWith(IOSIORestConstants.KIND_FLAG_TYPE)) {
//				flags.add(attribute);
//			}
//		}
//		for (TaskAttribute taskAttribute : flags) {
//			taskDataGet.getRoot().removeAttribute(taskAttribute.getId());
//		}
//
//		// attributes for operations
//		taskDataGet.getRoot().removeAttribute("task.common.operation-CONFIRMED");
//		taskDataGet.getRoot().removeAttribute("task.common.operation-IN_PROGRESS");
//		taskDataGet.getRoot().removeAttribute("task.common.operation-RESOLVED");
//		taskDataGet.getRoot().removeAttribute("resolutionInput");
//		taskDataGet.getRoot().removeAttribute("task.common.operation-duplicate");
//		taskDataGet.getRoot().removeAttribute(OSIORestTaskSchema.getDefault().DUPE_OF.getKey());
//
//		assertEquals(taskData.getRoot().toString(), taskDataGet.getRoot().toString());
//		assertEquals(
//				IOUtils.toString(
//						CommonTestUtil.getResource(this, actualFixture.getTestDataFolder() + "/taskDataFlags.txt")),
//				flags.toString());	}
//
//	@Test
//	public void testUpdateTaskData() throws Exception {
//		String taskId = harness.getNewTaksId4TestProduct();
//		TaskData taskDataGet = harness.getTaskFromServer(taskId);
//
//		Set<TaskAttribute> changed = new HashSet<TaskAttribute>();
//
//		TaskAttribute attribute = taskDataGet.getRoot()
//				.getMappedAttribute(OSIORestCreateTaskSchema.getDefault().PRODUCT.getKey());
//		attribute.setValue("Product with Spaces");
//		changed.add(attribute);
//		attribute = taskDataGet.getRoot()
//				.getMappedAttribute(OSIORestCreateTaskSchema.getDefault().COMPONENT.getKey());
//		attribute.setValue("Component 1");
//		changed.add(attribute);
//		attribute = taskDataGet.getRoot()
//				.getMappedAttribute(OSIORestCreateTaskSchema.getDefault().VERSION.getKey());
//		attribute.setValue("b");
//		changed.add(attribute);
//		attribute = taskDataGet.getRoot()
//				.getMappedAttribute(OSIORestCreateTaskSchema.getDefault().TARGET_MILESTONE.getKey());
//		attribute.setValue("M3.0");
//		changed.add(attribute);
//
//		attribute = taskDataGet.getRoot().getAttribute("cf_dropdown");
//		attribute.setValue("two");
//		changed.add(attribute);
//		attribute = taskDataGet.getRoot().getAttribute("cf_multiselect");
//		attribute.setValues(Arrays.asList("Red", "Yellow"));
//		changed.add(attribute);
//
//		//Act
//		RepositoryResponse reposonse = connector.getClient(actualFixture.repository()).postTaskData(taskDataGet,
//				changed, null);
//		assertNotNull(reposonse);
//		assertNotNull(reposonse.getReposonseKind());
//		assertThat(reposonse.getReposonseKind(), is(ResponseKind.TASK_UPDATED));
//		//Assert
//		TaskData taskDataUpdate = harness.getTaskFromServer(taskId);
//
//		attribute = taskDataUpdate.getRoot()
//				.getMappedAttribute(OSIORestCreateTaskSchema.getDefault().PRODUCT.getKey());
//		assertThat(attribute.getValue(), is("Product with Spaces"));
//		attribute = taskDataUpdate.getRoot()
//				.getMappedAttribute(OSIORestCreateTaskSchema.getDefault().COMPONENT.getKey());
//		assertThat(attribute.getValue(), is("Component 1"));
//		attribute = taskDataUpdate.getRoot()
//				.getMappedAttribute(OSIORestCreateTaskSchema.getDefault().VERSION.getKey());
//		assertThat(attribute.getValue(), is("b"));
//		attribute = taskDataUpdate.getRoot()
//				.getAttribute(OSIORestCreateTaskSchema.getDefault().TARGET_MILESTONE.getKey());
//		assertThat(attribute.getValue(), is("M3.0"));
//		attribute = taskDataUpdate.getRoot().getAttribute("cf_dropdown");
//		assertThat(attribute.getValue(), is("two"));
//		attribute = taskDataUpdate.getRoot().getAttribute("cf_multiselect");
//		assertThat(attribute.getValues(), is(Arrays.asList("Red", "Yellow")));
//	}
//
//	@Test
//	public void testAddComment() throws Exception {
//		String taskId = harness.getNewTaksId4TestProduct();
//		TaskData taskDataGet = harness.getTaskFromServer(taskId);
//
//		Set<TaskAttribute> changed = new HashSet<TaskAttribute>();
//
//		TaskAttribute attribute = taskDataGet.getRoot()
//				.getAttribute(OSIORestTaskSchema.getDefault().NEW_COMMENT.getKey());
//		attribute.setValue("The Comment");
//		changed.add(attribute);
//
//		//Act
//		RepositoryResponse reposonse = connector.getClient(actualFixture.repository()).postTaskData(taskDataGet,
//				changed, null);
//		assertNotNull(reposonse);
//		assertNotNull(reposonse.getReposonseKind());
//		assertThat(reposonse.getReposonseKind(), is(ResponseKind.TASK_UPDATED));
//		//Assert
//		TaskData taskDataUpdate = harness.getTaskFromServer(taskId);
//
//		attribute = taskDataUpdate.getRoot().getMappedAttribute(TaskAttribute.PREFIX_COMMENT + "1");
//		assertNotNull(attribute);
//		TaskAttribute commentAttribute = attribute.getMappedAttribute(TaskAttribute.COMMENT_TEXT);
//		assertNotNull(commentAttribute);
//		assertThat(commentAttribute.getValue(), is("The Comment"));
//		commentAttribute = attribute.getMappedAttribute(TaskAttribute.COMMENT_NUMBER);
//		assertNotNull(commentAttribute);
//		assertThat(commentAttribute.getValue(), is("1"));
//		commentAttribute = attribute.getMappedAttribute(TaskAttribute.COMMENT_ISPRIVATE);
//		assertNotNull(commentAttribute);
//		assertThat(commentAttribute.getValue(), is("false"));
//	}
//
//	private TaskData getTaskData(final String taskId) throws CoreException, OSIORestException {
//		OSIORestClient client = connector.getClient(actualFixture.repository());
//		final Map<String, TaskData> results = new HashMap<String, TaskData>();
//		client.getTaskData(new HashSet<String>() {
//			private static final long serialVersionUID = 1L;
//
//			{
//				add(taskId);
//			}
//		}, actualFixture.repository(), new TaskDataCollector() {
//
//			@Override
//			public void accept(TaskData taskData) {
//				results.put(taskData.getTaskId(), taskData);
//			}
//		}, null);
//		return results.get(taskId);
//	}
//
//	@Test
//	public void testCreateCCAttribute() throws Exception {
//		final TaskMapping taskMappingInit = new TaskMapping() {
//			@Override
//			public String getSummary() {
//				return "Test CC Attribute";
//			}
//
//			@Override
//			public String getDescription() {
//				return "The Description";
//			}
//
//			@Override
//			public String getProduct() {
//				return "ManualTest";
//			}
//
//			@Override
//			public String getComponent() {
//				return "ManualC1";
//			}
//
//			@Override
//			public String getVersion() {
//				return "R1";
//			}
//		};
//		AbstractTaskDataHandler taskDataHandler = connector.getTaskDataHandler();
//		TaskAttributeMapper mapper = taskDataHandler.getAttributeMapper(actualFixture.repository());
//		TaskData taskData = new TaskData(mapper, actualFixture.repository().getConnectorKind(),
//				actualFixture.repository().getRepositoryUrl(), "");
//		taskDataHandler.initializeTaskData(actualFixture.repository(), taskData, taskMappingInit, null);
//		taskData.getRoot().getAttribute("cf_dropdown").setValue("one");
//		taskData.getRoot()
//				.getAttribute(OSIORestCreateTaskSchema.getDefault().TARGET_MILESTONE.getKey())
//				.setValue("M2");
//		taskData.getRoot().getAttribute(OSIORestCreateTaskSchema.getDefault().CC.getKey()).setValue(
//				"admin@mylyn.eclipse.org, tests@mylyn.eclipse.org");
//		RepositoryResponse reposonse = connector.getClient(actualFixture.repository()).postTaskData(taskData, null,
//				null);
//		assertNotNull(reposonse);
//		assertNotNull(reposonse.getReposonseKind());
//		assertThat(reposonse.getReposonseKind(), is(ResponseKind.TASK_CREATED));
//		TaskData taskDataUpdate = harness.getTaskFromServer(reposonse.getTaskId());
//		TaskAttribute ccAttrib = taskDataUpdate.getRoot()
//				.getAttribute(OSIORestCreateTaskSchema.getDefault().CC.getKey());
//		assertEquals(2, ccAttrib.getValues().size());
//		assertEquals("admin@mylyn.eclipse.org", ccAttrib.getValues().get(0));
//		assertEquals("tests@mylyn.eclipse.org", ccAttrib.getValues().get(1));
//	}
//
//	@Test
//	public void testCCAttribute() throws Exception {
//		String taskId = harness.getNewTaksId4TestProduct();
//		TaskData taskDataGet = harness.getTaskFromServer(taskId);
//
//		Set<TaskAttribute> changed = new HashSet<TaskAttribute>();
//
//		TaskAttribute attribute = taskDataGet.getRoot()
//				.getAttribute(OSIORestTaskSchema.getDefault().ADD_CC.getKey());
//		attribute.setValue("tests@mylyn.eclipse.org");
//		changed.add(attribute);
//
//		//Act
//		RepositoryResponse reposonse = connector.getClient(actualFixture.repository()).postTaskData(taskDataGet,
//				changed, null);
//		assertNotNull(reposonse);
//		assertNotNull(reposonse.getReposonseKind());
//		assertThat(reposonse.getReposonseKind(), is(ResponseKind.TASK_UPDATED));
//		//Assert
//		TaskData taskDataUpdate = harness.getTaskFromServer(taskId);
//		TaskAttribute ccAttrib = taskDataUpdate.getRoot()
//				.getAttribute(OSIORestCreateTaskSchema.getDefault().CC.getKey());
//		assertEquals(1, ccAttrib.getValues().size());
//		assertEquals("tests@mylyn.eclipse.org", ccAttrib.getValues().get(0));
//
//		TaskAttribute ccAddAttrib = taskDataUpdate.getRoot()
//				.getAttribute(OSIORestTaskSchema.getDefault().ADD_CC.getKey());
//		ccAddAttrib.setValue("admin@mylyn.eclipse.org");
//		changed.add(ccAddAttrib);
//
//		TaskAttribute ccRemoveAttrib = taskDataUpdate.getRoot()
//				.getAttribute(OSIORestTaskSchema.getDefault().REMOVE_CC.getKey());
//		ccRemoveAttrib.setValue("tests@mylyn.eclipse.org");
//		changed.add(ccRemoveAttrib);
//
//		//Act
//		reposonse = connector.getClient(actualFixture.repository()).postTaskData(taskDataUpdate, changed, null);
//		assertNotNull(reposonse);
//		assertNotNull(reposonse.getReposonseKind());
//		assertThat(reposonse.getReposonseKind(), is(ResponseKind.TASK_UPDATED));
//		//Assert
//		taskDataUpdate = harness.getTaskFromServer(taskId);
//		ccAttrib = taskDataUpdate.getRoot().getAttribute(OSIORestCreateTaskSchema.getDefault().CC.getKey());
//		assertEquals(1, ccAttrib.getValues().size());
//		assertEquals("admin@mylyn.eclipse.org", ccAttrib.getValues().get(0));
//	}


}
