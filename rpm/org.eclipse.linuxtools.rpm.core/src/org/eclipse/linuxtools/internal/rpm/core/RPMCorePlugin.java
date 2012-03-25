/*******************************************************************************
 * Copyright (c) 2004-2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.rpm.core;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class RPMCorePlugin extends AbstractUIPlugin {
	//The shared instance.
	private static RPMCorePlugin plugin;
	
	/**
	 * The constructor.
	 */
	public RPMCorePlugin() {
		//super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static RPMCorePlugin getDefault() {
		return plugin;
	}

}
