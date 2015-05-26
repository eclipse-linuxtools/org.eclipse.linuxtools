/*******************************************************************************
 * Copyright (c) 2014 Red Hat.
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

	public List<String> binds();

	public String containerIDFile();

	public List<IDockerConfParameter> lxcConf();

	public boolean privileged();

	public Map<String, List<IDockerPortBinding>> portBindings();

	public List<String> links();

	public boolean publishAllPorts();

	public List<String> dns();

	public List<String> dnsSearch();

	public List<String> volumesFrom();

	public String networkMode();

}
