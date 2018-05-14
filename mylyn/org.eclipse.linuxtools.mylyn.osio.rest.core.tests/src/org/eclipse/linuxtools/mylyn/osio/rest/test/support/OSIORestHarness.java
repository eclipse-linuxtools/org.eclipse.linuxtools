/*******************************************************************************
 * Copyright (c) 2015, 2018 Frank Becker and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.mylyn.osio.rest.test.support;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestConnector;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestCore;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.OSIORestException;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.SingleTaskDataCollector;
import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryResponse.ResponseKind;
import org.eclipse.mylyn.tasks.core.TaskInitializationData;
import org.eclipse.mylyn.tasks.core.TaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;

public class OSIORestHarness {
	private final OSIORestTestFixture fixture;

	public final TaskMapping taskMappingInitTestProduct = new TaskMapping() {
		@Override
		public String getSummary() {
			return "The Summary";
		}

		@Override
		public String getDescription() {
			return "The Description";
		}

		@Override
		public String getProduct() {
			return "ManualTest";
		}

		@Override
		public String getComponent() {
			return "ManualC1";
		}

		@Override
		public String getVersion() {
			return "R1";
		}
	};

	public final TaskInitializationData taskInitializationData = new TaskInitializationData() {
		private TaskData result;

		@Override
		public TaskData getTaskData() {
			if (result == null) {
				AbstractTaskDataHandler taskDataHandler = connector().getTaskDataHandler();
				TaskAttributeMapper mapper = taskDataHandler.getAttributeMapper(repository());
				result = new TaskData(mapper, repository().getConnectorKind(), repository().getRepositoryUrl(), ""); //$NON-NLS-1$
			}
			return result;
		}
	};

	public OSIORestHarness(OSIORestTestFixture fixture) {
		this.fixture = fixture;
	}

	private OSIORestConnector connector() {
		return fixture.connector();
	}

	private TaskRepository repository() {
		return fixture.repository();
	}

	public TaskData createTaskData(ITaskMapping initializationData, ITaskMapping selectionData,
			IProgressMonitor monitor) throws CoreException {
		AbstractTaskDataHandler taskDataHandler = connector().getTaskDataHandler();
		TaskAttributeMapper mapper = taskDataHandler.getAttributeMapper(repository());
		TaskData taskData = new TaskData(mapper, repository().getConnectorKind(), repository().getRepositoryUrl(), ""); //$NON-NLS-1$
		boolean result = taskDataHandler.initializeTaskData(repository(), taskData, initializationData, monitor);
		if (!result) {
			throw new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN,
					"Initialization of task failed. The provided data is insufficient.")); //$NON-NLS-1$
		}
		if (selectionData != null) {
			connector().getTaskMapping(taskData).merge(selectionData);
		}
		return taskData;
	}

	public String submitNewTask(TaskData taskData) throws OSIORestException, CoreException {
		RepositoryResponse reposonse = connector().getClient(repository()).postTaskData(taskData, null, null, null);
		assertThat(reposonse.getReposonseKind(), is(ResponseKind.TASK_CREATED));
		return reposonse.getTaskId();
	}

	public TaskData getTaskFromServer(String taskId) throws OSIORestException, CoreException {
		Set<String> taskIds = new HashSet<String>();
		taskIds.add(taskId);
		SingleTaskDataCollector singleTaskDataCollector = new SingleTaskDataCollector();
		connector().getClient(repository()).getTaskData(taskIds, repository(), singleTaskDataCollector, null);
		TaskData taskDataGet = singleTaskDataCollector.getTaskData();
		assertNotNull(taskDataGet);
		assertNotNull(taskDataGet.getRoot());
		return taskDataGet;
	}

	public String getNewTaksId4TestProduct() throws OSIORestException, CoreException {

		return getNewTaksIdFromInitMapping(taskMappingInitTestProduct, taskInitializationData);
	}

	public String getNewTaksIdFromInitMapping(final ITaskMapping taskMappingInit,
			final ITaskMapping taskMappingSelection) throws CoreException, OSIORestException {

		TaskData taskData = createTaskData(taskMappingInit, taskMappingSelection, null);
		String taskId = submitNewTask(taskData);
		return taskId;
	}

	public String getTaksId4RelationTask1() throws OSIORestException, CoreException {
		return getTaskIdWithSummary("RelationTask1");
	}

	public String getTaksId4RelationTask2() throws OSIORestException, CoreException {
		return getTaskIdWithSummary("RelationTask2");
	}

	public String getTaksId4RelationTask3() throws OSIORestException, CoreException {
		return getTaskIdWithSummary("RelationTask3");
	}

	private String getTaskIdWithSummary(String summary) throws OSIORestException, CoreException {
		String taskID = null;
		String queryUrlString = repository().getRepositoryUrl() + "/bug?" + "short_desc=" + summary;
		RepositoryQuery query = new RepositoryQuery(repository().getConnectorKind(), "handle-testQueryViaConnector");
		query.setUrl(queryUrlString);
		final Map<Integer, TaskData> changedTaskData = new HashMap<Integer, TaskData>();
		TaskDataCollector collector = new TaskDataCollector() {
			@Override
			public void accept(TaskData taskData) {
				changedTaskData.put(Integer.valueOf(taskData.getTaskId()), taskData);
			}
		};
		connector().performQuery(repository(), query, collector, null, new NullProgressMonitor());
		if (changedTaskData.size() > 0) {
			Set<Integer> ks = changedTaskData.keySet();
			SortedSet<Integer> sks = new TreeSet<Integer>(ks);
			taskID = sks.last().toString();
		} else {
			final TaskMapping taskMappingInit = new TaskMapping() {
				@Override
				public String getSummary() {
					return summary;
				}

				@Override
				public String getDescription() {
					return "The Description";
				}

				@Override
				public String getProduct() {
					return "ManualTest";
				}

				@Override
				public String getComponent() {
					return "ManualC1";
				}

				@Override
				public String getVersion() {
					return "R1";
				}
			};
			taskID = getNewTaksIdFromInitMapping(taskMappingInit, taskInitializationData);
		}
		return taskID;

	}

	public String[] getRelationTasks() throws OSIORestException, CoreException {
		List<String> result = new ArrayList<String>(3);
		result.add(getTaksId4RelationTask1());
		result.add(getTaksId4RelationTask2());
		result.add(getTaksId4RelationTask3());

		String[] array = new String[result.size()];
		array = result.toArray(array);
		return array;
	}

}
