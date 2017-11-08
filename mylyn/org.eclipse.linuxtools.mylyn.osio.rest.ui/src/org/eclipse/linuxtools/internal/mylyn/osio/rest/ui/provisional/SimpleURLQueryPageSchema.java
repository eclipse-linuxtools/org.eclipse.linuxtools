/*******************************************************************************
 * Copyright (c) 2015 Frank Becker and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.ui.provisional;

import org.eclipse.mylyn.tasks.core.data.TaskAttribute;

@SuppressWarnings("nls")
public class SimpleURLQueryPageSchema extends AbstractQueryPageSchema {
	private static final SimpleURLQueryPageSchema instance = new SimpleURLQueryPageSchema();

	public SimpleURLQueryPageSchema() {
	}

	public static SimpleURLQueryPageSchema getInstance() {
		return instance;
	}

	public final Field wholeUrl = createField("wholeQueryURL", "URL:", TaskAttribute.TYPE_URL);
}
