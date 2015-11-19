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
 * Port mapping for {@link IDockerContainer}
 *
 */
public interface IDockerPortMapping extends Comparable<IDockerPortMapping> {

	public int getPrivatePort();
	
	public int getPublicPort();

	public String getType();

	public String getIp();

}
