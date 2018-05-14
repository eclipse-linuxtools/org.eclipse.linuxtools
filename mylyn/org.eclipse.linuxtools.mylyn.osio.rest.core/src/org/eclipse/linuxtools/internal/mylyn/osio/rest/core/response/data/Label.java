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

public class Label implements IdNamed {
	
	private String type;
	
	private String id;
	
	private LabelAttributes attributes;
	
	private LabelRelationships relationships;
	
	private GenericLinks links;
	
	// for testing purposes only
	public Label (String type, String id, LabelAttributes attributes,
			LabelRelationships relationships, GenericLinks links) {
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
	
	public LabelAttributes getAttributes() {
		return attributes;
	}
	
	public LabelRelationships getRelationships() {
		return relationships;
	}
	
	public GenericLinks getLinks() {
		return links;
	}

}
