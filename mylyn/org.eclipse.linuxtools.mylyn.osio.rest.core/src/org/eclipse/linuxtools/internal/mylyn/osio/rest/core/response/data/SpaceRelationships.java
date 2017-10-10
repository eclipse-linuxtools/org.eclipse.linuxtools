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

import com.google.gson.annotations.SerializedName;

public class SpaceRelationships {
	
	@SerializedName("owned-by")
	private SpaceOwnedBy owned_by;

	private RelationGeneric iterations;
	
	private RelationGeneric areas;
	
	private RelationGeneric workitemlinktypes;
	
	private RelationGeneric workitemtypes;
	
	private RelationGeneric workitems;
	
	private RelationGeneric codebases;
	
	private RelationGeneric collaborators;
	
	private RelationGeneric labels;
	
	public SpaceOwnedBy getOwnedBy() {
		return owned_by;
	}
	
	public RelationGeneric getIterations() {
		return iterations;
	}
	
	public RelationGeneric getWorkItemLinkTypes() {
		return workitemlinktypes;
	}
	
	public RelationGeneric getWorkItemTypes() {
		return workitemtypes;
	}
	
	public RelationGeneric getWorkItems() {
		return workitems;
	}
	
	public RelationGeneric getCodebases() {
		return codebases;
	}
	
	public RelationGeneric getCollaborators() {
		return collaborators;
	}
	
	public RelationGeneric getLabels() {
		return labels;
	}
	
}
