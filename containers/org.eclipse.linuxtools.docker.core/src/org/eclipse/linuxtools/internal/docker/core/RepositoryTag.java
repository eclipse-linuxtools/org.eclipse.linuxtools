/*******************************************************************************
 * Copyright (c) 2014, 2017 Red Hat.
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
import com.google.common.base.MoreObjects;

/**
 * Repository tag retrieved from Docker Registry version 0.6.3
 *
 */
@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE, setterVisibility = NONE)
public class RepositoryTag
		implements IRepositoryTag, Comparable<IRepositoryTag> {

	@JsonProperty("layer") //$NON-NLS-1$
	private String layer;
	@JsonProperty("name") //$NON-NLS-1$
	private String name;

	/**
	 * Default constructor
	 */
	public RepositoryTag() {
	}

	/**
	 * Full constructor.
	 * @param name
	 *            the repository name
	 * @param layer
	 *            the layer
	 */
	public RepositoryTag(final String name, final String layer) {
		this.layer = layer;
		this.name = name;
	}

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
		return MoreObjects.toStringHelper(this).add("name", getName()) //$NON-NLS-1$
				.add("layer", getLayer()).toString(); //$NON-NLS-1$
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((layer == null) ? 0 : layer.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		RepositoryTag other = (RepositoryTag) obj;
		if (layer == null) {
			if (other.layer != null) {
				return false;
			}
		} else if (!layer.equals(other.layer)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	/**
	 * Compares by the tag 'name' in reverse order.
	 */
	@Override
	public int compareTo(final IRepositoryTag other) {
		// TODO: put this in IRepositoryTag in the next major release.
		// tries to compare versions in the x.y.z.qualifer format, otherwise,
		// just do a lexicographical comparison
		try {
			final String[] thisParts = this.getName().split("\\."); //$NON-NLS-1$
			final String[] thatParts = other.getName().split("\\."); //$NON-NLS-1$
			int length = Math.max(thisParts.length, thatParts.length);
			for (int i = 0; i < length; i++) {
				int thisPart = i < thisParts.length
						? Integer.parseInt(thisParts[i]) : 0;
				int thatPart = i < thatParts.length
						? Integer.parseInt(thatParts[i]) : 0;
				if (thisPart < thatPart) {
					return 1;
				}
				if (thisPart > thatPart) {
					return -1;
				}
			}
			return 0;
		} catch (NumberFormatException e) {
			// if one of the name was not a valid version, just do this:
			return other.getName().compareTo(this.getName());
		}
	}

}
