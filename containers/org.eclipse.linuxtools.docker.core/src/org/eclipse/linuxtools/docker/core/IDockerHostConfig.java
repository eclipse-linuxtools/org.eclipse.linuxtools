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

	/**
	 * @since 3.0
	 */
	List<String> securityOpt();

	String networkMode();

}
