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
