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

public class WorkItemLinkTypeRelationships {
	
	private RelationWorkItemLinkCategory link_category;
	
	private RelationSpaces spaces;
	
	// for testing purposes only
	public WorkItemLinkTypeRelationships (RelationWorkItemLinkCategory link_category,
			RelationSpaces spaces) {
		this.link_category = link_category;
		this.spaces = spaces;
	}
	
	public RelationWorkItemLinkCategory getLinkCategory() {
		return link_category;
	}
	
	public RelationSpaces getSpaces() {
		return spaces;
	}

}
