/*******************************************************************************
 * Copyright (c) 2014, 2018 Frank Becker and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Frank Becker - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

import java.io.Serializable;

public class SortableActiveEntry implements Serializable {
	private static final long serialVersionUID = 6555410907399756521L;

	private String name;

	private int sort_key;

	private boolean is_active;

	public String getName() {
		return name;
	}

	public int getSortKey() {
		return sort_key;
	}

	public boolean isActive() {
		return is_active;
	}
}