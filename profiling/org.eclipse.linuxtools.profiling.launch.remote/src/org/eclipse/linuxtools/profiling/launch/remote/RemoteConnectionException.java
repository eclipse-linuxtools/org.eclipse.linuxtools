/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Red Hat Inc. - Initial implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.profiling.launch.remote;

public class RemoteConnectionException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public RemoteConnectionException(String message) {
		super(message);
	}

}
