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

public class RelationWorkItemData {
	
	private String type;
	
	private String id;
	
	// for testing purposes only
	public RelationWorkItemData (String id, String type) {
		this.id = id;
		this.type = type;
	}
	
	public String getType() {
		return type;
	}
	
	public String getId() {
		return id;
	}

}
