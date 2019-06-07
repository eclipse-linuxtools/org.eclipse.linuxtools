/*******************************************************************************
 * Copyright (c) 2019 Red Hat.
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

public interface IDockerConnection3 {

	/**
	 * Get whether the connection is finalizing or not.
	 * 
	 * @return true if connection is finalizing
	 * @since 4.3
	 */
	boolean isFinalizing();

}
