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

public class FieldDefinition {
	
	private boolean required;
	
	private FieldType fieldType;
	
	private String label;
	
	private String description;
	
	// for testing purposes only
	public FieldDefinition (String label, String description, FieldType fieldType, boolean required) {
		this.label = label;
	    this.description = description;
		this.fieldType = fieldType;
		this.required = required;
	}
	
	public boolean isRequired() {
		return required;
	}
	
	public FieldType getFieldType() {
		return fieldType;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getDescription() {
		return description;
	}

}
