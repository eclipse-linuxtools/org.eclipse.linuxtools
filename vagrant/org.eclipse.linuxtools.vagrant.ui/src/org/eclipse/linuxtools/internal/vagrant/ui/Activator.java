/*******************************************************************************
 * Copyright (c) 2014, 2018 Red Hat Inc.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - Initial Contribution
 *******************************************************************************/
package org.eclipse.linuxtools.internal.vagrant.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.terminal.view.ui.launcher.ILauncherDelegateManager;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.vagrant.ui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		final String LAUNCH_ID = "firstLaunch"; //$NON-NLS-1$
		final boolean firstLaunch = InstanceScope.INSTANCE.getNode(PLUGIN_ID)
				.getBoolean(LAUNCH_ID, true);
		if (Platform.OS_LINUX.equals(Platform.getOS()) && firstLaunch) {
			Thread t = new Thread(() -> {
				File polkitDir = Paths.get("/", "usr", "share", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						"polkit-1", "actions").toFile(); //$NON-NLS-1$ //$NON-NLS-2$
				if (polkitDir.exists()) {
					File[] libvirtFiles = polkitDir.listFiles((File dir, String name) -> {
						return name.matches("org\\.libvirt.*\\.policy"); //$NON-NLS-1$
					});
					if (libvirtFiles.length > 0) {
						Display.getDefault()
						.syncExec(() -> MessageDialog.openWarning(Display.getCurrent()
								.getActiveShell(),
										Messages.Activator_additional_configuration_title,
										Messages.Activator_additional_configuration_msg));
					}
				}
			});
			t.start();
			InstanceScope.INSTANCE.getNode(PLUGIN_ID).putBoolean(LAUNCH_ID, false);
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public static void log(IStatus status) {
		Activator.getDefault().getLog().log(status);
	}

	public static void logErrorMessage(String message) {
		log(Status.error(message));
	}

	public static void log(Throwable e) {
		if (e instanceof InvocationTargetException)
			e = ((InvocationTargetException) e).getTargetException();
		IStatus status = null;
		if (e instanceof CoreException)
			status = ((CoreException) e).getStatus();
		else
			status = Status.error(e.getMessage(), e);
		log(status);
	}

	private static ServiceTracker<ILauncherDelegateManager, ILauncherDelegateManager> launchDelegateServiceTracker;

	public static synchronized ILauncherDelegateManager getLaunchDelegateManager() {
		if (launchDelegateServiceTracker == null) {
			launchDelegateServiceTracker = new ServiceTracker<>(getDefault().getBundle().getBundleContext(),
					ILauncherDelegateManager.class, null);
			launchDelegateServiceTracker.open();
		}
		return launchDelegateServiceTracker.getService();
	}

}
