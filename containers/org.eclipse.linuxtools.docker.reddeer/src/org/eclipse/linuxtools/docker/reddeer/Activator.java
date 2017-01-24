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
		log(new Status(IStatus.WARNING, PLUGIN_ID, IStatus.WARNING, message, null));
	}

	/**
	 * @deprecated see https://bugs.eclipse.org/bugs/show_bug.cgi?id=489111
	 */
	@Deprecated
	public static void logErrorMessage(final String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, null));
	}

	public static void logErrorMessage(final String message, final Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, e));
	}

}
