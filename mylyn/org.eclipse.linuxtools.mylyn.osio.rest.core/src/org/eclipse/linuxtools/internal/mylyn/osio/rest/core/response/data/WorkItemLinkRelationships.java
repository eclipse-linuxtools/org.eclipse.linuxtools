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

public class WorkItemLinkRelationships {
	
	public RelationWorkItemLinkType link_type;
	
	public RelationWorkItem source;
	
	public RelationWorkItem target;
	
	// for testing purposes only
	public WorkItemLinkRelationships (RelationWorkItemLinkType link_type,
			RelationWorkItem source, RelationWorkItem target) {
		this.link_type = link_type;
		this.source = source;
		this.target = target;
	}
	
	public RelationWorkItemLinkType getLink_type() {
		return link_type;
	}
	
	public RelationWorkItem getSource() {
		return source;
	}
	
	public RelationWorkItem getTarget() {
		return target;
	}

}
