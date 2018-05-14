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

public class WorkItemLinkData implements Named {
	
	private String type;
	
	private WorkItemLinkAttributes attributes;
	
	private WorkItemLinkRelationships relationships;
	
	private GenericLinks links;
	
	private String id;
	
	// for testing purposes only
	public WorkItemLinkData (String id, String type, WorkItemLinkAttributes attributes,
			WorkItemLinkRelationships relationships, GenericLinks links) {
		this.id = id;
		this.type = type;
		this.attributes = attributes;
		this.relationships = relationships;
		this.links = links;
	}
	
	public String getType() {
		return type;
	}
	
	public String getName() {
		return type;
	}
	
	public WorkItemLinkAttributes getAttributes() {
		return attributes;
	}
	
	public WorkItemLinkRelationships getRelationships() {
		return relationships;
	}
	
	public String getId() {
		return id;
	}
	
	public GenericLinks getLinks() {
		return links;
	}

}
