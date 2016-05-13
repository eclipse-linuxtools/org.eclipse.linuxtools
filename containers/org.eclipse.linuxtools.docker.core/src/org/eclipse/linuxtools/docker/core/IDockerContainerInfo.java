/*******************************************************************************
 * Copyright (c) 2015, 2016 Red Hat.
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

	String id();

	Date created();

	String path();

	List<String> args();

	IDockerContainerConfig config();

	IDockerHostConfig hostConfig();

	IDockerContainerState state();

	String image();

	IDockerNetworkSettings networkSettings();

	String resolvConfPath();

	String hostnamePath();

	String hostsPath();

	String name();

	String driver();

	String execDriver();

	String processLabel();

	String mountLabel();

	Map<String, String> volumes();

	Map<String, Boolean> volumesRW();

	@Override
	boolean equals(Object o);

	@Override
	int hashCode();

	@Override
	String toString();

}