/*******************************************************************************
 * Copyright (c) 2014, 2016 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.docker.core;

import java.util.List;
import java.util.Map;

public interface IDockerHostConfig {

	List<String> binds();

	String containerIDFile();

	List<IDockerConfParameter> lxcConf();

	boolean privileged();

	Map<String, List<IDockerPortBinding>> portBindings();

	List<String> links();

	boolean publishAllPorts();

	List<String> dns();

	List<String> dnsSearch();

	List<String> volumesFrom();

	String networkMode();

}
