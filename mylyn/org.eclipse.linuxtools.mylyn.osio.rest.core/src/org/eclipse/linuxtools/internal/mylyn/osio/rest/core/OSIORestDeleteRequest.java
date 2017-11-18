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

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpRequestBase;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;

@SuppressWarnings("restriction")
public abstract class OSIORestDeleteRequest<T> extends OSIORestRequest<T> {

	public OSIORestDeleteRequest(CommonHttpClient client, String urlSuffix, boolean authenticationRequired) {
		super(client, urlSuffix, authenticationRequired, false);
	}

	@Override
	protected HttpRequestBase createHttpRequestBase(String url) {
		HttpDelete request = new HttpDelete(url);
		request.setHeader(CONTENT_TYPE, APPLICATION_VND_JSON);
		return request;
	}

}