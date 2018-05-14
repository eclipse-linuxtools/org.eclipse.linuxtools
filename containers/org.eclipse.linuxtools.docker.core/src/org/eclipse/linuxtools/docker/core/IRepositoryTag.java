/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
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

package org.eclipse.linuxtools.docker.core;

/**
 * A tag (or version) for a given repository.
 */
public interface IRepositoryTag {

	/**
	 * @return Name of the tag.
	 */
	String getName();

	/**
	 * @return The corresponding image layer for this specific tag
	 */
	String getLayer();

}
