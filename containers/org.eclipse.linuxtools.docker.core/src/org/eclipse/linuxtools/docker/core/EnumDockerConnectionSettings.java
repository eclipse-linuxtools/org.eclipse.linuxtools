/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat.
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
 * Constants to store/retrieve connection settings
 * 
 * @author xcoulon
 *
 */
public enum EnumDockerConnectionSettings {

	BINDING_MODE, UNIX_SOCKET, TCP_CONNECTION, UNIX_SOCKET_PATH, TCP_HOST, TCP_TLS_VERIFY, TCP_CERT_PATH;
}
