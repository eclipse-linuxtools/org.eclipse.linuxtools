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
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Space;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.User;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
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

@SuppressWarnings("restriction")
public class OSIORestGetTaskLinks extends OSIORestGetRequest<TaskAttribute> {
	
	private final TaskData taskData;
	private final Space space;
	private final String wid;
	private final OSIORestConfiguration config;
	private final OSIORestClient osioClient;
	
	private final CommonHttpClient client;

	public OSIORestGetTaskLinks(CommonHttpClient client, OSIORestClient osioClient, Space space, TaskData taskData, OSIORestConfiguration config) {
		super(client, "/workitems/" + taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().UUID.getKey()).getValue() + "/relationships/links", null); //$NON-NLS-1$ //$NON-NLS-2$
		this.taskData = taskData;
		this.space = space;
		this.wid = taskData.getRoot().getAttribute(OSIORestTaskSchema.getDefault().UUID.getKey()).getValue();
		this.client = client;
		this.osioClient = osioClient;
		this.config = config;
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
			TaskAttribute response = taskData.getRoot()
					.getAttribute(OSIORestTaskSchema.getDefault().LINKS.getKey());
			response.clearValues();
			for (JsonElement entry : json.getAsJsonObject().get("data") //$NON-NLS-1$
					.getAsJsonArray()) {
				JsonObject entryObject = (JsonObject) entry.getAsJsonObject();
				String id = entryObject.get("id").getAsString(); //$NON-NLS-1$
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
				String otherId = targetId;
				if (sourceId.equals(wid)) {
					link = space.getWorkItemLinkTypes().get(linkTypeId).getAttributes().getForwardName();
				} else {
					link = space.getWorkItemLinkTypes().get(linkTypeId).getAttributes().getReverseName();
					otherId = sourceId;
				}
				OSIORestWorkItem workitem = null;
				try {
					workitem = new OSIORestGetWorkItem(client, otherId).run(new NullOperationMonitor());
				} catch (OSIORestException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} //$NON-NLS-1$
				String otherWorkItem = OSIORestMessages.getString("UnknownName.msg"); //$NON-NLS-1$
				boolean haveOtherWorkItemName = false;
				if (workitem != null) {
					String spaceId = workitem.getSpaceId();
					Map<String, Space> spaces = config.getSpaces();
					if (spaces != null) {
						for (Space s : spaces.values()) {
							if (s.getId().equals(spaceId)) {
								User owner = null;
								try {
									owner = osioClient.getOwnedByLink(new NullOperationMonitor(), s);
								} catch (OSIORestException e1) {
									com.google.common.base.Throwables.propagate(
											new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN,
													"Can not get owner of Space (" + spaceId + ")"))); //$NON-NLS-1$ //$NON-NLS-2$
								}				
								
								int number = Integer.parseInt(workitem.getNumber());
								otherWorkItem = owner.getName() + "/" + s.getName() + "#" + number; //$NON-NLS-1$ //$NON-NLS-2$
								haveOtherWorkItemName = true;
							    break;
							}
						}
					}
					if (!haveOtherWorkItemName) {
						Map<String, Space> externalSpaces = config.getExternalSpaces();
						if (spaces != null) {
							for (Space s : externalSpaces.values()) {
								if (s.getId().equals(spaceId)) {
									User owner = null;
									try {
										owner = osioClient.getOwnedByLink(new NullOperationMonitor(), s);
									} catch (OSIORestException e1) {
										com.google.common.base.Throwables.propagate(
												new CoreException(new Status(IStatus.ERROR, OSIORestCore.ID_PLUGIN,
														"Can not get owner of Space (" + spaceId + ")"))); //$NON-NLS-1$ //$NON-NLS-2$
									}				
									
									int number = Integer.parseInt(workitem.getNumber());
									otherWorkItem = owner.getName() + "/" + s.getName() + "#" + number; //$NON-NLS-1$ //$NON-NLS-2$
								    break;
								}
							}
						}

					}
 				}
				link += " " + workitem.getTitle() + " [" //$NON-NLS-1$ //$NON-NLS-2$ 
						+ otherWorkItem + "]"; //$NON-NLS-1$
				response.addValue(link);
				TaskAttributeMetaData metadata = response.getMetaData();
				metadata.putValue(link, id);
			}
			return response;
		}

	}

}