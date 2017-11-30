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
