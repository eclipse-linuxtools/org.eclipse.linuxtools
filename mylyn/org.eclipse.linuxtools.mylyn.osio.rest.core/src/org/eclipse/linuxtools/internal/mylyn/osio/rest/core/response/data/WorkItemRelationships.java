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

public class WorkItemRelationships {
	
	private RelationGenericList assignees;
	
	private RelationGeneric creator;
	
	private RelationBaseType baseType;
	
	private RelationGeneric comments;
	
	private RelationGeneric iteration;
	
	private RelationGeneric area;
	
	private RelationGeneric children;
	
	private RelationSpaces space;
	
	// for testing purposes only
	public WorkItemRelationships (RelationGenericList assignees, RelationGeneric creator,
			RelationBaseType baseType, RelationGeneric comments, RelationGeneric iteration,
			RelationGeneric area, RelationGeneric children, RelationSpaces space) {
		this.assignees = assignees;
		this.creator = creator;
		this.baseType = baseType;
		this.comments = comments;
		this.iteration = iteration;
		this.area = area;
		this.children = children;
		this.space = space;
	}
	
	public RelationGenericList getAssignees() {
		return assignees;
	}
	
	public RelationGeneric getCreator() {
		return creator;
	}
	
	public RelationBaseType getBaseType() {
		return baseType;
	}
	
	public RelationGeneric getComments() {
		return comments;
	}
	
	public RelationGeneric getIteration() {
		return iteration;
	}
	
	public RelationGeneric getArea() {
		return area;
	}
	
	public RelationGeneric getChildren() {
		return children;
	}
	
	public RelationSpaces getSpace() {
		return space;
	}
	
}
