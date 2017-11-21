/*******************************************************************************
 * Copyright (c) 2015, 2017 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.apache.http.HttpStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Area;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.IdNamed;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Iteration;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Label;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Space;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.SpaceSingleResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.User;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemLinkTypeData;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemTypeData;
import org.eclipse.mylyn.commons.core.operations.IOperationMonitor;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpResponse;
import org.eclipse.mylyn.commons.repositories.http.core.HttpUtil;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.osgi.util.NLS;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class OSIORestGetTaskData extends OSIORestGetRequest<List<TaskData>> {

	private final TaskRepository taskRepository;
	private final OSIORestConfiguration taskConfiguration;
	private final CommonHttpClient client;

	private final OSIORestConnector connector;

	public OSIORestGetTaskData(@SuppressWarnings("restriction") CommonHttpClient client, OSIORestConnector connector, String urlSuffix,
			TaskRepository taskRepository) throws CoreException {
		super(client, urlSuffix, null); //$NON-NLS-1$
		this.client = client;
		this.taskRepository = taskRepository;
		this.taskConfiguration = connector.getRepositoryConfiguration(taskRepository);
		this.connector = connector;
	}

	@Override
	protected List<TaskData> parseFromJson(InputStreamReader in) throws OSIORestException {
		TypeToken<List<TaskData>> type = new TypeToken<List<TaskData>>() {
		};
		return new GsonBuilder().registerTypeAdapter(type.getType(), new JSonTaskDataDeserializer())
				.create()
				.fromJson(in, type.getType());
	}

	@Override
	protected List<TaskData> doProcess(CommonHttpResponse response, IOperationMonitor monitor)
			throws IOException, OSIORestException {
		InputStream is = response.getResponseEntityAsStream();
		InputStreamReader in = new InputStreamReader(is);
		return parseFromJson(in);
	}

	@Override
	protected void doValidate(CommonHttpResponse response, IOperationMonitor monitor)
			throws IOException, OSIORestException {
		int statusCode = response.getStatusCode();
		if (statusCode != 400 && statusCode != 200) {
			if (statusCode == HttpStatus.SC_NOT_FOUND) {
				throw new OSIORestResourceNotFoundException(
						NLS.bind("Requested resource ''{0}'' does not exist", response.getRequestPath()));
			}
			throw new OSIORestException(NLS.bind("Unexpected response from OSIO REST server for ''{0}'': {1}",
					response.getRequestPath(), HttpUtil.getStatusText(statusCode)));
		}

	}

	OSIORestTaskSchema taskSchema = OSIORestTaskSchema.getDefault();

	private class JSonTaskDataDeserializer implements JsonDeserializer<ArrayList<TaskData>> {

		@Override
		public ArrayList<TaskData> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			ArrayList<TaskData> response = new ArrayList<TaskData>();
			OSIORestTaskDataHandler dataHandler = (OSIORestTaskDataHandler) connector.getTaskDataHandler();
			TaskAttributeMapper mapper = dataHandler.getAttributeMapper(taskRepository);
			if (json.getAsJsonObject().get("data") == null) { //$NON-NLS-1$
				return response;
			}
			for (JsonElement workitem : json.getAsJsonObject().get("data").getAsJsonArray()) { //$NON-NLS-1$
				JsonObject workitemdata = workitem.getAsJsonObject();
				JsonObject attributes = workitemdata.get("attributes").getAsJsonObject(); //$NON-NLS-1$
				JsonObject relationships = workitemdata.get("relationships").getAsJsonObject(); //$NON-NLS-1$
				JsonObject space = relationships.get("space").getAsJsonObject(); //$NON-NLS-1$
				JsonObject spaceData = space.get("data").getAsJsonObject(); //$NON-NLS-1$
				String spaceId = spaceData.get("id").getAsString(); //$NON-NLS-1$
				Map<String, Space> spaces = taskConfiguration.getSpaces();
				Space actualSpace = null;
				for (Space entry : spaces.values()) {
					if (entry.getId().equals(spaceId)) {
						actualSpace = entry;
						break;
					}
				}
				OSIORestClient restClient = null;
				try {
					restClient = connector.getClient(taskRepository);
				} catch (CoreException e2) {
					// TODO Auto-generated catch block
					com.google.common.base.Throwables.propagate(
							new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN,
									"Can not get OSIORestClient"))); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (actualSpace == null) {
					Map<String, Space> externalSpaces = taskConfiguration.getExternalSpaces();
					for (Space entry : externalSpaces.values()) {
						if (entry.getId().equals(spaceId)) {
							actualSpace = entry;
							break;
						}
					}
					if (actualSpace == null) {
						SpaceSingleResponse spaceResponse = null;
						try {
							spaceResponse = new OSIORestGetRequest<SpaceSingleResponse>(client, "/spaces/" + spaceId, new TypeToken<SpaceSingleResponse>() {}, true).run(new NullOperationMonitor());
							actualSpace = spaceResponse.getData();
						} catch (OSIORestException e) {
							continue;
						}
						try {
							Map<String, WorkItemTypeData> workItemTypes = restClient.getSpaceWorkItemTypes(new NullOperationMonitor(), actualSpace);
							actualSpace.setWorkItemTypes(workItemTypes);
							Map<String, WorkItemLinkTypeData> workItemLinkTypes = restClient.getSpaceWorkItemLinkTypes(new NullOperationMonitor(), actualSpace);
							actualSpace.setWorkItemLinkTypes(workItemLinkTypes);
							Map<String, Area> areas = restClient.getSpaceAreas(new NullOperationMonitor(), actualSpace);
							actualSpace.setAreas(areas);
							Map<String, Iteration> iterations = restClient.getSpaceIterations(new NullOperationMonitor(), actualSpace);
							actualSpace.setIterations(iterations);
							Map<String, Label> labels = restClient.getSpaceLabels(new NullOperationMonitor(), actualSpace);
							actualSpace.setLabels(labels);
							Map<String, User> users = restClient.getUsers(new NullOperationMonitor(), actualSpace);
							actualSpace.setUsers(users);
						} catch (OSIORestException e) {
							e.printStackTrace();
							continue;
						}
						externalSpaces.put(actualSpace.getName(), actualSpace);
					}
				}
				String spaceName = actualSpace.getName();
				User owner = null;
				try {
					owner = restClient.getOwnedByLink(new NullOperationMonitor(), actualSpace);
				} catch (OSIORestException e1) {
					com.google.common.base.Throwables.propagate(
							new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN,
									"Can not get owner of Space (" + spaceId + ")"))); //$NON-NLS-1$ //$NON-NLS-2$
				}				
				
				int number = attributes.get("system.number").getAsInt(); //$NON-NLS-1$
				String taskId = owner.getName() + "/" + spaceName + "#" + number; //$NON-NLS-1$ //$NON-NLS-2$
				TaskData taskData = null;
				taskData = new TaskData(mapper, connector.getConnectorKind(), taskRepository.getRepositoryUrl(),
						taskId);
				try {
					dataHandler.initializeTaskData(taskRepository, taskData, null, null);
				} catch (CoreException e) {
					com.google.common.base.Throwables.propagate(e);
				}
				response.add(taskData);
				TaskAttribute spaceIdAttribute = taskData.getRoot().getAttribute(taskSchema.SPACE_ID.getKey());
				spaceIdAttribute.setValue(spaceId);
				TaskAttribute idAttribute = taskData.getRoot().getAttribute(taskSchema.ID.getKey());
				idAttribute.setValue(taskId);
				TaskAttribute uuidAttribute = taskData.getRoot().getAttribute(taskSchema.UUID.getKey());
				String uuid = workitemdata.get("id").getAsString(); //$NON-NLS-1$
				uuidAttribute.setValue(uuid);
				TaskAttribute spaceAttribute = taskData.getRoot().getAttribute(taskSchema.SPACE.getKey());
				spaceAttribute.setValue(spaceName);
				TaskAttribute addLinkAttribute = taskData.getRoot().getAttribute(taskSchema.ADD_LINK.getKey());
				addLinkAttribute.putOption("space", actualSpace.getId()); //$NON-NLS-1$
				// handle fields in the attributes section
				for (Entry<String, JsonElement> entry : attributes.entrySet()) {
					String attributeId = OSIORestTaskSchema.getAttributeNameFromFieldName(entry.getKey());
					if (entry.getKey().equals("system.updated_at")) { //$NON-NLS-1$
						TaskAttribute attribute = taskData.getRoot()
								.getAttribute(taskSchema.DATE_MODIFICATION.getKey());
						JsonElement value = entry.getValue();
						if (attribute != null) {
							try {
								SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", //$NON-NLS-1$
										Locale.US);
								iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
								Date tempDate = iso8601Format.parse(value.getAsString());
								attribute.setValue(Long.toString(tempDate.getTime()));
								continue;
							} catch (ParseException e) {
								com.google.common.base.Throwables.propagate(
										new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN,
												"Can not parse Date (" + value.getAsString() + ")"))); //$NON-NLS-1$ //$NON-NLS-2$
							}
						}
					} else if (entry.getKey().equals("system.created_at")) { //$NON-NLS-1$
						TaskAttribute attribute = taskData.getRoot()
								.getAttribute(taskSchema.DATE_CREATION.getKey());
						JsonElement value = entry.getValue();
						if (attribute != null) {
							try {
								SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", //$NON-NLS-1$
										Locale.US);
								iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
								Date tempDate = iso8601Format.parse(value.getAsString());
								attribute.setValue(Long.toString(tempDate.getTime()));
								continue;
							} catch (ParseException e) {
								com.google.common.base.Throwables.propagate(
										new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN,
												"Can not parse Date (" + value.getAsString() + ")"))); //$NON-NLS-1$ //$NON-NLS-2$
							}
						}
					} 

					TaskAttribute attribute = taskData.getRoot().getAttribute(attributeId);
					if (attribute != null) {
						JsonElement value = entry.getValue();
						if (!value.isJsonNull()) {
							if (value.isJsonArray()) {
								JsonArray valueArray = value.getAsJsonArray();
								attribute.clearValues();
								for (JsonElement jsonElement : valueArray) {
									attribute.addValue(jsonElement.getAsString());
								}
							} else {
								attribute.setValue(entry.getValue().getAsString());
							}
						}
					}
				}
				// handle fields in the relationships section
				for (Entry<String, JsonElement> entry : relationships.entrySet()) {
					String attributeId = OSIORestTaskSchema.getAttributeNameFromFieldName(entry.getKey());
					if (attributeId.equals("space") //$NON-NLS-1$
							|| attributeId.equals("assignees") //$NON-NLS-1$
							|| attributeId.equals("creator") //$NON-NLS-1$
							|| attributeId.equals("labels") //$NON-NLS-1$
						    || attributeId.equals("children")) { //$NON-NLS-1$
						continue;
					}
					TaskAttribute attribute = taskData.getRoot().getAttribute(attributeId);
					if (attribute != null) {
						JsonObject entryObject = entry.getValue().getAsJsonObject();
						if (entryObject.has("data")) { //$NON-NLS-1$
							JsonObject entryData = entryObject.get("data").getAsJsonObject(); //$NON-NLS-1$
							String entryId = entryData.get("id").getAsString(); //$NON-NLS-1$
							Map<String, IdNamed> itemMap = actualSpace.getMapFor(entry.getKey());
							if (itemMap != null) {
								for (Entry<String, IdNamed> itemEntry : itemMap.entrySet()) {
									if (itemEntry.getValue().getId().equals(entryId)) {
										attribute.setValue(itemEntry.getKey());
										break;
									}
								}
							}
						}
					}
				}
				
				// add assignee id (will resolve later)
				TaskAttribute assigneeIDs = taskData.getRoot().getAttribute(taskSchema.ASSIGNEE_IDS.getKey());
				JsonObject assigneeObject = relationships.get("assignees").getAsJsonObject(); //$NON-NLS-1$
				if (assigneeObject.get("data") != null) { //$NON-NLS-1$
					JsonArray assigneeArray = assigneeObject.get("data").getAsJsonArray(); //$NON-NLS-1$
					for (JsonElement entry : assigneeArray) {
						JsonObject entryObject = entry.getAsJsonObject();
						String id = entryObject.get("id").getAsString(); //$NON-NLS-1$
						assigneeIDs.addValue(id);
					}
				}
				
				// add creator id (will resolve later)
				TaskAttribute creatorID = taskData.getRoot().getAttribute(taskSchema.CREATOR_ID.getKey());
				JsonObject creatorObject = relationships.get("creator").getAsJsonObject(); //$NON-NLS-1$
				JsonObject creatorData = creatorObject.get("data").getAsJsonObject(); //$NON-NLS-1$
				creatorID.setValue(creatorData.get("id").getAsString()); //$NON-NLS-1$
				
				// add labels link (will resolve later)
				TaskAttribute labelsLink = taskData.getRoot().getAttribute(taskSchema.LABELS_LINK.getKey());
				JsonObject labelsObject = relationships.get("labels").getAsJsonObject(); //$NON-NLS-1$
				JsonObject labelsData = labelsObject.get("links").getAsJsonObject(); //$NON-NLS-1$
				labelsLink.setValue(labelsData.get("related").getAsString());
				
				// add workitem url
				TaskAttribute workitemURL = taskData.getRoot().getAttribute(taskSchema.TASK_URL.getKey());
				JsonObject linksObject = workitemdata.get("links").getAsJsonObject(); //$NON-NLS-1$
				String workitemself = linksObject.get("self").getAsString(); //$NON-NLS-1$
				workitemURL.setValue(workitemself);
				
				OSIORestConfiguration config;
				try {
					config = connector.getRepositoryConfiguration(taskRepository);
					if (config != null) {
						config.addValidOperations(taskData);
					}
				} catch (CoreException e) {
					com.google.common.base.Throwables.propagate(e);
				}
			}
			return response;
		}

	}

}