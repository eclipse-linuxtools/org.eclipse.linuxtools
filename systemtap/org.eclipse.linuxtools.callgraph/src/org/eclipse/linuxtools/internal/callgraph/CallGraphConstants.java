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

package org.eclipse.linuxtools.internal.callgraph;

public class CallGraphConstants {
	
	private static String PLUGIN_LOCATION = ""; //$NON-NLS-1$
	public static final String viewID = "org.eclipse.linuxtools.callgraph.callgraphview"; //$NON-NLS-1$
	
	public static void setPluginLocation(String val) {
		PLUGIN_LOCATION = val;
	}
	
	public static String getPluginLocation() {
		return PLUGIN_LOCATION;
	}
		
		

}
