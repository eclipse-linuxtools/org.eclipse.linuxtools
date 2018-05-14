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

public class GenericLinksForSpace {
	
	private String self;
	
	private String related;
	
	private String workitemtypes;
	
	private String workitemlinktypes;
	
	private String collaborators;
	
	private String filters;
	
	private String workitemtypegroups;
	
	private BacklogGenericLinkType backlog;
	
	// for testing purposes only
	public GenericLinksForSpace (String self, String related, String workitemtypes,
			String workitemlinktypes, String collaborators, String filters,
			String workitemtypegroups, BacklogGenericLinkType backlog) {
		this.self = self;
		this.related = related;
		this.workitemtypes = workitemtypes;
		this.workitemlinktypes = workitemlinktypes;
		this.collaborators = collaborators;
		this.filters = filters;
		this.workitemtypegroups = workitemtypegroups;
		this.backlog = backlog;
	}
	
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
