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

public class FieldDefinition {
	
	private boolean required;
	
	private FieldType fieldType;
	
	private String label;
	
	private String description;
	
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
