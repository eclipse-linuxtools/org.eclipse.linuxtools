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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.repositories.core.RepositoryLocation;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
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

public class OSIORestPostNewCommentTask extends OSIORestPostRequest<TaskAttribute> {

	private final TaskData taskData;
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
			out.beginObject();
			out.name("data"); //$NON-NLS-1$
			out.beginObject();
			for (TaskAttribute element : oldValues.oldAttributes) {
				TaskAttribute taskAttribute = taskData.getRoot().getAttribute(element.getId());
				String id = taskAttribute.getId();
				String value = OSIORestGsonUtil.convertString2GSonString(taskAttribute.getValue());
				id = OSIORestTaskSchema.getFieldNameFromAttributeName(id);
				if (id.equals(OSIORestTaskSchema.getDefault().NEW_COMMENT.getKey())) {
					out.name("attributes"); //$NON-NLS-1$
					out.beginObject();
					out.name("body").value(value); //$NON-NLS-1$
					out.name("markup").value("Markdown"); //$NON-NLS-1$ //$NON-NLS-2$
					out.endObject(); // end attributes
				}
			}
			out.name("type").value("comments"); //$NON-NLS-1$ //$NON-NLS-2$
			out.endObject(); // end data
			out.name("included").beginArray(); //$NON-NLS-1$
			out.endArray();
			out.endObject();
		}

		@Override
		public OldAttributes read(JsonReader in) throws IOException {
			throw new UnsupportedOperationException(
					"TaskAttributeTypeAdapter in OSIORestPatchUpdateTask only supports write"); //$NON-NLS-1$
		}

	}
	
	public OSIORestPostNewCommentTask(CommonHttpClient client, TaskData taskData, Set<TaskAttribute> oldAttributes) {
		super(client, "/workitems/" + //$NON-NLS-1$ 
				taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().UUID.getKey()).getValue() +
				"/comments", true); //$NON-NLS-1$
		this.taskData = taskData;
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
			((HttpPost) request).setEntity(requestEntity);
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
	protected TaskAttribute parseFromJson(InputStreamReader in) {
		TypeToken<TaskAttribute> type = new TypeToken<TaskAttribute>() {
		};
		return new GsonBuilder().registerTypeAdapter(type.getType(), new JSonTaskDataDeserializer())
				.create()
				.fromJson(in, type.getType());
	}

	OSIORestTaskSchema taskSchema = OSIORestTaskSchema.getDefault();

	private class JSonTaskDataDeserializer implements JsonDeserializer<TaskAttribute> {

		@Override
		public TaskAttribute deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			int i = 0;
			JsonElement commentEntry = (JsonObject) json.getAsJsonObject().get("data"); //$NON-NLS-1$
			JsonObject comment = (JsonObject) commentEntry.getAsJsonObject();
			JsonObject attributes = (JsonObject) comment.get("attributes").getAsJsonObject(); //$NON-NLS-1$
			JsonObject relationships = (JsonObject) comment.get("relationships").getAsJsonObject(); //$NON-NLS-1$
			JsonObject creator = (JsonObject) relationships.get("creator").getAsJsonObject(); //$NON-NLS-1$
			JsonObject creatorData = (JsonObject) creator.get("data").getAsJsonObject(); //$NON-NLS-1$
			JsonObject commentLinks = (JsonObject) comment.get("links").getAsJsonObject(); //$NON-NLS-1$
			TaskAttribute attribute = taskData.getRoot()
					.createAttribute(TaskAttribute.PREFIX_COMMENT + i);
			TaskAttribute numComments = taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().COMMENT_COUNT.getKey());
			numComments.setValue(Integer.toString(i));
			TaskCommentMapper taskComment = TaskCommentMapper.createFrom(attribute);
			taskComment.setCommentId(comment.get("id").getAsString()); //$NON-NLS-1$
			taskComment.setNumber(i);
			taskComment.setUrl(commentLinks.get("self").getAsString()); //$NON-NLS-1$
			String id = creatorData.get("id").getAsString(); //$NON-NLS-1$
			OSIORestUser user = null;
			String email = "??????"; //$NON-NLS-1$
			String fullName = "???"; //$NON-NLS-1$
			try {
				user = new OSIORestGetUser(client, id).run(new NullOperationMonitor());
				email = user.getEmail();
				fullName = user.getFullName();
			} catch (OSIORestException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} //$NON-NLS-1$
			IRepositoryPerson author = taskData.getAttributeMapper()
					.getTaskRepository()
					.createPerson(email); //$NON-NLS-1$
			author.setName(fullName);
			taskComment.setAuthor(author);
			taskComment.setIsPrivate(null);
			try {
				SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", //$NON-NLS-1$
						Locale.US);
				iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC")); //$NON-NLS-1$
				Date tempDate = iso8601Format.parse(attributes.get("created-at").getAsString()); //$NON-NLS-1$
				taskComment.setCreationDate(tempDate);
			} catch (ParseException e) {
				com.google.common.base.Throwables.propagate(new CoreException(new Status(IStatus.ERROR,
						OSIORestCore.ID_PLUGIN,
						"Can not parse Date (" + attributes.get("created-at").getAsString() + ")"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}

			if (attributes.get("body.rendered").getAsString() != null) { //$NON-NLS-1$
				String commentText = attributes.get("body.rendered").getAsString().trim(); //$NON-NLS-1$
				taskComment.setText(commentText);
			}
			taskComment.applyTo(attribute);
			return attribute;
		}
	}

}