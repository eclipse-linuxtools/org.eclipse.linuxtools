/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Otavio Busatto Pontes <obusatto@br.ibm.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tools.launch.core;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.tools.launch.core"; //$NON-NLS-1$
	public static final String UI_PLUGIN_ID = "org.eclipse.linuxtools.tools.launch.ui"; //$NON-NLS-1$

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext bundleContext) {
		Activator.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext bundleContext) {
		Activator.context = null;
	}

}
