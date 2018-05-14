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

public class RelationWorkItemLinkCategoryData {
	
	private String type;
	
	private String id;
	
	private WorkItemLinkCategoryAttributes attributes;
	
	private GenericLinks links;
	
	// for testing purposes only
	public RelationWorkItemLinkCategoryData (String id, String type,
			WorkItemLinkCategoryAttributes attributes, GenericLinks links) {
		this.id = id;
		this.type = type;
		this.attributes = attributes;
		this.links = links;
	}
	
	public String getType() {
		return type;
	}
	
	public String getId() {
		return id;
	}
	
	public WorkItemLinkCategoryAttributes getAttributes() {
		return attributes;
	}
	
	public GenericLinks getLinks() {
		return links;
	}

}
