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

public class SpaceResponse implements RestResponse<Space> {
	private Space[] data;
	
	private PagingLinks links;
	
	private SpaceListMeta meta;
	
	// for testing purposes only
	public SpaceResponse (Space[] data, PagingLinks links, SpaceListMeta meta) {
		this.data = data;
		this.links = links;
		this.meta = meta;
	}

	@Override
	public Space[] getArray() {
		return data;
	}
	
	public PagingLinks getLinks() {
		return links;
	}
	
	public SpaceListMeta getMeta() {
		return meta;
	}
}
