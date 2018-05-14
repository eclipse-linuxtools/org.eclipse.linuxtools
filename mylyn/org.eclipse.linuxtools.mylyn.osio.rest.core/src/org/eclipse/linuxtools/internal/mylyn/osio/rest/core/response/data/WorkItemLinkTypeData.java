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

public class WorkItemLinkTypeData implements IdNamed {
	
	private String type;
	
	private String id;
	
	private WorkItemLinkTypeAttributes attributes;
	
	private WorkItemLinkTypeRelationships relationships;
	
	private GenericLinks links;
	
	// for testing purposes only
	public WorkItemLinkTypeData (String id, String type, WorkItemLinkTypeAttributes attributes,
			WorkItemLinkTypeRelationships relationships, GenericLinks links) {
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
		if (attributes.getTopology().equals("tree")) { //$NON-NLS-1$
			return attributes.getName() + "for Tree"; //$NON-NLS-1$
		}
		return attributes.getName();
	}
	
	public String getId() {
		return id;
	}
	
	public WorkItemLinkTypeAttributes getAttributes() {
		return attributes;
	}
	
	public WorkItemLinkTypeRelationships getRelationships() {
		return relationships;
	}
	
	public GenericLinks getLinks() {
		return links;
	}

}
