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

public class AreaRelationships {
	
	private RelationGeneric space;
	
	private RelationGeneric parent;
	
	private RelationGeneric children;
	
	private RelationGeneric workitems;
	
	// for testing purposes only
	public AreaRelationships (RelationGeneric space, RelationGeneric parent,
			RelationGeneric children, RelationGeneric workitems) {
		this.space = space;
		this.parent = parent;
		this.children = children;
		this.workitems = workitems;
	}
	
	public RelationGeneric getSpace() {
		return space;
	}
	
	public RelationGeneric getParent() {
		return parent;
	}
	
	public RelationGeneric getChildren() {
		return children;
	}
	
	public RelationGeneric getWorkItems() {
		return workitems;
	}

}
