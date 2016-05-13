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
import java.util.Set;

public interface IDockerContainerConfig {

	String hostname();

	String domainname();

	String user();

	Long memory();

	Long memorySwap();

	Long cpuShares();

	String cpuset();

	boolean attachStdin();

	boolean attachStdout();

	boolean attachStderr();

	List<String> portSpecs();

	Set<String> exposedPorts();

	boolean tty();

	boolean openStdin();

	boolean stdinOnce();

	List<String> env();

	List<String> cmd();

	String image();

	Set<String> volumes();

	String workingDir();

	List<String> entrypoint();

	boolean networkDisabled();

	List<String> onBuild();

	// FIXME: to be included in Neon
	// public Map<String, String> labels();

}
