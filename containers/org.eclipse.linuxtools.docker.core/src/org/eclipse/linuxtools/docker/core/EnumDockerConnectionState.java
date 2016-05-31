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
