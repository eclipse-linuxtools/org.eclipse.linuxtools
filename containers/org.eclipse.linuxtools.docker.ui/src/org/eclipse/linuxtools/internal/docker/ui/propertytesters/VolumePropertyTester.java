/*******************************************************************************
 * Copyright (c) 2017, 2018 Red Hat.
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
package org.eclipse.linuxtools.internal.docker.ui.propertytesters;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.linuxtools.internal.docker.core.DockerConnection;
import org.eclipse.linuxtools.internal.docker.ui.views.DockerExplorerContentProvider.DockerContainerVolume;

public class VolumePropertyTester extends PropertyTester {

	/**
	 * Property name to check if a given {@link DockerContainerVolume} has path
	 * on local host.
	 */
	public static final String HAS_HOST_PATH = "hasHostPath"; //$NON-NLS-1$

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		try {
			if (receiver instanceof DockerContainerVolume volume) {
				switch (property) {
				case HAS_HOST_PATH:
					DockerConnection connection = (DockerConnection) volume.getContainer().getConnection();
					return connection.isLocal() && volume.getHostPath() != null && !volume.getHostPath().isEmpty();
				}
			}
		} catch (Exception e) {
			// fall-through
		}
		return false;
	}

}
