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

public class SpaceListData implements IdNamed {
	
	private SpaceAttributes attributes;
	
	private String id;
	
	private GenericLinksForSpace spaceLinks;
	
	private SpaceRelationships relationships;
	
	private PagingLinks pagingLinks;
	
	private SpaceListMeta spaceListMeta;
	
	public String getId() {
		return id; 
	}
	
	public String getName() {
		return attributes.getName();
	}
	
	public GenericLinksForSpace getSpaceLinks() {
		return spaceLinks;
	}
	
	public SpaceRelationships getRelationships() {
		return relationships;
	}
	
	public PagingLinks getPagingLinks() {
		return pagingLinks;
	}
	
	public SpaceListMeta getSpaceListMeta() {
		return spaceListMeta;
	}

}
