/*******************************************************************************
 * Copyright (c) 2019 Red Hat.
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

import java.util.Map;

/**
 * @since 4.2
 */
public interface IDockerVolume {

	/**
	 * @return the name of the volume
	 */
	String name();

	/**
	 * @return the driver name of the volume
	 */
	String driver();

	/**
	 * @return map of driver opts
	 */
	Map<String, String> driverOpts();

	/**
	 * @return the options for the volume
	 */
	Map<String, String> options();

	/**
	 * @return labels for the volume
	 */
	Map<String, String> labels();
	
	/**
	 * @return the nount point of the volume
	 */
	String mountPoint();
	
	/**
	 * @return the scope of the volume
	 */
	String scope();

	/**
	 * @return the status of the volume
	 */
	Map<String, String> status();

}
