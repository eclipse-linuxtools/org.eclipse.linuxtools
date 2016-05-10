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

import java.util.Map;

public interface IDockerNetwork {

	public String name();

	public String id();

	public String scope();

	public String driver();

	public Map<String, String> options();

	public Map<String, IDockerNetworkContainer> containers();

	public IDockerIpam ipam();

}
