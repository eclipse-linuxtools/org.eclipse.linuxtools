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

public class SpaceListData implements IdNamed {
	
	private SpaceAttributes attributes;
	
	private String id;
	
	private GenericLinksForSpace spaceLinks;
	
	private SpaceRelationships relationships;
	
	private PagingLinks pagingLinks;
	
	private SpaceListMeta spaceListMeta;
	
	// for testing purposes only
	public SpaceListData (String id, SpaceAttributes attributes, GenericLinksForSpace spaceLinks,
			SpaceRelationships relationships, PagingLinks pagingLinks, SpaceListMeta spaceListMeta) {
		this.id = id;
		this.attributes = attributes;
		this.spaceLinks = spaceLinks;
		this.relationships = relationships;
		this.pagingLinks = pagingLinks;
		this.spaceListMeta = spaceListMeta;
	}
	
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
