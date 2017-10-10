/*******************************************************************************
 * Copyright (c) 2013, 2017 Frank Becker and others.
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

import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;

import com.google.gson.reflect.TypeToken;

public class OSIORestUnauthenticatedGetRequest<T> extends OSIORestGetRequest<T> {

	public OSIORestUnauthenticatedGetRequest(CommonHttpClient client, String urlSuffix, TypeToken<?> responseType) {
		super(client, urlSuffix, responseType, false);
	}

}