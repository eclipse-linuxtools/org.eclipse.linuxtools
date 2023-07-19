/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
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
package org.eclipse.linuxtools.internal.docker.core;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.linuxtools.docker.core.IRepositoryTag;

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

	public static final String UNKNOWN_LAYER = "Unknown"; //$NON-NLS-1$

	@JsonProperty("name") //$NON-NLS-1$
	private String name;

	@JsonProperty("tags") //$NON-NLS-1$
	private List<String> tags;

	public List<IRepositoryTag> getTags() {
		return tags.stream().map(t -> {
			final RepositoryTag tag = new RepositoryTag();
			tag.setName(t);
			tag.setLayer(UNKNOWN_LAYER);
			return tag;
		}).collect(Collectors.toList());
	}

	public String getName() {
		return name;
	}

}
