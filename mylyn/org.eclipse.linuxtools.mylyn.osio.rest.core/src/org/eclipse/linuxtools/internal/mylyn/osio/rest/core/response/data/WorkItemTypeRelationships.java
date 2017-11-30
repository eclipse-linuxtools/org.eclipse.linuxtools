/*******************************************************************************
 * Copyright (c) 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data;

public class WorkItemTypeRelationships {
	
	private RelationSpaces space;
	
	private RelationGenericList assignees;
	
	private RelationGeneric creator;
	
	private RelationBaseType baseType;
	
	private RelationGeneric comments;
	
	private RelationGeneric iteration;
	
	private RelationGeneric area;
	
	private RelationGeneric children;
	
	// for testing purposes only
	public WorkItemTypeRelationships (RelationSpaces space, RelationGenericList assignees,
			RelationGeneric creator, RelationBaseType baseType, RelationGeneric comments,
			RelationGeneric iteration, RelationGeneric area, RelationGeneric children) {
		this.space = space;
		this.assignees = assignees;
		this.creator = creator;
		this.baseType = baseType;
		this.comments = comments;
		this.iteration = iteration;
		this.area = area;
		this.children = children;
	}
	
	public RelationSpaces getSpace() {
		return space;
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
	
}
