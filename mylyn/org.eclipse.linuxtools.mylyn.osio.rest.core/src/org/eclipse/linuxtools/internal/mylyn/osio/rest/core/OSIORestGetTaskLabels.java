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

import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Space;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("restriction")
public class OSIORestGetTaskLabels extends OSIORestGetRequest<TaskAttribute> {
	
	private final TaskData taskData;
	public OSIORestGetTaskLabels(CommonHttpClient client, Space space, TaskData taskData) {
		super(client, "/workitems/" + taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().UUID.getKey()).getValue() + "/labels", null); //$NON-NLS-1$ //$NON-NLS-2$
		this.taskData = taskData;
		taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().UUID.getKey()).getValue();
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
			TaskAttribute response = taskData.getRoot().getAttribute(taskSchema.LABELS.getKey());
			response.clearValues();
			if (json.getAsJsonObject().get("data") != null) {
				for (JsonElement entry : json.getAsJsonObject().get("data") //$NON-NLS-1$
						.getAsJsonArray()) {
					JsonObject entryObject = (JsonObject) entry.getAsJsonObject();
					JsonObject attributes = (JsonObject) entryObject.get("attributes").getAsJsonObject();
					String name = attributes.getAsJsonObject().get("name").getAsString(); //$NON-NLS-1$
					response.addValue(name);
				}
			}
			return response;
		}

	}

}