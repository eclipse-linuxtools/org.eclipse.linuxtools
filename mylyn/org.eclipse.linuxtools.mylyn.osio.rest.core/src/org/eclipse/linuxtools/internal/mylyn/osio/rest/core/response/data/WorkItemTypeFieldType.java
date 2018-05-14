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

public class WorkItemTypeFieldType {
	
	private String kind;
	
	private String baseType;
	
	private String componentType;
	
	private String[] values;
	
	// for testing purposes only
	public WorkItemTypeFieldType (String kind, String baseType, String componentType,
			String[] values) {
		this.kind = kind;
		this.baseType = baseType;
		this.componentType = componentType;
		this.values = values;
	}
	
	public String getKind() {
		return kind;
	}
	
	public String getBaseType() {
		return baseType;
	}
	
	public String getComponentType() {
		return componentType;
	}
	
	public String[] getValues() {
		return values;
	}

}
