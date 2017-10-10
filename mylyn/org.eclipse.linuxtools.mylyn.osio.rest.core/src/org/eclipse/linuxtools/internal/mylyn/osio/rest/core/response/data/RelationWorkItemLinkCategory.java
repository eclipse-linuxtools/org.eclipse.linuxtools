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

public class RelationWorkItemLinkCategory {
	
	private RelationWorkItemLinkCategoryData data;
	
	private GenericLinks links;
	
	public RelationWorkItemLinkCategoryData getData() {
		return data;
	}
	
	public GenericLinks getLinks() {
		return links;
	}

}
