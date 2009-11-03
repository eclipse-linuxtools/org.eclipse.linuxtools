/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Elliott Baron <ebaron@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.valgrind.core;

public final class PluginConstants {
	// IDs for important plugins
	public static final String CORE_PLUGIN_ID = "org.eclipse.linuxtools.valgrind.core"; //$NON-NLS-1$
	public static final String LAUNCH_PLUGIN_ID = "org.eclipse.linuxtools.valgrind.launch"; //$NON-NLS-1$
	public static final String UI_PLUGIN_ID = "org.eclipse.linuxtools.valgrind.ui"; //$NON-NLS-1$
	
	// Extension point constants
	public static final String TOOL_EXT_ID = "valgrindTools"; //$NON-NLS-1$
	public static final String TOOL_EXT_DEFAULT = LAUNCH_PLUGIN_ID + ".memcheck"; //$NON-NLS-1$
	public static final String VIEW_EXT_ID = "valgrindToolViews"; //$NON-NLS-1$
	public static final String OUTPUT_DIR_EXT_ID = "outputDirectoryProviders"; //$NON-NLS-1$
	
	// Extension constants
	public static final String EXPORT_CMD_ID = LAUNCH_PLUGIN_ID + ".exportCommand"; //$NON-NLS-1$
}
