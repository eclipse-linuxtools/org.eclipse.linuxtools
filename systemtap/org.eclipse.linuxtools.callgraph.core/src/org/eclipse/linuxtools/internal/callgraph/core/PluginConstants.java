/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.callgraph.core;

public final class PluginConstants {

	private static String PLUGIN_LOCATION = ""; //$NON-NLS-1$
	public static final String CONFIGURATION_TYPE_ID = "org.eclipse.linuxtools.callgraph.launch.systemtapLaunch";	 //$NON-NLS-1$
	public static final String PARSER_CLASS = "org.eclipse.linuxtools.callgraph.core.parser"; //$NON-NLS-1$
	public static final String PARSER_RESOURCE = "org.eclipse.linuxtools.callgraph.core"; //$NON-NLS-1$
	public static final String PARSER_NAME = "parser";  //$NON-NLS-1$
	public static final String VIEW_CLASS = "org.eclipse.linuxtools.callgraph.core.view"; //$NON-NLS-1$
	public static final String VIEW_RESOURCE = "org.eclipse.ui"; //$NON-NLS-1$
	public static final String VIEW_NAME = "views";  //$NON-NLS-1$
	public static final String ATTR_CLASS = "class"; //$NON-NLS-1$
	public static final String ATTR_REALTIME = "realtime"; //$NON-NLS-1$
	public static final String VAL_TRUE = "true"; //$NON-NLS-1$

	public static final String NEW_LINE = "\n"; //$NON-NLS-1$
	public static final int MAX_ERRORS = 500;	//Maximum number of errors to tolerate
	public static int SYSTEMTAP_OPTIONS_TAB_HORIZONTAL_SPACING = 30;
	public static String STAP_PATH = "stap"; //$NON-NLS-1$

	public static final String DEFAULT_VIEW_ID = "org.eclipse.linuxtools.callgraph.core.staptextview"; //$NON-NLS-1$
	public static final String DEFAULT_PARSER_ID = "org.eclipse.linuxtools.callgraph.core.systemtaptextparser"; //$NON-NLS-1$
	private static String DEFAULT_OUTPUT = ""; //$NON-NLS-1$
	private static String STAP_GRAPH_DEFAULT_IO_PATH = ""; //$NON-NLS-1$

	/**
	 * Sets the absolute path of the Plugin folder and updates
	 * the PLUGIN_LOCATION.
	 *
	 * @param loc
	 */
	public static void setPluginLocation(String loc) {
		PLUGIN_LOCATION = loc;
	}

	public static String getPluginLocation() {
		if (PLUGIN_LOCATION.length() < 1) {
			PluginConstants.setPluginLocation(CallgraphCorePlugin.getPluginLocation());
		}

		return PLUGIN_LOCATION;
	}

	public static String getDefaultOutput() {
		if (DEFAULT_OUTPUT.length() < 1){
			DEFAULT_OUTPUT = CallgraphCorePlugin.getDefault().getStateLocation().toString()+"/"; //$NON-NLS-1$
		}

		return DEFAULT_OUTPUT;
	}

	public static String getDefaultIOPath() {
		if (STAP_GRAPH_DEFAULT_IO_PATH.length() < 1)
		 {
			STAP_GRAPH_DEFAULT_IO_PATH = CallgraphCorePlugin.getDefault().getStateLocation().toString()+"/callgraph.out"; //$NON-NLS-1$
		}
		return STAP_GRAPH_DEFAULT_IO_PATH;
	}


}
