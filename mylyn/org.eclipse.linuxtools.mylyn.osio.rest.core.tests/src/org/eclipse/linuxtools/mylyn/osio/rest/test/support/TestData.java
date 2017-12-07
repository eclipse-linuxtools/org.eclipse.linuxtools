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
package org.eclipse.linuxtools.mylyn.osio.rest.test.support;

import java.util.Map;

import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.Space;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.SpaceResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemLinkTypeResponse;
import org.eclipse.linuxtools.internal.mylyn.osio.rest.core.response.data.WorkItemTypeResponse;

public class TestData {
	
	public SpaceResponse spaces;

	public SpaceResponse externalspaces;

	public Map<String, Space> spaceMap;
	
	public WorkItemTypeResponse defaultWorkItemTypeResponse;
	
	public WorkItemLinkTypeResponse defaultWorkItemLinkTypeResponse;
	
}
