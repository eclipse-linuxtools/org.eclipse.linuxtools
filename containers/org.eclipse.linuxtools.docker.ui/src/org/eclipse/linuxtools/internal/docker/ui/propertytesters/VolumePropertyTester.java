/*******************************************************************************
 * Copyright (c) 2017 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		if (receiver instanceof DockerContainerVolume) {
			final DockerContainerVolume volume = (DockerContainerVolume) receiver;
			switch (property) {
			case HAS_HOST_PATH:
				DockerConnection connection = (DockerConnection) volume
						.getContainer().getConnection();
				return connection.isLocal() && volume.getHostPath() != null
						&& !volume.getHostPath().isEmpty();
			}
		}
		return false;
	}

}
