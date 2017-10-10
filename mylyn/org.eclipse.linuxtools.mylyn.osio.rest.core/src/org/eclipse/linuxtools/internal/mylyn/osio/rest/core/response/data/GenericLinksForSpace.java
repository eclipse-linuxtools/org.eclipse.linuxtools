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

public class GenericLinksForSpace {
	
	private String self;
	
	private String related;
	
	private String workitemtypes;
	
	private String workitemlinktypes;
	
	private String collaborators;
	
	private String filters;
	
	private String workitemtypegroups;
	
	private BacklogGenericLinkType backlog;
	
	public String getSelf() {
		return self;
	}
	
	public String getRelated() {
		return related;
	}
	
	public String getWorkItemTypes() {
		return workitemtypes;
	}

	public String getWorkItemLinkTypes() {
		return workitemlinktypes;
	}
	
	public String getCollaborators() {
		return collaborators;
	}
	
	public String getFilters() {
		return filters;
	}
	
	public String getWorkItemTypeGroups() {
		return workitemtypegroups;
	}
	
	public BacklogGenericLinkType getBacklog() {
		return backlog;
	}
}
