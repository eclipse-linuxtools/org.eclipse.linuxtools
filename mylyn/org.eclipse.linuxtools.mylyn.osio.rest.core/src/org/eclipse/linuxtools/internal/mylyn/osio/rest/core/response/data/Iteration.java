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

public class Iteration implements IdNamed {
	
	private String type;
	
	private String id;
	
	private IterationAttributes attributes;
	
	private IterationRelationships relationships;
	
	private GenericLinks links;
	
	// for testing purposes only
	public Iteration (String id, String type, IterationAttributes attributes,
			IterationRelationships relationships, GenericLinks links) {
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
		return attributes.getName();
	}
	
	public String getId() {
		return id;
	}
	
	public IterationAttributes getAttributes() {
		return attributes;
	}
	
	public IterationRelationships getRelationships() {
		return relationships;
	}
	
	public GenericLinks getLinks() {
		return links;
	}

}
