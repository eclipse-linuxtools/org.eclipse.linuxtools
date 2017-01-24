/*******************************************************************************
 * Copyright (c) 2017 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.docker.reddeer.ui.resources;

/**
 * 
 * @author mlabuda@redhat.com, jkopriva@redhat.com
 *
 */
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