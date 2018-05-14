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

	/**
	 * @since 4.0
	 */
	@SuppressWarnings("rawtypes")
	Map<String, Map> volumes();

	String workingDir();

	List<String> entrypoint();

	boolean networkDisabled();

	List<String> onBuild();

	Map<String, String> labels();

}
