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

public class FieldType {
	
	private String kind;
	
	private String componentType;
	
	private String baseType;
	
	private String[] values;
	
	// for testing purposes only
	public FieldType (String kind, String componentType, String baseType, String[] values) {
		this.kind = kind;
		this.componentType = componentType;
		this.baseType = baseType;
		this.values = values;
	}
	
	public String getKind() {
		return kind;
	}
	
	public String getComponentType() {
		return componentType;
	}
	
	public String getBaseType() {
		return baseType;
	}
	
	public String[] getValues() {
		return values;
	}

}
