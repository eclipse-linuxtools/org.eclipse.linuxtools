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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Area;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Iteration;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Label;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.LabelResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Space;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.User;
import org.eclipse.mylyn.commons.repositories.core.RepositoryLocation;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
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

@SuppressWarnings("restriction")
public class OSIORestPatchUpdateTask extends OSIORestPatchRequest<TaskData> {

	private final TaskData taskData;
	private final OSIORestTaskSchema taskSchema;
	private final Space space;
	private final CommonHttpClient client;

	class OldAttributes {
		private final Set<TaskAttribute> oldAttributes;

		public OldAttributes(Set<TaskAttribute> oldAttributes) {
			super();
			this.oldAttributes = oldAttributes;
		}

	}

	OldAttributes oldAttributes;


	class TaskAttributeTypeAdapter extends TypeAdapter<OldAttributes> {
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
		public void write(JsonWriter out, OldAttributes oldValues) throws IOException {
			out.beginObject(); // BEGIN
			out.name("data"); //$NON-NLS-1$
			out.beginObject();
			out.name("attributes"); //$NON-NLS-1$
			out.beginObject();
			TaskAttribute attribute = taskData.getRoot().getAttribute(taskSchema.DESCRIPTION.getKey());
			String attValue = attribute.getValue() == null ? "" : attribute.getValue(); //$NON-NLS-1$
			out.name("system.description").value(attValue); //$NON-NLS-1$
			if (taskData.getRoot().getAttribute(TaskAttribute.OPERATION) != null) {
				attribute = taskData.getRoot().getAttribute(TaskAttribute.OPERATION);
			} else {
				attribute = taskData.getRoot().getAttribute(taskSchema.STATUS.getKey());
			}
			attValue = attribute.getValue();
			out.name("system.state").value(attValue); //$NON-NLS-1$
			attribute = taskData.getRoot().getAttribute(taskSchema.SUMMARY.getKey());
			attValue = attribute.getValue();
			out.name("system.title").value(attValue); //$NON-NLS-1$
			attribute = taskData.getRoot().getAttribute(taskSchema.VERSION.getKey());
			attValue = attribute.getValue();
			out.name("version").value(attValue);
			out.endObject(); // attributes
			attribute = taskData.getRoot().getAttribute(taskSchema.UUID.getKey());
			attValue = attribute.getValue();
			out.name("id").value(attValue); //$NON-NLS-1$
			// relationships
			out.name("relationships"); //$NON-NLS-1$
			out.beginObject();
			// space
			out.name("space"); //$NON-NLS-1$
			out.beginObject();
			out.name("data"); //$NON-NLS-1$
			out.beginObject();
			attribute = taskData.getRoot().getAttribute(taskSchema.SPACE.getKey());
			String spaceName = attribute.getValue();
			out.name("id").value(space.getId()); //$NON-NLS-1$
			out.name("type").value("spaces"); //$NON-NLS-1$ //$NON-NLS-2$
			out.endObject(); // space data
			out.endObject(); // spaces
			// area
			attribute = taskData.getRoot().getAttribute(taskSchema.AREA.getKey());
			String areaName = attribute.getValue();
			if (areaName != null && !areaName.trim().isEmpty()) {
				out.name("area"); //$NON-NLS-1$
				out.beginObject();
				out.name("data"); //$NON-NLS-1$
				out.beginObject();
				Area area = space.getAreas().get(areaName);
				out.name("id").value(area.getId()); //$NON-NLS-1$
				out.name("type").value("areas"); //$NON-NLS-1$ //$NON-NLS-2$
				out.endObject(); // area data
				out.endObject(); // areas
			}
			// iteration
			attribute = taskData.getRoot().getAttribute(taskSchema.ITERATION.getKey());
			String iterationName = attribute.getValue();
			if (iterationName != null && !iterationName.trim().isEmpty()) {
				out.name("iteration"); //$NON-NLS-1$
				out.beginObject();
				out.name("data"); //$NON-NLS-1$
				out.beginObject();
				Iteration iteration = space.getIterations().get(iterationName);
				out.name("id").value(iteration.getId()); //$NON-NLS-1$
				out.name("type").value("iterations"); //$NON-NLS-1$ //$NON-NLS-2$
				out.endObject(); // iteration data
				out.endObject(); // iterations
			}
			// assignees
			out.name("assignees"); //$NON-NLS-1$
			out.beginObject();
			attribute = taskData.getRoot().getAttribute(taskSchema.ASSIGNEES.getKey());
			List<String> assignees = attribute.getValues();
			if (assignees == null) {
				assignees = new ArrayList<String>();
			} else {
				assignees = new ArrayList<>(assignees);
			}
			attribute = taskData.getRoot().getAttribute(taskSchema.REMOVE_ASSIGNEE.getKey());
			List<String> removals = attribute.getValues();
			if (removals != null) {
				for (String removal : removals) {
					int index = assignees.indexOf(removal);
					if (index >= 0) {
						assignees.remove(index);
					}
				}
			}
			attribute = taskData.getRoot().getAttribute(taskSchema.ADD_ASSIGNEE.getKey());
			List<String> additions = attribute.getValues();
			if (additions != null) {
				for (String addition : additions) {
					int index = assignees.indexOf(addition);
					if (index < 0) {
						assignees.add(addition);
					}
				}
			}
			if (assignees.size() > 0 && !assignees.get(0).isEmpty()) {
				Map<String, User> users = space.getUsers();
				out.name("data"); //$NON-NLS-1$
				out.beginArray();
				for (String assignee : assignees) {
					if (assignee != null && !assignee.isEmpty()) {
						User user = users.get(assignee);
						if (user == null) {
							throw new UnsupportedOperationException(
									OSIORestMessages.getFormattedString("UnknownAssignee.msg", assignee)); //$NON-NLS-1$
						}
						String userid = user.getId();
						out.beginObject();
						out.name("id").value(userid); //$NON-NLS-1$
						out.name("type").value("users"); //$NON-NLS-1$ //$NON-NLS-2$
						out.endObject();
					}
				}
				out.endArray();
			}
			out.endObject(); // assignees
			// labels
			attribute = taskData.getRoot().getAttribute(taskSchema.LABELS.getKey());
			List<String> labels = attribute.getValues();
			if (labels == null) {
				labels = new ArrayList<String>();
			} else {
				labels = new ArrayList<>(labels);
			}
			attribute = taskData.getRoot().getAttribute(taskSchema.REMOVE_LABEL.getKey());
			List<String> labelRemovals = attribute.getValues();
			if (labelRemovals != null) {
				for (String removal : labelRemovals) {
					int index = labels.indexOf(removal);
					if (index >= 0) {
						labels.remove(index);
					}
				}
			}
			attribute = taskData.getRoot().getAttribute(taskSchema.ADD_LABEL.getKey());
			String labelAddString = attribute.getValue();
			String[] labelAdditions = labelAddString.split(",");
			if (labelAdditions != null) {
				for (String addition : labelAdditions) {
					int index = labels.indexOf(addition);
					if (index < 0) {
						labels.add(addition);
					}
				}
			}
			out.name("labels"); //$NON-NLS-1$
			out.beginObject();
			out.name("data"); //$NON-NLS-1$
			out.beginArray();
			if (labels.size() > 0 && !labels.get(0).isEmpty()) {
				Map<String, Label> spaceLabels = space.getLabels();
				for (String label : labels) {
					Label l = spaceLabels.get(label);
					if (l == null && !label.trim().isEmpty()) {
						try {
							LabelResponse response = new OSIORestPostNewLabelTask(client, space, label).run(new NullOperationMonitor());
							Label newLabel = response.getData();
							spaceLabels.put(label, newLabel);
						} catch (OSIORestException e) {
							e.printStackTrace();
						}
					}
					l = spaceLabels.get(label);
					if (l != null) {
						String labelid = l.getId();
						out.beginObject();
						out.name("id").value(labelid); //$NON-NLS-1$
						out.name("type").value("labels"); //$NON-NLS-1$ //$NON-NLS-2$
						out.endObject();
					}
				}
			}
			out.endArray();
			out.endObject(); // labels
			out.endObject(); // relationships
			
			out.name("type").value("workitems"); //$NON-NLS-1$ //$NON-NLS-2$
			
			out.endObject(); // data
			
			out.name("included"); //$NON-NLS-1$
			out.beginArray();
			out.value(true);
			out.endArray();
			
			out.endObject(); // END
		}

		@Override
		public OldAttributes read(JsonReader in) throws IOException {
			throw new UnsupportedOperationException(
					"TaskAttributeTypeAdapter in OSIORestPatchUpdateTask only supports write"); //$NON-NLS-1$
		}

	}
	
	public OSIORestPatchUpdateTask(CommonHttpClient client, TaskData taskData, 
			Set<TaskAttribute> oldAttributes, Space space) {
		super(client, "/workitems/" + //$NON-NLS-1$ 
				taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().UUID.getKey()).getValue(), true); //$NON-NLS-1$
		this.taskData = taskData;
		this.taskSchema = OSIORestTaskSchema.getDefault();
		this.space = space;
		this.client = client;
		this.oldAttributes = new OldAttributes(oldAttributes);
	}

	List<NameValuePair> requestParameters;

	@Override
	protected void addHttpRequestEntities(HttpRequestBase request) throws OSIORestException {
		super.addHttpRequestEntities(request);
		try {
			Gson gson = new GsonBuilder()
					.registerTypeAdapter(OldAttributes.class, new TaskAttributeTypeAdapter(getClient().getLocation()))
					.create();
			StringEntity requestEntity = new StringEntity(gson.toJson(oldAttributes));
			((HttpPatch) request).setEntity(requestEntity);
		} catch (UnsupportedEncodingException e) {
			Throwables.propagate(new CoreException(
					new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN, "Can not build HttpRequest", e))); //$NON-NLS-1$
		}
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
	protected TaskData parseFromJson(InputStreamReader in) {
		TypeToken<TaskData> type = new TypeToken<TaskData>() {
		};
		return new GsonBuilder().registerTypeAdapter(type.getType(), new JSonTaskDataDeserializer())
				.create()
				.fromJson(in, type.getType());
	}

	private class JSonTaskDataDeserializer implements JsonDeserializer<TaskData> {

		@Override
		public TaskData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonObject workitemdata = json.getAsJsonObject().get("data").getAsJsonObject(); //$NON-NLS-1$
			JsonObject attributes = workitemdata.get("attributes").getAsJsonObject(); //$NON-NLS-1$
			
			String version = attributes.get("version").getAsString(); //$NON-NLS-1$
			TaskAttribute versionAttr = taskData.getRoot().getAttribute(taskSchema.VERSION.getKey());
			versionAttr.setValue(version);
			
			String updateString = attributes.get("system.updated_at").getAsString(); //$NON-NLS-1$
			TaskAttribute updatedAt = taskData.getRoot()
					.getAttribute(taskSchema.DATE_MODIFICATION.getKey());
			try {
				SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", //$NON-NLS-1$
						Locale.US);
				iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
				Date tempDate = iso8601Format.parse(updateString);
				updatedAt.setValue(Long.toString(tempDate.getTime()));
			} catch (ParseException e) {
				com.google.common.base.Throwables.propagate(
						new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN,
								"Can not parse Date (" + updateString + ")"))); //$NON-NLS-1$ //$NON-NLS-2$
			}	
			
			return taskData;
		}
	}

}