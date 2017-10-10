/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
