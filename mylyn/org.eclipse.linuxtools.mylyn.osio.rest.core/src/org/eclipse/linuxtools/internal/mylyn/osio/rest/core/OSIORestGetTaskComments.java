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

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Space;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.User;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class OSIORestGetTaskComments extends OSIORestGetRequest<ArrayList<TaskAttribute>> {
	
	private final TaskData taskData;
	private final Space space;
	@SuppressWarnings("restriction")
	private final CommonHttpClient client;

	public OSIORestGetTaskComments(@SuppressWarnings("restriction") CommonHttpClient client, Space space, TaskData taskData) {
		super(client, "/workitems/" + taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().UUID.getKey()).getValue() + "/comments", null); //$NON-NLS-1$ //$NON-NLS-2$
		this.taskData = taskData;
		this.space = space;
		this.client = client;
	}

	@Override
	protected ArrayList<TaskAttribute> parseFromJson(InputStreamReader in) {
		TypeToken<ArrayList<TaskAttribute>> type = new TypeToken<ArrayList<TaskAttribute>>() {
		};
		return new GsonBuilder().registerTypeAdapter(type.getType(), new JSonTaskDataDeserializer())
				.create()
				.fromJson(in, type.getType());
	}

	OSIORestTaskSchema taskSchema = OSIORestTaskSchema.getDefault();

	private class JSonTaskDataDeserializer implements JsonDeserializer<ArrayList<TaskAttribute>> {

		@Override
		public ArrayList<TaskAttribute> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			ArrayList<TaskAttribute> response = new ArrayList<TaskAttribute>();
			int i = 0;
			Map<String, User> users = space.getUsers();
			for (JsonElement commentEntry : json.getAsJsonObject().get("data") //$NON-NLS-1$
					.getAsJsonArray()) {
				JsonObject comment = (JsonObject) commentEntry.getAsJsonObject();
				JsonObject attributes = (JsonObject) comment.get("attributes").getAsJsonObject(); //$NON-NLS-1$
				JsonObject relationships = (JsonObject) comment.get("relationships").getAsJsonObject(); //$NON-NLS-1$
				JsonObject creator = (JsonObject) relationships.get("creator").getAsJsonObject(); //$NON-NLS-1$
				JsonObject creatorData = (JsonObject) creator.get("data").getAsJsonObject(); //$NON-NLS-1$
				JsonObject commentLinks = (JsonObject) comment.get("links").getAsJsonObject(); //$NON-NLS-1$
				TaskAttribute attribute = taskData.getRoot()
						.createAttribute(TaskAttribute.PREFIX_COMMENT + i++);
				TaskAttribute numComments = taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().COMMENT_COUNT.getKey());
				numComments.setValue(Integer.toString(i));
				TaskCommentMapper taskComment = TaskCommentMapper.createFrom(attribute);
				taskComment.setCommentId(comment.get("id").getAsString()); //$NON-NLS-1$
				taskComment.setNumber(i);
				taskComment.setUrl(commentLinks.get("self").getAsString()); //$NON-NLS-1$
				String id = creatorData.get("id").getAsString(); //$NON-NLS-1$
				String email = "??????"; //$NON-NLS-1$
				String fullName = "???"; //$NON-NLS-1$
				try {
					// check for the comment creator in the collaborators list of users
					for (Entry<String, User> entry : users.entrySet()) {
						User user = entry.getValue();
						if (user.getId().equals(id)) {
							email = user.getAttributes().getEmail();
							fullName = user.getAttributes().getFullName();
							break;
						}
					}
					// most users should be in the collaborators list, but it is possible that
					// a previous collaborator has been removed from the space and so we have
					// to go fetch their data now
					if (email.startsWith("?")) { //$NON-NLS-1$
						OSIORestUser user = new OSIORestGetUser(client, id).run(new NullOperationMonitor());
						email = user.getEmail();
						fullName = user.getFullName();
					}
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

				if (attributes.get("body").getAsString() != null) { //$NON-NLS-1$
					String commentText = attributes.get("body").getAsString().trim(); //$NON-NLS-1$
					taskComment.setText(commentText);
				}
				taskComment.applyTo(attribute);
				response.add(attribute);
			}
			// Comments come back in reverse time order...number them backwards so replies are consistent
			int numComments = response.size();
			for (i = 0; i < numComments; ++i) {
				TaskAttribute attribute = response.get(i);
				TaskCommentMapper comment = TaskCommentMapper.createFrom(attribute);
				comment.setNumber(numComments - i);
				comment.applyTo(attribute);
			}
			return response;
		}

	}

}