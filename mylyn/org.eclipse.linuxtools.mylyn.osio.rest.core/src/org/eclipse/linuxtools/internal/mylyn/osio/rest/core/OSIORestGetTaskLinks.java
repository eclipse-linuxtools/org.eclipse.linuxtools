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
import java.util.TimeZone;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Space;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
import org.eclipse.mylyn.tasks.core.IRepositoryPerson;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMetaData;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class OSIORestGetTaskLinks extends OSIORestGetRequest<ArrayList<TaskAttribute>> {
	
	private final TaskData taskData;
	private final Space space;
	private final String wid;
	
	@SuppressWarnings("restriction")
	private final CommonHttpClient client;

	public OSIORestGetTaskLinks(@SuppressWarnings("restriction") CommonHttpClient client, Space space, TaskData taskData) {
		super(client, "/workitems/" + taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().UUID.getKey()).getValue() + "/relationships/links", null); //$NON-NLS-1$ //$NON-NLS-2$
		this.taskData = taskData;
		this.space = space;
		this.wid = taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().UUID.getKey()).getValue();
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
			for (JsonElement entry : json.getAsJsonObject().get("data") //$NON-NLS-1$
					.getAsJsonArray()) {
				JsonObject entryObject = (JsonObject) entry.getAsJsonObject();
				JsonObject relationships = (JsonObject) entryObject.get("relationships").getAsJsonObject();  //$NON-NLS-1$
				JsonObject source = (JsonObject) relationships.get("source").getAsJsonObject(); //$NON-NLS-1$
				JsonObject sourceData = source.get("data").getAsJsonObject();
				String sourceId = sourceData.get("id").getAsString(); //$NON-NLS-1$
				JsonObject target = (JsonObject) relationships.get("target").getAsJsonObject(); //$NON-NLS-1$
				JsonObject targetData = target.get("data").getAsJsonObject();
				String targetId = targetData.get("id").getAsString(); //$NON-NLS-1$
				JsonObject linkType = (JsonObject) relationships.get("link_type"); //$NON-NLS-1$
				JsonObject linkTypeData = linkType.get("data").getAsJsonObject();
				String linkTypeId = linkTypeData.get("id").getAsString();
				String link = ""; //$NON-NLS-1$
				String otherId = ""; //$NON-NLS-1$
				if (sourceId.equals(wid)) {
					link = space.getWorkItemLinkTypes().get(linkTypeId).getAttributes().getForwardName();
					otherId = targetId;
				} else {
					link = space.getWorkItemLinkTypes().get(linkTypeId).getAttributes().getReverseName();
					otherId = sourceId;
				}
				TaskAttribute attribute = taskData.getRoot()
						.createAttribute(OSIORestTaskSchema.PREFIX_LINK + i);
				TaskAttributeMetaData metadata = attribute.getMetaData();
				metadata.putValue("type", link);
				metadata.putValue("otherId", otherId);
				OSIORestWorkItem workitem = null;
				try {
					workitem = new OSIORestGetWorkItem(client, otherId).run(new NullOperationMonitor());
				} catch (OSIORestException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} //$NON-NLS-1$
				if (workitem != null) {
					metadata.putValue("otherTitle", workitem.getTitle()); //$NON-NLS-1$
					metadata.putValue("otherNumber", workitem.getNumber()); //$NON-NLS-1$
				}
				response.add(attribute);
			}
			return response;
		}

	}

}