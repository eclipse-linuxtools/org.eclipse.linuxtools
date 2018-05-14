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

package org.eclipse.linuxtools.docker.core;

/**
 * The state of a given connection
 * 
 * @since 2.0.0
 */
public enum EnumDockerConnectionState {

	/** unknown connection state. */
	UNKNOWN,
	/** established connection: messages can be sent and received. */
	ESTABLISHED,
	/** closed connection, host may not be reachable. */
	CLOSED;

}
