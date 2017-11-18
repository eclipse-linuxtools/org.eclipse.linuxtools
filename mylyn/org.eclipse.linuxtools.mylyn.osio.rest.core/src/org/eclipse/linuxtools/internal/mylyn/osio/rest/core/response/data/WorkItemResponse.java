/*******************************************************************************
 * Copyright (c) 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

import java.util.Map;

public class WorkItemResponse implements IdNamed {
	
	private String type;
	
	private String id;
	
	private Map<String, Object> attributes;
	
	private WorkItemRelationships relationships;
	
	private GenericLinksForWorkItem links;
	
	public String getName() {
		return (String)attributes.get("system.title"); //$NON-NLS-1$
	}
	
	public String getType() {
		return type;
	}
	
	public String getId() {
		return id;
	}
	
	public Object getAttributes() {
		return attributes;
	}
	
	public WorkItemRelationships getRelationships() {
		return relationships;
	}
	
	public GenericLinksForWorkItem getLinks() {
		return links;
	}
	
}
