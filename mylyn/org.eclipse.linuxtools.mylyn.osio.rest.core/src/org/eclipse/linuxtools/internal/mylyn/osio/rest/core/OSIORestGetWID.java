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

import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

public class OSIORestGetWID extends OSIORestGetRequest<String> {
	
	private final TaskRepository repository;
	@SuppressWarnings("restriction")
	private final CommonHttpClient client;

	public OSIORestGetWID(@SuppressWarnings("restriction") CommonHttpClient client, String query, TaskRepository repository) {
		super(client, query, null, false);
		this.repository = repository;
		this.client = client;
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
			// following should never get called as we should get OSIORestResourceMovedPermanentlyException
			JsonObject data = json.getAsJsonObject().get("data").getAsJsonObject(); //$NON-NLS-1$
			String response = data.get("id").getAsString();
			return response;
		}

	}

}