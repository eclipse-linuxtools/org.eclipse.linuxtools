/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat Inc. and others.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class GenericLinksForWorkItem {
	
	private String self;
	
	private String related;
	
	private String editCodebase;
	
	private Object meta;
	
	// for testing purposes only
	public GenericLinksForWorkItem (String self, String related, String editCodebase,
			Object meta) {
		this.self = self;
		this.related = related;
		this.editCodebase = editCodebase;
		this.meta = meta;
	}
	
	public String getSelf() {
		return self;
	}
	
	public String getRelated() {
		return related;
	}
	
	public String getEditCodebase() {
		return editCodebase;
	}
	
	public Object getMeta() {
		return meta;
	}

}
