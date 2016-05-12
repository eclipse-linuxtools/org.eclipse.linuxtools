/*******************************************************************************
 * Copyright (c) 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.docker.core;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Binding for Repository Tag results from a Registry V2.
 * 
 * Currently the returned repo tag results lack the same degree of information
 * returned from the V1 registries.
 */
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class RepositoryTagV2 {

	@JsonProperty("name") //$NON-NLS-1$
	private String name;

	@JsonProperty("tags") //$NON-NLS-1$
	private List<String> tags;

	public List<RepositoryTag> getTags() {
		List<RepositoryTag> result = new ArrayList<>();
		for (String tag : tags) {
			RepositoryTag rtag = new RepositoryTag();
			rtag.setName(tag);
			rtag.setLayer("Unknown"); //$NON-NLS-1$
			result.add(rtag);
		}
		return result;
	}

	public String getName() {
		return name;
	}

}
