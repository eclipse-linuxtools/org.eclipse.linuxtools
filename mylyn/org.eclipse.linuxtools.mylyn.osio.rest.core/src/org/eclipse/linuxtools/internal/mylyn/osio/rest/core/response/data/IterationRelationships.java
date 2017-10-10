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

public class IterationRelationships {

	private RelationGeneric space;
	
	private RelationGeneric parent;
	
	private RelationGeneric workitems;
	
	public RelationGeneric getSpace() {
		return space;
	}
	
	public RelationGeneric getParent() {
		return parent;
	}
	
	public RelationGeneric getWorkItems() {
		return workitems;
	}
	
}
