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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.operations.IOperationMonitor;
import org.eclipse.mylyn.commons.core.operations.OperationUtil;
import org.eclipse.mylyn.commons.net.AuthenticationCredentials;
import org.eclipse.mylyn.commons.net.Policy;
import org.eclipse.mylyn.commons.repositories.core.RepositoryLocation;
import org.eclipse.mylyn.commons.repositories.core.auth.AuthenticationType;
import org.eclipse.mylyn.commons.repositories.core.auth.UserCredentials;
import org.eclipse.mylyn.internal.commons.core.operations.NullOperationMonitor;
import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.RepositoryInfo;
import org.eclipse.mylyn.tasks.core.RepositoryVersion;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.AbstractTaskDataHandler;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.core.data.TaskDataCollector;
import org.eclipse.mylyn.tasks.core.data.TaskMapper;
import org.eclipse.mylyn.tasks.core.sync.ISynchronizationSession;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.UncheckedExecutionException;

public class OSIORestConnector extends AbstractRepositoryConnector {
	
	public final static String CONNECTOR_LABEL = "Connector.label"; //$NON-NLS-1$

	public static final Duration CLIENT_CACHE_DURATION = new Duration(24, TimeUnit.HOURS);

	public static final Duration CONFIGURATION_CACHE_EXPIRE_DURATION = new Duration(7, TimeUnit.DAYS);

	public static final Duration CONFIGURATION_CACHE_REFRESH_AFTER_WRITE_DURATION = new Duration(1, TimeUnit.DAYS);

	private static final ThreadLocal<IOperationMonitor> context = new ThreadLocal<IOperationMonitor>();
	
	private boolean ignoredProperty(String propertyName) {
		if (propertyName.equals(RepositoryLocation.PROPERTY_LABEL) || propertyName.equals(TaskRepository.OFFLINE)
				|| propertyName.equals(IRepositoryConstants.PROPERTY_ENCODING)
				|| propertyName.equals(TaskRepository.PROXY_HOSTNAME) || propertyName.equals(TaskRepository.PROXY_PORT)
				|| propertyName.equals("org.eclipse.mylyn.tasklist.repositories.savePassword") //$NON-NLS-1$
				|| propertyName.equals("org.eclipse.mylyn.tasklist.repositories.proxy.usedefault") //$NON-NLS-1$
				|| propertyName.equals("org.eclipse.mylyn.tasklist.repositories.proxy.savePassword") //$NON-NLS-1$
				|| propertyName.equals("org.eclipse.mylyn.tasklist.repositories.proxy.username") //$NON-NLS-1$
				|| propertyName.equals("org.eclipse.mylyn.tasklist.repositories.proxy.password") //$NON-NLS-1$
				|| propertyName.equals("org.eclipse.mylyn.tasklist.repositories.proxy.enabled")) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

	private final PropertyChangeListener repositoryChangeListener4ClientCache = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (ignoredProperty(evt.getPropertyName())) {
				return;
			}
			TaskRepository taskRepository = (TaskRepository) evt.getSource();
			clientCache.invalidate(new RepositoryKey(taskRepository));
		}
	};

	private final PropertyChangeListener repositoryChangeListener4ConfigurationCache = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (ignoredProperty(evt.getPropertyName())
					|| evt.getPropertyName().equals("org.eclipse.mylyn.tasklist.repositories.password")) { //$NON-NLS-1$
				return;
			}
			TaskRepository taskRepository = (TaskRepository) evt.getSource();
			configurationCache.invalidate(new RepositoryKey(taskRepository));
		}
	};

	private final LoadingCache<RepositoryKey, OSIORestClient> clientCache = CacheBuilder.newBuilder()
			.expireAfterAccess(CLIENT_CACHE_DURATION.getValue(), CLIENT_CACHE_DURATION.getUnit())
			.build(new CacheLoader<RepositoryKey, OSIORestClient>() {

				@Override
				public OSIORestClient load(RepositoryKey key) throws Exception {
					TaskRepository repository = key.getRepository();
					repository.addChangeListener(repositoryChangeListener4ClientCache);
					return createClient(repository);
				}
			});

	private final LoadingCache<RepositoryKey, Optional<OSIORestConfiguration>> configurationCache;
	
	public OSIORestConnector() {
		this(CONFIGURATION_CACHE_REFRESH_AFTER_WRITE_DURATION);
	}

	public OSIORestConnector(Duration refreshAfterWriteDuration) {
		super();
		configurationCache = createCacheBuilder(CONFIGURATION_CACHE_EXPIRE_DURATION, refreshAfterWriteDuration)
				.build(new CacheLoader<RepositoryKey, Optional<OSIORestConfiguration>>() {

					@Override
					public Optional<OSIORestConfiguration> load(RepositoryKey key) throws Exception {
						OSIORestClient client = clientCache.get(key);
						TaskRepository repository = key.getRepository();
						repository.addChangeListener(repositoryChangeListener4ConfigurationCache);
						return Optional.fromNullable(client.getConfiguration(key.getRepository(), context.get()));
					}

					@Override
					public ListenableFuture<Optional<OSIORestConfiguration>> reload(final RepositoryKey key,
							Optional<OSIORestConfiguration> oldValue) throws Exception {
						// asynchronous!
						ListenableFutureJob<Optional<OSIORestConfiguration>> job = new ListenableFutureJob<Optional<OSIORestConfiguration>>(
								"") {

							@Override
							protected IStatus run(IProgressMonitor monitor) {
								OSIORestClient client;
								try {
									client = clientCache.get(key);
									set(Optional
											.fromNullable(client.getConfiguration(key.getRepository(), context.get())));
								} catch (ExecutionException e) {
									e.printStackTrace();
									return new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN,
											"OSIORestConnector reload Configuration", e);
								}
								return Status.OK_STATUS;
							}
						};
						job.schedule();
						return job;
					}
				});
	}

	protected CacheBuilder<Object, Object> createCacheBuilder(Duration expireAfterWriteDuration,
			Duration refreshAfterWriteDuration) {
		return CacheBuilder.newBuilder()
				.expireAfterWrite(expireAfterWriteDuration.getValue(), expireAfterWriteDuration.getUnit())
				.refreshAfterWrite(refreshAfterWriteDuration.getValue(), refreshAfterWriteDuration.getUnit());
	}

	private OSIORestClient createClient(TaskRepository repository) {
		RepositoryLocation location = new RepositoryLocation(convertProperties(repository));
		OSIORestClient client = new OSIORestClient(location, this);

		return client;
	}

	private Map<String, String> convertProperties(TaskRepository repository) {
		return repository.getProperties().entrySet().stream().collect(
				Collectors.toMap(e -> convertProperty(e.getKey()), Map.Entry::getValue));
	}

	@SuppressWarnings("restriction")
	private String convertProperty(String key) {
		if (TaskRepository.PROXY_USEDEFAULT.equals(key)) {
			return RepositoryLocation.PROPERTY_PROXY_USEDEFAULT;
		} else if (TaskRepository.PROXY_HOSTNAME.equals(key)) {
			return RepositoryLocation.PROPERTY_PROXY_HOST;
		} else if (TaskRepository.PROXY_PORT.equals(key)) {
			return RepositoryLocation.PROPERTY_PROXY_PORT;
		}
		return key;
	}


	/**
	 * Returns the Client for the {@link TaskRepository}.
	 *
	 * @param repository
	 *            the {@link TaskRepository} object
	 * @return the client Object
	 * @throws CoreException
	 */
	public OSIORestClient getClient(TaskRepository repository) throws CoreException {
		try {
			return clientCache.get(new RepositoryKey(repository));
		} catch (ExecutionException e) {
			throw new CoreException(
					new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN, "TaskRepositoryManager is null"));
		}
	}


	@Override
	public boolean canCreateNewTask(TaskRepository repository) {
		return true;
	}

	@Override
	public boolean canCreateTaskFromKey(TaskRepository repository) {
		// ignore
		return false;
	}

	@Override
	public String getConnectorKind() {
		return OSIORestCore.CONNECTOR_KIND;
	}

	@Override
	public String getLabel() {
		return OSIORestMessages.getString(CONNECTOR_LABEL);
	}

	@Override
	public String getRepositoryUrlFromTaskUrl(String taskUrl) {
		if (taskUrl == null) {
			return null;
		}
		int index = taskUrl.indexOf("/api/"); //$NON-NLS-1$
		return index == -1 ? null : taskUrl.substring(0, index);
	}

	@Override
	public AbstractTaskDataHandler getTaskDataHandler() {
		return new OSIORestTaskDataHandler(this);
	}
	
	@Override
	public TaskData getTaskData(TaskRepository repository, String taskIdOrKey, IProgressMonitor monitor)
	throws CoreException {
		return ((OSIORestTaskDataHandler) getTaskDataHandler()).getTaskData(repository, taskIdOrKey, monitor);
	}

	@Override
	public String getTaskIdFromTaskUrl(String taskUrl) {
		// ignore
		return null;
	}

	public String getURLSuffix(String url) {
		int index = url.indexOf("/api/"); //$NON-NLS-1$
		return index == -1 ? null : url.substring(index + 4);
	}
	
	@Override
	public String getTaskUrl(String repositoryUrl, String taskIdOrKey) {
		return repositoryUrl + "/api/workitems/" + taskIdOrKey; //$NON-NLS-1$
	}

	@Override
	public boolean hasTaskChanged(TaskRepository taskRepository, ITask task, TaskData taskData) {
		String lastKnownLocalModValue = task
				.getAttribute(OSIORestTaskSchema.getDefault().DATE_MODIFICATION.getKey());
		TaskAttribute latestRemoteModAttribute = taskData.getRoot().getMappedAttribute(TaskAttribute.DATE_MODIFICATION);
		String latestRemoteModValue = latestRemoteModAttribute != null ? latestRemoteModAttribute.getValue() : null;
		return !Objects.equal(latestRemoteModValue, lastKnownLocalModValue);
	}

	@Override
	public IStatus performQuery(TaskRepository repository, IRepositoryQuery query, TaskDataCollector collector,
			ISynchronizationSession session, IProgressMonitor monitor) {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask("performQuery", IProgressMonitor.UNKNOWN);
			OSIORestClient client = getClient(repository);
			IOperationMonitor progress = OperationUtil.convert(monitor, "performQuery", 3); //$NON-NLS-1$
			return client.performQuery(repository, query, collector, progress);
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN, IStatus.INFO,
					"CoreException from performQuery", e);
		} catch (OSIORestException e) {
			return new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN, IStatus.INFO,
					"OSIORestException from performQuery", e);
		} finally {
			monitor.done();
		}
	}

	@Override
	public void updateRepositoryConfiguration(TaskRepository taskRepository, IProgressMonitor monitor)
			throws CoreException {
		context.set(monitor != null ? OperationUtil.convert(monitor) : new NullOperationMonitor());
		configurationCache.refresh(new RepositoryKey(taskRepository));
		context.remove();
	}

	@Override
	public void updateTaskFromTaskData(TaskRepository taskRepository, ITask task, TaskData taskData) {
		TaskMapper scheme = getTaskMapping(taskData);
		scheme.applyTo(task);
		task.setUrl(taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().TASK_URL.getKey()).getValue());

		boolean isComplete = false;
		TaskAttribute attributeStatus = taskData.getRoot().getMappedAttribute(TaskAttribute.STATUS);
		if (attributeStatus != null) {
			String statusValue = attributeStatus.getValue();
			isComplete = (statusValue.equals(IOSIORestConstants.RESOLVED) ||
					statusValue.equals(IOSIORestConstants.CLOSED));
		}
		if (taskData.isPartial()) {
			if (isComplete) {
				if (task.getCompletionDate() == null) {
					task.setCompletionDate(new Date(0));
				}
			} else {
				task.setCompletionDate(null);
			}
		} else {
			inferCompletionDate(task, taskData, scheme, isComplete);
		}

	}

	private void inferCompletionDate(ITask task, TaskData taskData, TaskMapper scheme, boolean isComplete) {
		if (isComplete) {
			Date completionDate = null;
			List<TaskAttribute> taskComments = taskData.getAttributeMapper().getAttributesByType(taskData,
					TaskAttribute.TYPE_COMMENT);
			if (taskComments != null && taskComments.size() > 0) {
				TaskAttribute lastComment = taskComments.get(0); // comments are in reverse order
				if (lastComment != null) {
					TaskAttribute attributeCommentDate = lastComment.getMappedAttribute(TaskAttribute.COMMENT_DATE);
					if (attributeCommentDate != null) {
						completionDate = new Date(Long.parseLong(attributeCommentDate.getValue()));
					}
				}
			}
			if (completionDate == null) {
				// Use last modified date
				TaskAttribute attributeLastModified = taskData.getRoot()
						.getMappedAttribute(TaskAttribute.DATE_MODIFICATION);
				if (attributeLastModified != null && attributeLastModified.getValue().length() > 0) {
					completionDate = taskData.getAttributeMapper().getDateValue(attributeLastModified);
				}
			}
			task.setCompletionDate(completionDate);
		} else {
			task.setCompletionDate(null);
		}
		// OSIO Specific Attributes

		// Space
		TaskAttribute attrDelta = taskData.getRoot()
				.getAttribute(OSIORestTaskSchema.getDefault().SPACE.getKey());
		if (attrDelta != null && !attrDelta.getValue().equals("")) { //$NON-NLS-1$
			task.setAttribute(OSIORestTaskSchema.getDefault().SPACE.getKey(), attrDelta.getValue());
		}

		// Date Modification
		attrDelta = taskData.getRoot()
				.getAttribute(OSIORestTaskSchema.getDefault().DATE_MODIFICATION.getKey());
		if (attrDelta != null && !attrDelta.getValue().equals("")) { //$NON-NLS-1$
			task.setAttribute(OSIORestTaskSchema.getDefault().DATE_MODIFICATION.getKey(), attrDelta.getValue());
		}
	}
	
	public RepositoryInfo validateRepository(TaskRepository repository, IProgressMonitor monitor) throws CoreException {
		try {
			OSIORestClient client = createClient(repository);
			if (!client.validate(OperationUtil.convert(monitor))) {
				throw new CoreException(
						new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN, "repository is invalid"));
			}
			// TODO: get real version when this is possible
			String version = "1.0.0"; //$NON-NLS-1$
			return new RepositoryInfo(new RepositoryVersion(version));
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN, e.getMessage(), e));
		}
	}

	public OSIORestConfiguration getRepositoryConfiguration(TaskRepository repository) throws CoreException {
		if (clientCache.getIfPresent(new RepositoryKey(repository)) == null) {
			getClient(repository);
		}
		try {
			Optional<OSIORestConfiguration> configurationOptional = configurationCache
					.get(new RepositoryKey(repository));
			return configurationOptional.isPresent() ? configurationOptional.get() : null;
		} catch (UncheckedExecutionException e) {
			throw new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN, e.getMessage(), e));
		} catch (ExecutionException e) {
			throw new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN, e.getMessage(), e));
		}
	}

	public void clearClientCache() {
		clientCache.invalidateAll();
	}

	public void clearConfigurationCache() {
		configurationCache.invalidateAll();
	}

	public void clearAllCaches() {
		clearClientCache();
		clearConfigurationCache();
	}

	@Override
	public boolean isRepositoryConfigurationStale(TaskRepository repository, IProgressMonitor monitor)
			throws CoreException {
		return false;
	}

	@Override
	public TaskMapper getTaskMapping(final TaskData taskData) {

		return new TaskMapper(taskData) {
			@Override
			public String getTaskKey() {
				TaskAttribute attribute = getTaskData().getRoot()
						.getAttribute(OSIORestTaskSchema.getDefault().ID.getKey());
				if (attribute != null) {
					return attribute.getValue();
				}
				return super.getTaskKey();
			}

			@Override
			public String getTaskKind() {
				return taskData.getConnectorKind();
			}

			@Override
			public String getTaskUrl() {
				return taskData.getRepositoryUrl();
			}
		};
	}


}
