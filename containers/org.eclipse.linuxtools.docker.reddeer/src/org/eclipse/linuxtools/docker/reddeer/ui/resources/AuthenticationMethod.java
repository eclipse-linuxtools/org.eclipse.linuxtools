/*******************************************************************************
 * Copyright (c) 2017,2018 Red Hat, Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.docker.reddeer.ui.resources;

public enum AuthenticationMethod {
	UNIX_SOCKET("Unix socket"), TCP_CONNECTION("TCP Connection");

	private final String text;

	private AuthenticationMethod(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}