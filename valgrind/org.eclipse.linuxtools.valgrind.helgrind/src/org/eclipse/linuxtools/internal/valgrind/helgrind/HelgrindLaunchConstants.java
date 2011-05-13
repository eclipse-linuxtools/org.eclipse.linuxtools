/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Daniel H Barboza <danielhb@br.ibm.com> - initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.valgrind.helgrind;

public final class HelgrindLaunchConstants {
	// LaunchConfiguration attributes
	public static final String ATTR_HELGRIND_LOCKORDERS = HelgrindPlugin.PLUGIN_ID + ".HELGRIND_LOCKORDERS";
	public static final String ATTR_HELGRIND_HISTORYLEVEL = HelgrindPlugin.PLUGIN_ID + ".HELGRIND_HISTORYLEVEL";
	public static final String ATTR_HELGRIND_CACHESIZE = HelgrindPlugin.PLUGIN_ID + ".HELGRIND_CACHESIZE";
	
	// default values
	public static final String HISTORY_NONE = "none";
	public static final String HISTORY_APPROX = "approx";
	public static final String HISTORY_FULL = "full";
	
	public static final boolean DEFAULT_HELGRIND_LOCKORDERS = true;
	public static final String DEFAULT_HELGRIND_HISTORYLEVEL = HISTORY_FULL;
	public static final int DEFAULT_HELGRIND_CACHESIZE = 1000000;

}
