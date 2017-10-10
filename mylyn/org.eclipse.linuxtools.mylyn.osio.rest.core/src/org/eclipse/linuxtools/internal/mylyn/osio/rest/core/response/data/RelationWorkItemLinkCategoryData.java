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

public class RelationWorkItemLinkCategoryData {
	
	private String type;
	
	private String id;
	
	private WorkItemLinkCategoryAttributes attributes;
	
	private GenericLinks links;
	
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
