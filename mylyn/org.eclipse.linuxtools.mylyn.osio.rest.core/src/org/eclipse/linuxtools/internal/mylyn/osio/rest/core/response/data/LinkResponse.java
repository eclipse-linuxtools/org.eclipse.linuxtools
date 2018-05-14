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

public class LinkResponse implements SingleRestResponse<WorkItemLinkData> {
	private WorkItemLinkData data;
	
	// for testing purposes only
	public LinkResponse (WorkItemLinkData data) {
		this.data = data;
	}
	
	@Override
	public WorkItemLinkData getData() {
		return data;
	}
	
}
