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

public class BacklogGenericLinkType {
	
	private String self;
	
	private BacklogLinkMeta meta;
	
	// for testing purposes only
	public BacklogGenericLinkType (String self, BacklogLinkMeta meta) {
		this.self = self;
		this.meta = meta;
	}
	
	public String getSelf() {
		return self;
	}
	
	public BacklogLinkMeta getMeta() {
		return meta;
	}

}
