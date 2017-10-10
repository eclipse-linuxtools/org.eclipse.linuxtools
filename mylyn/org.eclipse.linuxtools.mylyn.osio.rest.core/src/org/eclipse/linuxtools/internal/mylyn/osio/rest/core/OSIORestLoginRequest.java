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

import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.LoginToken;
import org.eclipse.mylyn.commons.repositories.http.core.CommonHttpClient;

import com.google.gson.reflect.TypeToken;

public class OSIORestLoginRequest extends OSIORestGetRequest<LoginToken> {

	public OSIORestLoginRequest(CommonHttpClient client) {
		super(client, "/login?", new TypeToken<LoginToken>() { //$NON-NLS-1$
		});
	}
}
