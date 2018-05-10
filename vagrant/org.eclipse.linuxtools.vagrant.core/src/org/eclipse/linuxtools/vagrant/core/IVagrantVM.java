/*******************************************************************************
 * Copyright (c) 2015, 2018 Red Hat.
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
package org.eclipse.linuxtools.vagrant.core;

import java.io.File;
import java.util.Map;

public interface IVagrantVM {

	String id();

	String name();

	String provider();

	String state();

	String state_desc();

	File directory();

	String ip();

	String user();

	int port();

	String identityFile();

	/**
	 * Return the environment to be passed to any vagrant command process
	 * launches
	 * 
	 * @return an environment
	 */
	Map<String, String> getEnvironment();
}
