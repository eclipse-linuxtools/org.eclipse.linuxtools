/*******************************************************************************
 * Copyright (c) 2013, 2018 Frank Becker and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *     Red Hat Inc. - modified for use with OpenShift.io
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.core;

import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;

import com.google.gson.reflect.TypeToken;

@SuppressWarnings("restriction")
public class OSIORestUnauthenticatedGetRequest<T> extends OSIORestGetRequest<T> {

	public OSIORestUnauthenticatedGetRequest(CommonHttpClient client, String urlSuffix, TypeToken<?> responseType) {
		super(client, urlSuffix, responseType, false);
	}

}