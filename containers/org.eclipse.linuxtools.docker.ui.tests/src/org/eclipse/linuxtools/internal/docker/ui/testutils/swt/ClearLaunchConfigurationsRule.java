/*******************************************************************************
 * Copyright (c) 2016, 2018 Red Hat.
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

package org.eclipse.linuxtools.internal.docker.ui.testutils.swt;

import static org.junit.Assert.fail;

import java.util.stream.Stream;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.linuxtools.internal.docker.ui.launch.LaunchConfigurationUtils;
import org.junit.rules.ExternalResource;

/**
 * Removes all {@link ILaunchConfiguration} of a given type
 */
public class ClearLaunchConfigurationsRule extends ExternalResource {

	/** the id of {@link ILaunchConfiguration} type to remove. */
	private final String launchConfigTypeId;

	/**
	 * Constructor
	 *
	 * @param launchConfigTypeId
	 *            the id of the {@link ILaunchConfiguration} type to remove.
	 */
	public ClearLaunchConfigurationsRule(final String launchConfigTypeId) {
		this.launchConfigTypeId = launchConfigTypeId;
	}

	@Override
	protected void before() throws Throwable {
		final ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		final ILaunchConfigurationType launchConfigType = LaunchConfigurationUtils
				.getLaunchConfigType(launchConfigTypeId);
		Stream.of(manager.getLaunchConfigurations(launchConfigType)).forEach(launchConfig -> {
			try {
				launchConfig.delete();
			} catch (Exception e) {
				fail("Failed to remove a launch configuration  '" + launchConfig.getName() + "' of type '"
						+ this.launchConfigTypeId + "'");
			}
		});
	}
}
