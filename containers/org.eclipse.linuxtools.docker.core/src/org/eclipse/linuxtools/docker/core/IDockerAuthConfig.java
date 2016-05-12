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
 * Docker Authorization Config
 * 
 * Used to authorize actions for a registry
 * 
 * @author jjohnstn
 *
 */
public interface IDockerAuthConfig {

	/**
	 * Get username
	 * 
	 * @return user name
	 */
	public char[] username();

	/**
	 * Get password
	 * 
	 * @return password
	 */
	public char[] password();

	/**
	 * Get email
	 * 
	 * @return email
	 */
	public char[] email();

	/**
	 * Get server address
	 * 
	 * @return server address
	 */
	public char[] serverAddress();

}
