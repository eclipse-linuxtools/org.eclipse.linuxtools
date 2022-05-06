/*******************************************************************************
 * Copyright (c) 2017,2018 Red Hat.
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
package org.eclipse.linuxtools.docker.reddeer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * 
 * @author jkopriva@redhat.com
 *
 */

public class Activator extends Plugin {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.docker.reddeer"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		plugin = this;
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		plugin = null;
		super.stop(bundleContext);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public static void log(IStatus status) {
		if (Activator.getDefault() != null && Activator.getDefault().getLog() != null) {
			Activator.getDefault().getLog().log(status);
		}
	}

	public static void logWarningMessage(final String message) {
		log(Status.warning(message));
	}

	public static void logErrorMessage(final String message, final Throwable e) {
		log(Status.error(message, e));
	}

}
