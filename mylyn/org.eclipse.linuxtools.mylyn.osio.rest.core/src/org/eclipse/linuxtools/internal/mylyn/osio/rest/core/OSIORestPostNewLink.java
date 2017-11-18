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
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.LinkResponse;
import org.eclipse.mylyn.commons.core.operations.IOperationMonitor;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpResponse;

import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

@SuppressWarnings("restriction")
public class OSIORestPostNewLink extends OSIORestPostRequest<LinkResponse> {

	private final String linkid;
	private final String sourceid;
	private final String targetid;
	private final boolean isForward;

	class TaskAttributeTypeAdapter extends TypeAdapter<String> {

		public TaskAttributeTypeAdapter() {
			super();
		}

		@Override
		public void write(JsonWriter out, String requestEntity) throws IOException {
			out.beginObject();
			out.name("data"); //$NON-NLS-1$
			out.beginObject();
			out.name("relationships"); //$NON-NLS-1$
			out.beginObject();
			out.name("link_type"); //$NON-NLS-1$
			out.beginObject();
			out.name("data"); //$NON-NLS-1$
			out.beginObject();
			out.name("id").value(linkid); //$NON-NLS-1$
			out.name("type").value("workitemlinktypes"); //$NON-NLS-1$ //$NON-NLS-2$
			out.endObject();
			out.endObject(); // end link_type
			out.name("source"); //$NON-NLS-1$
			out.beginObject();
			out.name("data"); //$NON-NLS-1$
			out.beginObject();
			if (isForward) {
				out.name("id").value(sourceid); //$NON-NLS-1$
			} else {
				out.name("id").value(targetid); //$NON-NLS-1$
			}
			out.name("type").value("workitems"); //$NON-NLS-1$ //$NON-NLS-2$
			out.endObject();
			out.endObject(); // end source
			out.name("target"); //$NON-NLS-1$
			out.beginObject();
			out.name("data"); //$NON-NLS-1$
			out.beginObject();
			if (isForward) {
				out.name("id").value(targetid); //$NON-NLS-1$
			} else {
				out.name("id").value(sourceid); //$NON-NLS-1$
			}
			out.name("type").value("workitems"); //$NON-NLS-1$ //$NON-NLS-2$
			out.endObject();
			out.endObject(); // end target
			out.endObject(); // end relationships
			out.name("type").value("workitemlinks"); //$NON-NLS-1$ //$NON-NLS-2$
			out.endObject(); // end data
			out.name("included").beginArray(); //$NON-NLS-1$
			out.value(false);
			out.value(false);
			out.value(false);
			out.endArray();
			out.endObject();
		}

		@Override
		public String read(JsonReader in) throws IOException {
			throw new UnsupportedOperationException(
					"TaskAttributeTypeAdapter in OSIORestNewLabelTask only supports write"); //$NON-NLS-1$
		}

	}
	
	public OSIORestPostNewLink(CommonHttpClient client, String linkid, String sourceid, String targetid, boolean isForward) {
		super(client, "/workitems/" + //$NON-NLS-1$ 
				(isForward ? sourceid : targetid) + 
				"/relationships/links", true); //$NON-NLS-1$
		this.linkid = linkid;
		this.sourceid = sourceid;
		this.targetid = targetid;
		this.isForward = isForward;
	}

	List<NameValuePair> requestParameters;

	@Override
	protected void addHttpRequestEntities(HttpRequestBase request) throws OSIORestException {
		super.addHttpRequestEntities(request);
		try {
			Gson gson = new GsonBuilder()
					.registerTypeAdapter(String.class, new TaskAttributeTypeAdapter())
					.create();
			StringEntity requestEntity = new StringEntity(gson.toJson("unused")); //$NON-NLS-1$
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
	protected void doValidate(CommonHttpResponse response, IOperationMonitor monitor)
			throws IOException, OSIORestException {
		validate(response, HttpStatus.SC_CREATED, monitor);
	}

	@Override
	protected LinkResponse parseFromJson(InputStreamReader in) throws OSIORestException {
		return new Gson().fromJson(in, new TypeToken<LinkResponse>() {}.getType());
	}


}