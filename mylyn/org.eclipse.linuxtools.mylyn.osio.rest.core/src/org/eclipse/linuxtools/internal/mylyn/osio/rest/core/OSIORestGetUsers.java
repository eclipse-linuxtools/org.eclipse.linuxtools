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

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
import org.eclipse.mylyn.tasks.core.TaskRepository;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("restriction")
public class OSIORestGetUsers extends OSIORestGetRequest<List<OSIORestUser>>{

	public OSIORestGetUsers(CommonHttpClient client, TaskRepository repository) {
		super(client, "/users/" + //$NON-NLS-1$
				repository.getProperty(IOSIORestConstants.REPOSITORY_AUTH_ID), 
				null, true, true);
	}

	@Override
	protected List<OSIORestUser> parseFromJson(InputStreamReader in) {
		TypeToken<List<OSIORestUser>> type = new TypeToken<List<OSIORestUser>>() {
		};
		return new GsonBuilder().registerTypeAdapter(type.getType(), new JSonTaskDataDeserializer())
				.create()
				.fromJson(in, type.getType());
	}

	private class JSonTaskDataDeserializer implements JsonDeserializer<List<OSIORestUser>> {


		@Override
		public List<OSIORestUser> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			ArrayList<OSIORestUser> response = new ArrayList<OSIORestUser>();
			for (JsonElement dataElement : json.getAsJsonObject().get("data").getAsJsonArray()) { //$NON-NLS-1$

				JsonObject data = (JsonObject)dataElement.getAsJsonObject(); //$NON-NLS-1$
				JsonObject attributes = (JsonObject)data.get("attributes"); //$NON-NLS-1$

				OSIORestUser user = new OSIORestUser();

				String userId = attributes.get("userId").getAsString(); //$NON-NLS-1$
				user.setUserID(userId);

				String identityId = attributes.get("identityId").getAsString(); //$NON-NLS-1$
				user.setIdentityID(identityId);

				String fullName = attributes.get("fullName").getAsString(); //$NON-NLS-1$
				user.setFullName(fullName);

				String email = attributes.get("email").getAsString(); //$NON-NLS-1$
				user.setEmail(email);

				String imageURL = attributes.get("imageURL").getAsString(); //$NON-NLS-1$
				user.setImageURL(imageURL);

				String company = attributes.get("company").getAsString(); //$NON-NLS-1$
				user.setCompany(company);

				String username = attributes.get("username").getAsString(); //$NON-NLS-1$
				user.setUsername(username);

				String bio = attributes.get("bio").getAsString(); //$NON-NLS-1$
				user.setBio(bio);

				String providerType = attributes.get("providerType").getAsString(); //$NON-NLS-1$
				user.setProviderType(providerType);

				Boolean registrationCompleted = attributes.get("registrationCompleted").getAsBoolean();
				user.setRegistrationCompleted(registrationCompleted);

				String created_at = attributes.get("created-at").getAsString(); //$NON-NLS-1$
				user.setCreated_at(created_at);

				String updated_at = attributes.get("updated-at").getAsString(); //$NON-NLS-1$
				user.setUpdated_at(updated_at);
				
				response.add(user);
			}

			return response;
		}
	}
}
