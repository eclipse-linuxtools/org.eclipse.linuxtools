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

public class WorkItemLinkCategoryAttributes {
	
	private String name;
	
	private String description;
	
	private int version;
	
	// for testing purposes only
	public WorkItemLinkCategoryAttributes (String name, String description, int version) {
		this.name = name;
		this.description = description;
		this.version = version;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getVersion() {
		return version;
	}

}
