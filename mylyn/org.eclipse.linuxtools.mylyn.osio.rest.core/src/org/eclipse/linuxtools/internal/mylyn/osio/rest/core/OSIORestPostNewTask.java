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
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Space;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemTypeData;
import org.eclipse.mylyn.commons.core.operations.IOperationMonitor;
import org.eclipse.mylyn.commons.repositories.core.RepositoryLocation;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpResponse;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class OSIORestPostNewTask extends OSIORestPostRequest<String> {
	
	private final TaskData taskData;
	private final Space space;
	private final TaskRepository taskRepository;
	private final OSIORestConnector connector;
	private final OSIORestConfiguration taskConfiguration;
	private final CommonHttpClient client;

	class TaskAttributeTypeAdapter extends TypeAdapter<TaskData> {
		RepositoryLocation location;

		public TaskAttributeTypeAdapter(RepositoryLocation location) {
			super();
			this.location = location;
		}

		private final Function<String, String> function = new Function<String, String>() {

			@Override
			public String apply(String input) {
				return OSIORestGsonUtil.convertString2GSonString(input);
			}
		};

		@Override
		public void write(JsonWriter out, TaskData taskData) throws IOException {
			out.beginObject();
			out.name("data"); //$NON-NLS-1$
			out.beginObject();
			out.name("attributes"); //$NON-NLS-1$
			out.beginObject();
			out.name("system.state").value("new"); //$NON-NLS-1$ //$NON-NLS-2$
			TaskAttribute titleAttribute = taskData.getRoot().getAttribute(taskSchema.SUMMARY.getKey());
			out.name("system.title").value(titleAttribute.getValue()); //$NON-NLS-1$
			out.name("version").value("1"); //$NON-NLS-1$ //$NON-NLS-2$
			TaskAttribute descAttribute = taskData.getRoot().getAttribute(taskSchema.DESCRIPTION.getKey());
			if (descAttribute != null) {
				String description = descAttribute.getValue();
				if (description != null && !description.isEmpty()) {
					out.name("system.description").value(description);
				}
			}
			out.endObject();
			out.name("relationships"); //$NON-NLS-1$
			out.beginObject();
			out.name("baseType"); //$NON-NLS-1$
			out.beginObject();
			out.name("data"); //$NON-NLS-1$
			out.beginObject();
			TaskAttribute taskType = taskData.getRoot().getAttribute(taskSchema.WORKITEM_TYPE.getKey());
			WorkItemTypeData taskTypeData = space.getWorkItemTypes().get(taskType.getValue());
			out.name("id").value(taskTypeData.getId()); //$NON-NLS-1$
			out.name("type").value("workitemtypes"); //$NON-NLS-1$
			out.endObject();
			out.endObject();
			out.name("space"); //$NON-NLS-1$
			out.beginObject();
			out.name("data"); //$NON-NLS-1$
			out.beginObject();
			out.name("id").value(space.getId()); //$NON-NLS-1$
			out.name("type").value("spaces"); //$NON-NLS-1$ //$NON-NLS-2$
			out.endObject();
			out.endObject();
			out.endObject();
			out.name("type").value("workitems"); //$NON-NLS-1$ //$NON-NLS-2$
			out.endObject();
			out.name("included"); //$NON-NLS-1$
			out.beginArray();
			out.value(true);
			out.endArray();
			out.endObject();
		}

		@Override
		public TaskData read(JsonReader in) throws IOException {
			throw new UnsupportedOperationException(
					"TaskAttributeTypeAdapter in OSIORestPatchUpdateTask only supports write"); //$NON-NLS-1$
		}

	}
	
	public OSIORestPostNewTask(CommonHttpClient client, TaskData taskData, Space space, OSIORestConnector connector,
			TaskRepository taskRepository) throws CoreException {
		super(client, "/spaces/" + space.getId() + "/workitems", true); //$NON-NLS-1$ //$NON-NLS-2$
		this.space = space;
		this.connector = connector;
		this.taskRepository = taskRepository;
		this.taskConfiguration = connector.getRepositoryConfiguration(taskRepository);
		this.taskData = taskData;
		this.client = client;
	}

	List<NameValuePair> requestParameters;

	@Override
	protected void addHttpRequestEntities(HttpRequestBase request) throws OSIORestException {
		super.addHttpRequestEntities(request);
		try {
			Gson gson = new GsonBuilder()
					.registerTypeAdapter(TaskData.class, new TaskAttributeTypeAdapter(getClient().getLocation()))
					.create();
			StringEntity requestEntity = new StringEntity(gson.toJson(taskData));
			((HttpPost) request).setEntity(requestEntity);
		} catch (UnsupportedEncodingException e) {
			Throwables.propagate(new CoreException(
					new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN, "Can not build HttpRequest", e))); //$NON-NLS-1$
		}
	}

	@Override
	protected void doValidate(CommonHttpResponse response, IOperationMonitor monitor)
			throws IOException, OSIORestException {
		validate(response, HttpStatus.SC_CREATED, monitor);
	}

	public static String convert(String str) {
		str = str.replace("\"", "\\\"").replace("\n", "\\\n"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
		StringBuffer ostr = new StringBuffer();
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if ((ch >= 0x0020) && (ch <= 0x007e)) {
				ostr.append(ch);
			} else {
				ostr.append("\\u"); //$NON-NLS-1$
				String hex = Integer.toHexString(str.charAt(i) & 0xFFFF);
				for (int j = 0; j < 4 - hex.length(); j++) {
					ostr.append("0"); //$NON-NLS-1$
				}
				ostr.append(hex.toLowerCase());
			}
		}
		return (new String(ostr));
	}

	@Override
	protected String parseFromJson(InputStreamReader in) {
		TypeToken<String> type = new TypeToken<String>() {
		};
		return new GsonBuilder().registerTypeAdapter(type.getType(), new JSonTaskDataDeserializer())
				.create()
				.fromJson(in, type.getType());
	}

	OSIORestTaskSchema taskSchema = OSIORestTaskSchema.getDefault();

	private class JSonTaskDataDeserializer implements JsonDeserializer<String> {

		@Override
		public String deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			OSIORestTaskDataHandler dataHandler = (OSIORestTaskDataHandler) connector.getTaskDataHandler();
			TaskAttributeMapper mapper = dataHandler.getAttributeMapper(taskRepository);
			JsonObject workitemdata = json.getAsJsonObject().get("data").getAsJsonObject(); //$NON-NLS-1$
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
			String spaceName = actualSpace.getName();
			int number = attributes.get("system.number").getAsInt(); //$NON-NLS-1$
			String taskId = spaceName + "#" + number; //$NON-NLS-1$
			return taskId;
//			taskData.getTaskId()
//			try {
//				dataHandler.initializeTaskData(taskRepository, taskData, null, null);
//			} catch (CoreException e) {
//				com.google.common.base.Throwables.propagate(e);
//			}
//			TaskAttribute idAttribute = taskData.getRoot().getAttribute(taskSchema.ID.getKey());
//			idAttribute.setValue(taskId);
//			TaskAttribute uuidAttribute = taskData.getRoot().getAttribute(taskSchema.UUID.getKey());
//			String uuid = workitemdata.get("id").getAsString(); //$NON-NLS-1$
//			uuidAttribute.setValue(uuid);
//			TaskAttribute spaceAttribute = taskData.getRoot().getAttribute(taskSchema.SPACE.getKey());
//			spaceAttribute.setValue(spaceName);
//			// handle fields in the attributes section
//			for (Entry<String, JsonElement> entry : attributes.entrySet()) {
//				String attributeId = OSIORestTaskSchema.getAttributeNameFromFieldName(entry.getKey());
//				if (entry.getKey().equals("system.updated_at")) { //$NON-NLS-1$
//					TaskAttribute attribute = taskData.getRoot()
//							.getAttribute(taskSchema.DATE_MODIFICATION.getKey());
//					JsonElement value = entry.getValue(); //.get("real_name");
//					if (attribute != null) {
//						try {
//							SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", //$NON-NLS-1$
//									Locale.US);
//							iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
//							Date tempDate = iso8601Format.parse(value.getAsString());
//							attribute.setValue(Long.toString(tempDate.getTime()));
//							continue;
//						} catch (ParseException e) {
//							com.google.common.base.Throwables.propagate(
//									new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN,
//											"Can not parse Date (" + value.getAsString() + ")"))); //$NON-NLS-1$ //$NON-NLS-2$
//						}
//					}
//				} else if (entry.getKey().equals("system.created_at")) { //$NON-NLS-1$
//					TaskAttribute attribute = taskData.getRoot()
//							.getAttribute(taskSchema.DATE_CREATION.getKey());
//					JsonElement value = entry.getValue(); //.get("real_name");
//					if (attribute != null) {
//						try {
//							SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", //$NON-NLS-1$
//									Locale.US);
//							iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
//							Date tempDate = iso8601Format.parse(value.getAsString());
//							attribute.setValue(Long.toString(tempDate.getTime()));
//							continue;
//						} catch (ParseException e) {
//							com.google.common.base.Throwables.propagate(
//									new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN,
//											"Can not parse Date (" + value.getAsString() + ")"))); //$NON-NLS-1$ //$NON-NLS-2$
//						}
//					}
//				} 
//
//				TaskAttribute attribute = taskData.getRoot().getAttribute(attributeId);
//				if (attribute != null) {
//					JsonElement value = entry.getValue();
//					if (!value.isJsonNull()) {
//						if (value.isJsonArray()) {
//							JsonArray valueArray = value.getAsJsonArray();
//							attribute.clearValues();
//							for (JsonElement jsonElement : valueArray) {
//								attribute.addValue(jsonElement.getAsString());
//							}
//						} else {
//							attribute.setValue(entry.getValue().getAsString());
//						}
//					}
//				}
//			}
//			// handle fields in the relationships section
//			for (Entry<String, JsonElement> entry : relationships.entrySet()) {
//				String attributeId = OSIORestTaskSchema.getAttributeNameFromFieldName(entry.getKey());
//				if (attributeId.equals("space") //$NON-NLS-1$
//						|| attributeId.equals("assignees") //$NON-NLS-1$
//						|| attributeId.equals("creator") //$NON-NLS-1$
//						|| attributeId.equals("children")) { //$NON-NLS-1$
//					continue;
//				}
//				TaskAttribute attribute = taskData.getRoot().getAttribute(attributeId);
//				if (attribute != null) {
//					JsonObject entryObject = entry.getValue().getAsJsonObject();
//					if (entryObject.has("data")) { //$NON-NLS-1$
//						JsonObject entryData = entryObject.get("data").getAsJsonObject(); //$NON-NLS-1$
//						String entryId = entryData.get("id").getAsString(); //$NON-NLS-1$
//						Map<String, IdNamed> itemMap = actualSpace.getMapFor(entry.getKey());
//						if (itemMap != null) {
//							for (Entry<String, IdNamed> itemEntry : itemMap.entrySet()) {
//								if (itemEntry.getValue().getId().equals(entryId)) {
//									attribute.setValue(itemEntry.getKey());
//									break;
//								}
//							}
//						}
//					}
//				}
//			}
//
//			// add assignee id (will resolve later)
//			TaskAttribute assigneeIDs = taskData.getRoot().getAttribute(taskSchema.ASSIGNEE_IDS.getKey());
//			JsonObject assigneeObject = relationships.get("assignees").getAsJsonObject(); //$NON-NLS-1$
//			if (assigneeObject.get("data") != null) { //$NON-NLS-1$
//				JsonArray assigneeArray = assigneeObject.get("data").getAsJsonArray(); //$NON-NLS-1$
//				for (JsonElement entry : assigneeArray) {
//					JsonObject entryObject = entry.getAsJsonObject();
//					String id = entryObject.get("id").getAsString(); //$NON-NLS-1$
//					assigneeIDs.addValue(id);
//				}
//			}
//
//			// add creator id (will resolve later)
//			TaskAttribute creatorID = taskData.getRoot().getAttribute(taskSchema.CREATOR_ID.getKey());
//			JsonObject creatorObject = relationships.get("creator").getAsJsonObject(); //$NON-NLS-1$
//			JsonObject creatorData = creatorObject.get("data").getAsJsonObject(); //$NON-NLS-1$
//			creatorID.setValue(creatorData.get("id").getAsString());
//
//			// add workitem url
//			TaskAttribute workitemURL = taskData.getRoot().getAttribute(taskSchema.TASK_URL.getKey());
//			JsonObject linksObject = workitemdata.get("links").getAsJsonObject(); //$NON-NLS-1$
//			String workitemself = linksObject.get("self").getAsString(); //$NON-NLS-1$
//			workitemURL.setValue(workitemself);
//
//			OSIORestConfiguration config;
//			try {
//				config = connector.getRepositoryConfiguration(taskRepository);
//				if (config != null) {
//					config.addValidOperations(taskData);
//				}
//			} catch (CoreException e) {
//				com.google.common.base.Throwables.propagate(e);
//			}
//			return taskData;
		}
	}

}