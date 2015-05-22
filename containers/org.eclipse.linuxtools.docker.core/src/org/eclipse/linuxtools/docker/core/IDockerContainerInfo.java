/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.docker.core;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IDockerContainerInfo {

	public String id();

	public Date created();

	public String path();

	public List<String> args();

	public IDockerContainerConfig config();

	public IDockerHostConfig hostConfig();

	public IDockerContainerState state();

	public String image();

	public IDockerNetworkSettings networkSettings();

	public String resolvConfPath();

	public String hostnamePath();

	public String hostsPath();

	public String name();

	public String driver();

	public String execDriver();

	public String processLabel();

	public String mountLabel();

	public Map<String, String> volumes();

	public Map<String, Boolean> volumesRW();

	@Override
	public boolean equals(Object o);

	@Override
	public int hashCode();

	@Override
	public String toString();

}