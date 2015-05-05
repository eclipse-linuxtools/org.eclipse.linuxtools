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
import java.util.Set;

public interface IDockerContainerConfig {

	public String hostname();

	public String domainname();

	public String user();

	public Long memory();

	public Long memorySwap();

	public Long cpuShares();

	public String cpuset();

	public Boolean attachStdin();

	public Boolean attachStdout();

	public Boolean attachStderr();

	public List<String> portSpecs();

	public Set<String> exposedPorts();

	public Boolean tty();

	public Boolean openStdin();

	public Boolean stdinOnce();

	public List<String> env();

	public List<String> cmd();

	public String image();

	public Set<String> volumes();

	public String workingDir();

	public List<String> entrypoint();

	public Boolean networkDisabled();

	public List<String> onBuild();

}
