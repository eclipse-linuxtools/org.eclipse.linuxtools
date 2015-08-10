/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
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

import org.eclipse.linuxtools.docker.core.IRepositoryTag;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

/**
 * Repository tag retrieved from Docker Registry version 0.6.3
 *
 */
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class RepositoryTag implements IRepositoryTag {

	@JsonProperty("layer") //$NON-NLS-1$
	private String layer;
	@JsonProperty("name") //$NON-NLS-1$
	private String name;

	/**
	 * @return the layer
	 */
	@Override
	public String getLayer() {
		return layer;
	}

	/**
	 * @param layer
	 *            the layer to set
	 */
	public void setLayer(String layer) {
		this.layer = layer;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("name", getName()) //$NON-NLS-1$
				.add("layer", getLayer()).toString(); //$NON-NLS-1$
	}

}
