/*******************************************************************************
 * Copyright (c) 2008 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.linuxtools.valgrind.massif;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class MassifPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.linuxtools.valgrind.massif"; //$NON-NLS-1$

	// The shared instance
	private static MassifPlugin plugin;
	
	protected ILaunchConfiguration config;
	protected ILaunch launch;
	
	/**
	 * The constructor
	 */
	public MassifPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static FontMetrics getFontMetrics(Control control) {
		 GC gc = new GC(control);
		 gc.setFont(control.getFont());
		 FontMetrics fontMetrics = gc.getFontMetrics();
		 gc.dispose();
		 return fontMetrics;
	}
	
	public ILaunch getLaunch() {
		return launch;
	}
	
	protected void setLaunch(ILaunch launch) {
		this.launch = launch;
	}
	
	public ILaunchConfiguration getConfig() {
		return config;
	}
	
	public void setConfig(ILaunchConfiguration config) {
		this.config = config;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static MassifPlugin getDefault() {
		return plugin;
	}

}
