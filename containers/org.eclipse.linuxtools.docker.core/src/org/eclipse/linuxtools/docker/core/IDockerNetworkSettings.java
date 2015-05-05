/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat.
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

/**
 * @author xcoulon
 *
 */
public interface IDockerNetworkSettings {

	String bridge();

	String gateway();

	String ipAddress();

	Integer ipPrefixLen();

	Map<String, Map<String, String>> portMapping();

	Map<String, List<IDockerPortBinding>> ports();

}
