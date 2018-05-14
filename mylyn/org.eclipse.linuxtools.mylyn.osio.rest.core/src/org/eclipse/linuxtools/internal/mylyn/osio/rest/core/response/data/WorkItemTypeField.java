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

public class WorkItemTypeField {
	
	private String description;
	
	private String label;
	
	private Boolean required;
	
	private WorkItemTypeFieldType type;
	
	// for testing purposes only
	public WorkItemTypeField (String description, String label, Boolean required,
			WorkItemTypeFieldType type) {
		this.description = description;
		this.label = label;
		this.required = required;
		this.type = type;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getLabel() {
		return label;
	}
	
	public Boolean getRequired() {
		return required;
	}
	
	public WorkItemTypeFieldType getType() {
		return type;
	}

}