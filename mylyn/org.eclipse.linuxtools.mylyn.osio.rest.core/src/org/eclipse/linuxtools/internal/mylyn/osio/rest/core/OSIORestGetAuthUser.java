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

import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

@SuppressWarnings("restriction")
public class OSIORestGetAuthUser extends OSIORestGetRequest<OSIORestUser>{

	public OSIORestGetAuthUser(CommonHttpClient client) {
		super(client, "/user", null, true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	protected OSIORestUser parseFromJson(InputStreamReader in) {
		TypeToken<OSIORestUser> type = new TypeToken<OSIORestUser>() {
		};
		return new GsonBuilder().registerTypeAdapter(type.getType(), new JSonTaskDataDeserializer())
				.create()
				.fromJson(in, type.getType());
	}

	private class JSonTaskDataDeserializer implements JsonDeserializer<OSIORestUser> {


		@Override
		public OSIORestUser deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			OSIORestUser response = new OSIORestUser();
			
			JsonObject data = ((JsonObject)json.getAsJsonObject().get("data")); //$NON-NLS-1$
			JsonObject attributes = (JsonObject)data.get("attributes"); //$NON-NLS-1$
			
			String userId = attributes.get("userID").getAsString(); //$NON-NLS-1$
			response.setUserID(userId);
			
			String identityId = attributes.get("identityID").getAsString(); //$NON-NLS-1$
			response.setIdentityID(identityId);
			
			String fullName = attributes.get("fullName").getAsString(); //$NON-NLS-1$
			response.setFullName(fullName);
			
			String email = attributes.get("email").getAsString(); //$NON-NLS-1$
			response.setEmail(email);
			
			String imageURL = attributes.get("imageURL").getAsString(); //$NON-NLS-1$
			response.setImageURL(imageURL);
			
			String company = attributes.get("company").getAsString(); //$NON-NLS-1$
			response.setCompany(company);
			
			String username = attributes.get("username").getAsString(); //$NON-NLS-1$
			response.setUsername(username);
			
			String bio = attributes.get("bio").getAsString(); //$NON-NLS-1$
			response.setBio(bio);
			
			String providerType = attributes.get("providerType").getAsString(); //$NON-NLS-1$
			response.setProviderType(providerType);
			
			Boolean registrationCompleted = attributes.get("registrationCompleted").getAsBoolean();
			response.setRegistrationCompleted(registrationCompleted);
			
			String created_at = attributes.get("created-at").getAsString(); //$NON-NLS-1$
			response.setCreated_at(created_at);
			
			String updated_at = attributes.get("updated-at").getAsString(); //$NON-NLS-1$
			response.setUpdated_at(updated_at);

			return response;
		}
	}
}
