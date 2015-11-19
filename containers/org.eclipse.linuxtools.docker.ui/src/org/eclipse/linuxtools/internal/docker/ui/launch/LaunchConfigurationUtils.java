/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/

package org.eclipse.linuxtools.internal.docker.ui.launch;

import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.COMMAND;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.CONTAINER_NAME;
import static org.eclipse.linuxtools.internal.docker.ui.launch.IRunDockerImageLaunchConfigurationConstants.IMAGE_NAME;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.linuxtools.docker.core.IDockerContainer;
import org.eclipse.linuxtools.docker.ui.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;

/**
 * Utility class to manage {@link ILaunchConfiguration}
 */
public class LaunchConfigurationUtils {

	public static final String RUN_IMAGE_CONFIGURATION_TYPE = "org.eclipse.linuxtools.docker.ui.runDockerImageLaunchConfigurationType"; //$NON-NLS-1$

	/**
	 * Private constructor for this utility class.
	 */
	private LaunchConfigurationUtils() {
		// empty
	}
	
	/**
	 * @return the ILaunchConfigurationType for the given configuration type.
	 * @param configType
	 *            the id of the configuration type
	 */
	public static ILaunchConfigurationType getLaunchConfigType(
			final String configType) {
		return DebugPlugin.getDefault().getLaunchManager()
				.getLaunchConfigurationType(configType);
	}

	public static ILaunchConfiguration saveLaunchConfiguration(
			final IDockerContainer container) {
		try {
			final ILaunchManager manager = DebugPlugin.getDefault()
					.getLaunchManager();
			final String configurationName = manager
					.generateLaunchConfigurationName(container.name());
			final ILaunchConfigurationType type = manager
					.getLaunchConfigurationType(RUN_IMAGE_CONFIGURATION_TYPE);
			final ILaunchConfigurationWorkingCopy workingCopy = type
					.newInstance(null, configurationName);
			workingCopy.setAttribute(IMAGE_NAME, container.image());
			workingCopy.setAttribute(CONTAINER_NAME, container.name());
			workingCopy.setAttribute(COMMAND, container.command());

		} catch (CoreException e) {
			Activator.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
					LaunchMessages.getString(
							"RunDockerImageLaunchConfiguration.creation.failure"), //$NON-NLS-1$
					e));
		}
		return null;
	}

	public static int getButtonWidthHint(Button button) {
		/* button.setFont(JFaceResources.getDialogFont()); */
		PixelConverter converter = new PixelConverter(button);
		int widthHint = converter
				.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint,
				button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
	}

	public static ILaunchConfiguration getLaunchConfiguration(
			final ILaunchConfigurationType type, final String name)
					throws CoreException {
		final ILaunchManager manager = DebugPlugin.getDefault()
				.getLaunchManager();
		for (ILaunchConfiguration launchConfiguration : manager
				.getLaunchConfigurations(type)) {
			if (launchConfiguration.getName().equals(name)) {
				return launchConfiguration;
			}
		}
		return null;
	}

	public static String convertToUnixPath(String path) {
		String unixPath = path;

		if (Platform.OS_WIN32.equals(Platform.getOS())) {
			// replace backslashes with slashes
			unixPath = unixPath.replaceAll("\\\\", "/");

			// replace "C:/" with "/c/"
			Matcher m = Pattern.compile("([a-zA-Z]):/").matcher(unixPath);
			if (m.find()) {
				StringBuffer b = new StringBuffer();
				b.append('/');
				m.appendReplacement(b, m.group(1).toLowerCase());
				b.append('/');
				m.appendTail(b);
				unixPath = b.toString();
			}
		}

		return unixPath;
	}

}
