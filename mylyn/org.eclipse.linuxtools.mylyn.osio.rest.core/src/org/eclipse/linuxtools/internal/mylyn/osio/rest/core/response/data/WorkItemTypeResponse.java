/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat.
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

public class WorkItemTypeResponse implements RestResponse<WorkItemTypeData> {
	private WorkItemTypeData[] data;
	
	// for testing purposes only
	public WorkItemTypeResponse (WorkItemTypeData[] data) {
		this.data = data;
	}

	@Override
	public WorkItemTypeData[] getArray() {
		return data;
	}
}
