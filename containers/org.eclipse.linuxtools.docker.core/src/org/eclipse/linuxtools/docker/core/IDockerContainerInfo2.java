/*******************************************************************************
 * Copyright (c) 2020 Red Hat.
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

/**
 * @since 4.7
 * @author jjohnstn
 *
 */
public interface IDockerContainerInfo2 {

	List<IDockerContainerMount> mounts();

}
