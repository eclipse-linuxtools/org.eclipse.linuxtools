/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
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
 * Constants to store/retrieve connection settings
 * 
 * @author xcoulon
 *
 */
public enum EnumDockerConnectionSettings {

	BINDING_MODE, UNIX_SOCKET, TCP_CONNECTION, UNIX_SOCKET_PATH, TCP_HOST, TCP_TLS_VERIFY, TCP_CERT_PATH;
}
