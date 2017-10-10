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

public class Area implements IdNamed {
	
	private String type;
	
	private String id;
	
	private AreaAttributes attributes;
	
	private AreaRelationships relationships;
	
	private GenericLinks links;
	
	public String getType() {
		return type;
	}
	
	public String getName() {
		return attributes.getName();
	}
	
	public String getId() {
		return id;
	}
	
	public AreaAttributes getAttributes() {
		return attributes;
	}
	
	public AreaRelationships getRelationships() {
		return relationships;
	}
	
	public GenericLinks getLinks() {
		return links;
	}

}
